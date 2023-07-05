package com.lunatech.cmt.client.command

import caseapp.{AppName, CommandName, ExtraName, HelpMessage, Recurse, RemainingArgs}
import com.lunatech.cmt.client.{Configuration, CoursesDirectory}
import com.lunatech.cmt.client.Domain.{InstallationSource, StudentifiedRepo}
import com.lunatech.cmt.{
  CMTcConfig,
  CmtError,
  GenericError,
  ProcessDSL,
  printErrorAndExit,
  printMessage,
  printResult,
  toExecuteCommandErrorMessage
}
import com.lunatech.cmt.client.cli.CmtcCommand
import com.lunatech.cmt.client.command.Executable
import com.lunatech.cmt.core.validation.Validatable
import com.lunatech.cmt.client.cli.ArgParsers.{installationSourceArgParser, studentifiedRepoArgParser}
import cats.effect.unsafe.implicits.global
import cats.syntax.either.*
import sbt.io.syntax.{URL, *}
import sbt.io.IO as sbtio
import com.lunatech.cmt.ProcessDSL.{ProcessCmd, runAndReadOutput, toProcessCmd}
import com.lunatech.cmt.client.Domain.InstallationSource.{GithubProject, LocalDirectory, ZipFile}
import com.lunatech.cmt.core.cli.enforceNoTrailingArguments
import org.http4s.client.Client
import org.http4s.client.JavaNetClientBuilder
import cats.effect.IO
import com.lunatech.cmt.client.Configuration.GithubApiToken
import github4s.Github

import java.io.{File, FileInputStream, FileOutputStream}
import java.nio.file.Path
import java.util.zip.ZipInputStream
import scala.concurrent.Await
import scala.concurrent.duration.*
import scala.sys.process.*

object Install:

  @AppName("install")
  @CommandName("install")
  @HelpMessage(
    "Install a course - from either a local directory, a zip file on the local file system or a Github project")
  final case class Options(
      @ExtraName("s")
      source: InstallationSource)

  given Validatable[Install.Options] with
    extension (options: Install.Options)
      def validated(): Either[CmtError, Install.Options] =
        Right(options)
      end validated
  end given

  given Executable[Install.Options] with
    extension (cmd: Install.Options)
      def execute(configuration: Configuration): Either[CmtError, String] =
        cmd.source match {
          case localDirectory: LocalDirectory => installFromLocalDirectory(localDirectory)
          case zipFile: ZipFile               => installFromZipFile(zipFile, configuration)
          case githubProject: GithubProject   => installFromGithubProject(githubProject, configuration)
        }

      private def installFromLocalDirectory(localDirectory: LocalDirectory): Either[CmtError, String] =
        Left(GenericError(
          s"unable to install course from local directory at '${localDirectory.value.getCanonicalPath}' - installing from a local directory is not supported... yet"))

      private def installFromZipFile(zipFile: ZipFile, configuration: Configuration): Either[CmtError, String] =
        println(s"INSTALLING FROM ZIP FILE - ${zipFile.value}")
        sbtio.unzip(zipFile.value, configuration.coursesDirectory.value)
        Right(s"Unzipped '${zipFile.value.name}' to '${configuration.coursesDirectory.value.getAbsolutePath}'")

      private def installFromGithubProject(
          githubProject: GithubProject,
          configuration: Configuration): Either[CmtError, String] =
        println(s"INSTALLING FROM GITHUB - ${githubProject.displayName}")
        implicit val httpClient: Client[IO] = JavaNetClientBuilder[IO].create
        val github = Github[IO](httpClient, Some(configuration.githubApiToken.value))
        val latestRelease =
          github.repos.latestRelease(githubProject.organisation, githubProject.project).unsafeToFuture()
        val response = Await.result(latestRelease, 10.seconds)
        println(s"Found release: ${response.result}")

        response.result match {
          case Left(error) =>
            Left(s"failed to retrieve latest release of ${githubProject.displayName}".toExecuteCommandErrorMessage)
          case Right(None) =>
            Left(s"failed to retrieve latest release of ${githubProject.displayName}".toExecuteCommandErrorMessage)
          case Right(Some(result)) =>
            result.zipball_url match {
              case Some(zipballUrl) =>
                val downloadDir = file(
                  s"${configuration.coursesDirectory.value.getAbsolutePath}/${githubProject.project}/${result.tag_name}")
                sbtio.createDirectory(downloadDir)
                val zipFile = ZipFile(file(s"$downloadDir/${githubProject.project}-${result.tag_name}.zip"))
                downloadFile(zipballUrl, zipFile)
                installFromZipFile(zipFile, configuration).flatMap { _ =>
                  sbtio.delete(
                    file(s"${configuration.coursesDirectory.value.getAbsolutePath}/${githubProject.project}"))
                  Right(
                    s"${githubProject.project} (${result.tag_name}) successfully installed to ${configuration.coursesDirectory.value}/${githubProject.project}")
                }
              case None =>
                Left(
                  s"Failed to install ${githubProject.displayName} - No zip of the latest release is available.".toExecuteCommandErrorMessage)
            }
        }

      private def downloadFile(fileUri: String, destination: ZipFile)(implicit client: Client[IO]): Unit =
        import java.net.URL
        println(s"DOWNLOADING '$cmd'")
        (new URL(fileUri) #> new File(destination.value.getAbsolutePath)).!!

    end extension
  end given

  val command = new CmtcCommand[Install.Options] {

    def run(options: Install.Options, args: RemainingArgs): Unit =
      args
        .enforceNoTrailingArguments()
        .flatMap(_ => options.validated().flatMap(_.execute(configuration)))
        .printResult()
  }

end Install
