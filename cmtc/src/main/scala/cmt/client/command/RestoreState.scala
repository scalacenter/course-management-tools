package cmt.client.command

import cmt.client.Domain.ExerciseId
import caseapp.{AppName, CommandName, ExtraName, HelpMessage, Recurse, RemainingArgs}
import cmt.client.Domain.{ExerciseId, TemplatePath}
import cmt.client.cli.ArgParsers.exerciseIdArgParser
import cmt.client.cli.SharedOptions
import cmt.client.command.deleteCurrentState
import cmt.core.execution.Executable
import cmt.core.validation.Validatable
import cmt.*
import cmt.core.cli.CmtCommand
import sbt.io.IO as sbtio
import sbt.io.syntax.*
import cmt.core.cli.enforceTrailingArgumentCount

object RestoreState:

  @AppName("restore-state")
  @CommandName("restore-state")
  @HelpMessage("Restore a previously saved exercise state")
  final case class Options(
      @ExtraName("e")
      exercise: Option[ExerciseId] = None,
      @Recurse shared: SharedOptions)

  given Validatable[RestoreState.Options] with
    extension (options: RestoreState.Options)
      def validated(): Either[CmtError, RestoreState.Options] =
        Right(options)
      end validated
  end given

  given Executable[RestoreState.Options] with
    extension (options: RestoreState.Options)
      def execute(): Either[CmtError, String] = {
        val config = new CMTcConfig(options.shared.studentifiedRepo.value)

        options.exercise
          .map { exercise =>
            val savedState = config.studentifiedSavedStatesFolder / s"${exercise.value}.zip"
            if !savedState.exists
            then Left(s"No such saved state: ${exercise.value}".toExecuteCommandErrorMessage)
            else {
              deleteCurrentState(options.shared.studentifiedRepo.value)(config)

              Helpers.withZipFile(config.studentifiedSavedStatesFolder, exercise.value) { solution =>
                val files = Helpers.fileList(solution / exercise.value)
                sbtio.copyDirectory(
                  config.studentifiedSavedStatesFolder / exercise.value,
                  config.activeExerciseFolder,
                  preserveLastModified = true)

                Helpers.writeStudentifiedCMTBookmark(config.bookmarkFile, exercise.value)
                Right(toConsoleGreen(s"Restored state for ${exercise.value}"))
              }
            }
          }
          .getOrElse(Left("Exercise ID not specified".toExecuteCommandErrorMessage))
      }

  val command = new CmtCommand[RestoreState.Options] {

    def run(options: RestoreState.Options, args: RemainingArgs): Unit =
      args
        .enforceTrailingArgumentCount(expectedCount = 1)
        .flatMap(
          _.remaining.headOption
            .map(exercise => options.copy(exercise = Some(ExerciseId(exercise))))
            .getOrElse(options)
            .validated()
            .flatMap(_.execute()))
        .printResult()
  }
end RestoreState
