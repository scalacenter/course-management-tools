package com.lunatech.cmt.client.command

import caseapp.{AppName, CommandName, ExtraName, HelpMessage, RemainingArgs}
import com.lunatech.cmt.Helpers.zipAndDeleteOriginal
import com.lunatech.cmt.client.command.{getCurrentExerciseId, getCurrentExerciseStateExceptDontTouch}
import com.lunatech.cmt.core.validation.Validatable
import com.lunatech.cmt.*
import com.lunatech.cmt.client.Configuration
import Domain.StudentifiedRepo
import com.lunatech.cmt.client.cli.CmtcCommand
import sbt.io.IO as sbtio
import sbt.io.syntax.*
import com.lunatech.cmt.core.cli.enforceNoTrailingArguments
import com.lunatech.cmt.core.cli.ArgParsers.studentifiedRepoArgParser

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
