package coursemgmt.client.command

import caseapp.{AppName, CommandName, ExtraName, HelpMessage, RemainingArgs}
import coursemgmt.Helpers.zipAndDeleteOriginal
import coursemgmt.client.command.getCurrentExerciseStateExceptDontTouch
import coursemgmt.core.validation.Validatable
import coursemgmt.*
import coursemgmt.client.Configuration
import Domain.StudentifiedRepo
import coursemgmt.client.cli.CmtcCommand
import sbt.io.IO as sbtio
import sbt.io.syntax.*
import coursemgmt.core.cli.enforceNoTrailingArguments
import coursemgmt.core.cli.ArgParsers.studentifiedRepoArgParser
import coursemgmt.core.command.Package.getCurrentExerciseId

object SaveState:

  @AppName("save-state")
  @CommandName("save-state")
  @HelpMessage("Save the state of the active exercise")
  final case class Options(
      @ExtraName("s")
      studentifiedRepo: Option[StudentifiedRepo] = None)

  given Validatable[SaveState.Options] with
    extension (options: SaveState.Options)
      def validated(): Either[CmtError, SaveState.Options] =
        Right(options)
      end validated
  end given

  given Executable[SaveState.Options] with
    extension (options: SaveState.Options)
      def execute(configuration: Configuration): Either[CmtError, String] = {
        val studentifiedRepo = options.studentifiedRepo.getOrElse(configuration.currentCourse.value)
        val config = new CMTcConfig(studentifiedRepo.value)
        val currentExerciseId = getCurrentExerciseId(config.bookmarkFile)
        val savedStatesFolder = config.studentifiedSavedStatesFolder

        sbtio.delete(savedStatesFolder / currentExerciseId)
        val ExerciseFiles(filesInScope, _) = getCurrentExerciseStateExceptDontTouch(studentifiedRepo.value)(config)

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

        Right(toConsoleGreen(s"Saved state for ${toConsoleYellow(currentExerciseId)}"))
      }

  val command = new CmtcCommand[SaveState.Options] {

    def run(options: SaveState.Options, args: RemainingArgs): Unit =
      args
        .enforceNoTrailingArguments()
        .flatMap(_ => options.validated().flatMap(_.execute(configuration)))
        .printResult()
  }
end SaveState
