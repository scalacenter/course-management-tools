package cmt.client.command

import caseapp.{AppName, CommandName, Recurse, RemainingArgs}
import cmt.Helpers.zipAndDeleteOriginal
import cmt.client.cli.SharedOptions
import cmt.client.command.ClientCommand.SaveState
import cmt.client.command.execution.{getCurrentExerciseId, getCurrentExerciseState}
import cmt.core.CmtCommand
import cmt.core.execution.Executable
import cmt.core.validation.Validatable
import cmt.*
import sbt.io.IO as sbtio
import sbt.io.syntax.*

object SaveState:

  @AppName("save-state")
  @CommandName("save-state")
  final case class Options(@Recurse shared: SharedOptions)

  given Validatable[SaveState.Options] with
    extension (options: SaveState.Options)
      def validated(): Either[CmtError, SaveState.Options] =
        Right(options)
      end validated
  end given

  given Executable[SaveState.Options] with
    extension (cmd: SaveState.Options)
      def execute(): Either[CmtError, String] = {
        val config = new CMTcConfig(cmd.shared.studentifiedRepo.value)
        val currentExerciseId = getCurrentExerciseId(config.bookmarkFile)
        val savedStatesFolder = config.studentifiedSavedStatesFolder

        sbtio.delete(savedStatesFolder / currentExerciseId)
        val filesInScope = getCurrentExerciseState(cmd.shared.studentifiedRepo.value)(config)

        for {
          file <- filesInScope
          f <- file.relativeTo(config.activeExerciseFolder)
          dest = savedStatesFolder / currentExerciseId / f.getPath
        } {
          sbtio.touch(dest)
          sbtio.copyFile(file, dest)
        }

        zipAndDeleteOriginal(
          baseFolder = savedStatesFolder,
          zipToFolder = savedStatesFolder,
          exercise = currentExerciseId)

        Right(toConsoleGreen(s"Saved state for $currentExerciseId"))
      }

  val command = new CmtCommand[SaveState.Options] {

    def run(options: SaveState.Options, args: RemainingArgs): Unit =
      options.validated().flatMap(_.execute()).printResult()
  }
end SaveState
