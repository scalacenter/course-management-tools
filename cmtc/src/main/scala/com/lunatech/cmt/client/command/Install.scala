package com.lunatech.cmt.client.command

import caseapp.*
import cats.syntax.either.*
import com.lunatech.cmt.*
import com.lunatech.cmt.Domain.InstallationSource.{GithubProject, LocalDirectory, ZipFile}
import com.lunatech.cmt.Domain.{InstallationSource, StudentifiedRepo}
import com.lunatech.cmt.Helpers.ignoreProcessStdOutStdErr
import com.lunatech.cmt.client.Configuration
import com.lunatech.cmt.client.cli.CmtcCommand
import com.lunatech.cmt.core.cli.ArgParsers.installationSourceArgParser
import com.lunatech.cmt.core.cli.enforceNoTrailingArguments
import com.lunatech.cmt.core.validation.Validatable
import sbt.io.IO as sbtio
import sbt.io.syntax.*

import sys.process.*
import scala.util.{Failure, Success, Try}
import java.io.File
import java.net.URL

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
        options.asRight
      end validated
  end given

  given Executable[Install.Options] with
    extension (cmd: Install.Options)
      def execute(configuration: Configuration): Either[CmtError, String] =
        cmd.source match {
          case localDirectory: LocalDirectory         => installFromLocalDirectory(localDirectory)
          case zipFile: ZipFile                       => installFromZipFile(zipFile, configuration)
          case githubProject @ GithubProject(_, _, _) => installFromGithubProject(githubProject, configuration)
        }

      private def installFromLocalDirectory(localDirectory: LocalDirectory): Either[CmtError, String] =
        GenericError(
          s"unable to install course from local directory at '${localDirectory.value.getCanonicalPath}' - installing from a local directory is not supported... yet").asLeft

      private def installFromZipFile(
          zipFile: ZipFile,
          configuration: Configuration,
          deleteZipAfterInstall: Boolean = false): Either[CmtError, String] =
        sbtio.unzip(zipFile.value, configuration.coursesDirectory.value)
        if (deleteZipAfterInstall) {
          sbtio.delete(zipFile.value)
        }
        s"Unzipped '${zipFile.value.name}' to '${configuration.coursesDirectory.value.getAbsolutePath}'".asRight

      private def extractTag(lsFilesTagLine: String): String =
        lsFilesTagLine.replaceAll(""".*refs/tags/""", "")
      private def installFromGithubProject(
          githubProject: GithubProject,
          configuration: Configuration): Either[CmtError, String] = {
        val cwd = file(".").getCanonicalFile
        // TODO This may be brittle; if the user doesn't have its git credentials set, we
        // may want to retry using HTTP
        // if it fails, we may still attempt to download the artefact
        val maybeTags = Try(
          Process(
            Seq(
              "git",
              "-c",
              "versionsort.suffix=-",
              "ls-remote",
              "--tags",
              "--refs",
              "--sort",
              "v:refname",
              s"git@github.com:${githubProject.organisation}/${githubProject.project}.git"),
            cwd).!!(ignoreProcessStdOutStdErr).split("\n").to(Seq).map(extractTag))
        val tags: Seq[String] = maybeTags match {
          case Success(s) => s
          case Failure(_) => Seq.empty[String]
        }

        val aTagWasPassedToInstall = githubProject.tag.isDefined
        val aTagWasPassedToInstallWhichMatchesARelease =
          githubProject.tag.isDefined && tags.contains(githubProject.tag.get)

        (aTagWasPassedToInstall, aTagWasPassedToInstallWhichMatchesARelease) match {
          case (false, _) =>
            s"${githubProject.displayName}: Missing tag".toExecuteCommandErrorMessage.asLeft
          case (true, false) =>
            s"${githubProject.displayName}. ${githubProject.tag.get}: No such tag".toExecuteCommandErrorMessage.asLeft
          case (true, true) =>
            downloadAndInstallStudentifiedRepo(githubProject, githubProject.tag.get, configuration)
        }
      }

      private def downloadAndInstallStudentifiedRepo(
          githubProject: GithubProject,
          tag: String,
          configuration: Configuration): Either[CmtError, String] =
        for {
          studentAssetUrl <- getStudentAssetUrl(githubProject, tag)
          _ = printMessage(s"downloading studentified course from '$studentAssetUrl' to courses directory")
          downloadedZipFile <- downloadStudentAsset(studentAssetUrl, githubProject, configuration)
          _ <- installFromZipFile(downloadedZipFile, configuration, deleteZipAfterInstall = true)
          _ <- setCurrentCourse(githubProject, configuration)
        } yield s"${githubProject.project} (${tag}) successfully installed to ${configuration.coursesDirectory.value}/${githubProject.project}"

      private def setCurrentCourse(
          githubProject: GithubProject,
          configuration: Configuration): Either[CmtError, String] = {
        val courseDirectory = configuration.coursesDirectory.value / githubProject.project
        val studentifiedRepo = StudentifiedRepo(courseDirectory)
        SetCurrentCourse.Options(studentifiedRepo).execute(configuration)
      }

      private def getStudentAssetUrl(githubProject: GithubProject, tag: String): Either[CmtError, String] = {
        val organisation = githubProject.organisation
        val project = githubProject.project
        val tag = githubProject.tag.get
        Right(s"https://github.com/$organisation/$project/releases/download/$tag/$project-student.zip")
      }

      private def downloadStudentAsset(
          url: String,
          githubProject: GithubProject,
          configuration: Configuration): Either[CmtError, ZipFile] = {
        val zipFile = ZipFile(configuration.coursesDirectory.value / s"${githubProject.project}.zip")
        downloadFile(url, zipFile)
        zipFile.asRight
      }

      private def downloadFile(fileUri: String, destination: ZipFile): Unit =
        val _ = (new URL(fileUri) #> new File(destination.value.getAbsolutePath)).!!

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
