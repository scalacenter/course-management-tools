package cmt.client.command

import caseapp.{AppName, CommandName, HelpMessage, Recurse, RemainingArgs}
import cmt.client.Domain.ExerciseId
import cmt.client.cli.ArgParsers.exerciseIdArgParser
import cmt.client.cli.SharedOptions
import cmt.client.command.deleteCurrentState
import cmt.core.CmtCommand
import cmt.core.execution.Executable
import cmt.core.validation.Validatable
import cmt.*
import sbt.io.IO as sbtio
import sbt.io.syntax.*

object RestoreState:

  @AppName("restore-state")
  @CommandName("restore-state")
  @HelpMessage("Restore a previously saved exercise state")
  final case class Options(exercise: ExerciseId, @Recurse shared: SharedOptions)

  given Validatable[RestoreState.Options] with
    extension (options: RestoreState.Options)
      def validated(): Either[CmtError, RestoreState.Options] =
        Right(options)
      end validated
  end given

  given Executable[RestoreState.Options] with
    extension (cmd: RestoreState.Options)
      def execute(): Either[CmtError, String] = {
        val config = new CMTcConfig(cmd.shared.studentifiedRepo.value)
        val savedState = config.studentifiedSavedStatesFolder / s"${cmd.exercise.value}.zip"
        if !savedState.exists
        then Left(s"No such saved state: ${cmd.exercise.value}".toExecuteCommandErrorMessage)
        else {
          deleteCurrentState(cmd.shared.studentifiedRepo.value)(config)

          Helpers.withZipFile(config.studentifiedSavedStatesFolder, cmd.exercise.value.toString) { solution =>
            val files = Helpers.fileList(solution / cmd.exercise.value.toString)
            sbtio.copyDirectory(
              config.studentifiedSavedStatesFolder / cmd.exercise.value.toString,
              config.activeExerciseFolder,
              preserveLastModified = true)

            Helpers.writeStudentifiedCMTBookmark(config.bookmarkFile, cmd.exercise.value.toString)
            Right(toConsoleGreen(s"Restored state for ${cmd.exercise.value}"))
          }
        }
      }

  val command = new CmtCommand[RestoreState.Options] {

    def run(options: RestoreState.Options, args: RemainingArgs): Unit =
      options.validated().flatMap(_.execute()).printResult()
  }
end RestoreState
