package cmt.client.command

import caseapp.{AppName, CommandName, ExtraName, HelpMessage, Recurse, RemainingArgs}
import cmt.{CMTcConfig, CmtError, printResult, toConsoleGreen, toConsoleYellow, toExecuteCommandErrorMessage}
import cmt.Helpers.{getFilesToCopyAndDelete, pullTestCode}
import cmt.client.{Configuration, CurrentCourse}
import cmt.client.Domain.{ExerciseId, ForceMoveToExercise, StudentifiedRepo}
import cmt.client.cli.CmtcCommand
import cmt.client.command.Executable
import cmt.core.validation.Validatable
import cmt.core.cli.enforceNoTrailingArguments
import sbt.io.syntax.*
import cmt.client.cli.ArgParsers.studentifiedRepoArgParser

object SetCurrentCourse:

  @AppName("set-current-course")
  @CommandName("set-current-course")
  @HelpMessage("Sets the current course to point to a directory")
  final case class Options(
      @ExtraName("s")
      directory: StudentifiedRepo)

  given Validatable[SetCurrentCourse.Options] with
    extension (options: SetCurrentCourse.Options)
      def validated(): Either[CmtError, SetCurrentCourse.Options] =
        Right(options)
      end validated
  end given

  given Executable[SetCurrentCourse.Options] with
    extension (options: SetCurrentCourse.Options)
      def execute(configuration: Configuration): Either[CmtError, String] =
        configuration
          .copy(currentCourse = CurrentCourse(options.directory))
          .flush()
          .map(_ => s"Current course set to '${options.directory.value.getAbsolutePath}'")

  val command = new CmtcCommand[SetCurrentCourse.Options] {

    def run(options: SetCurrentCourse.Options, args: RemainingArgs): Unit =
      args
        .enforceNoTrailingArguments()
        .flatMap(_ => options.validated().flatMap(_.execute(configuration)))
        .printResult()
  }

end SetCurrentCourse
