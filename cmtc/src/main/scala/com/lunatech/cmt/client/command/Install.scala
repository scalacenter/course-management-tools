package com.lunatech.cmt.client.command

import caseapp.{AppName, CommandName, ExtraName, HelpMessage, Recurse, RemainingArgs}
import com.lunatech.cmt.client.{Configuration, CoursesDirectory}
import com.lunatech.cmt.client.Domain.{InstallationSource, StudentifiedRepo}
import com.lunatech.cmt.{CMTcConfig, CmtError, GenericError, ProcessDSL, printErrorAndExit, printMessage, printResult}
import com.lunatech.cmt.client.cli.CmtcCommand
import com.lunatech.cmt.client.command.Executable
import com.lunatech.cmt.core.validation.Validatable
import com.lunatech.cmt.client.cli.ArgParsers.{installationSourceArgParser, studentifiedRepoArgParser}
import cats.syntax.either.*
import sbt.io.syntax.*
import sbt.io.IO as sbtio
import com.lunatech.cmt.ProcessDSL.{ProcessCmd, runAndReadOutput, toProcessCmd}
import com.lunatech.cmt.client.Domain.InstallationSource.{GithubProject, LocalDirectory, ZipFile}
import com.lunatech.cmt.core.cli.enforceNoTrailingArguments

object Install:

  @AppName("install")
  @CommandName("install")
  @HelpMessage("Install a course - from either a local directory, a zip file on the local file system or a Github project")
  final case class Options(
    @ExtraName("s")
    source: InstallationSource
  )

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
          case zipFile: ZipFile => installFromZipFile(zipFile)
          case githubProject: GithubProject => installFromGithubProject(githubProject, configuration)
        }

      private def installFromLocalDirectory(localDirectory: LocalDirectory): Either[CmtError, String] =
        Left(GenericError(s"unable to install course from local directory at '${localDirectory.value.getCanonicalPath}' - installing from a local directory is not supported... yet"))

      private def installFromZipFile(zipFile: ZipFile): Either[CmtError, String] =
        Left(GenericError(s"unable to install course from zip file at '${zipFile.value.getCanonicalPath}' - installing from a zip file is not supported... yet"))

      private def installFromGithubProject(githubProject: GithubProject, configuration: Configuration): Either[CmtError, String] = {
        printMessage(s"Installing course '${githubProject.displayName}' into '${configuration.coursesDirectory}'")
        val workingDir = configuration.coursesDirectory.value / githubProject.organisation / githubProject.project
        sbtio.createDirectory(workingDir)
        val cloneCommand = s"git clone git@github.com:${githubProject.displayName}.git".toProcessCmd(workingDir)
        cloneCommand.runAndReadOutput()
      }
      
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
