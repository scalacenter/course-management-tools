package cmt.client.command

import caseapp.{AppName, CommandName, ExtraName, Recurse, RemainingArgs}
import cmt.client.Configuration
import cmt.client.Domain.GithubCourseRef
import cmt.{CMTcConfig, CmtError, ProcessDSL, printErrorAndExit, printMessage, printResult}
import cmt.client.cli.{CmtcCommand, SharedOptions}
import cmt.client.command.Executable
import cmt.core.validation.Validatable
import cmt.client.cli.ArgParsers.githubCourseRefArgParser
import cats.syntax.either.*
import sbt.io.syntax.*
import sbt.io.IO as sbtio
import cmt.ProcessDSL.{ProcessCmd, runAndReadOutput, toProcessCmd}

object Install:

  @AppName("install")
  @CommandName("install")
  final case class Options(
      @ExtraName("c")
      course: GithubCourseRef)

  given Validatable[Install.Options] with
    extension (options: Install.Options)
      def validated(): Either[CmtError, Install.Options] =
        Right(options)
      end validated
  end given

  given Executable[Install.Options] with
    extension (cmd: Install.Options)
      def execute(configuration: Configuration): Either[CmtError, String] = {
        printMessage(s"Installing course '${cmd.course}' into '${configuration.coursesDirectory}'")
        val workingDir = configuration.coursesDirectory.value / cmd.course.organisation / cmd.course.project
        sbtio.createDirectory(workingDir)
        val cloneCommand = s"git clone git@github.com:${cmd.course.value}.git".toProcessCmd(workingDir)
        cloneCommand.runAndReadOutput()
      }
    end extension
  end given

  val command = new CmtcCommand[Install.Options] {

    def run(options: Install.Options, args: RemainingArgs): Unit = {
      options.validated().flatMap(_.execute(configuration)).printResult()
    }
  }

end Install
