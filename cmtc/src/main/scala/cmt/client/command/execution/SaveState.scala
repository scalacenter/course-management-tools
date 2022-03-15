package cmt.client.command.execution

import cmt.Helpers.zipAndDeleteOriginal
import cmt.client.command.ClientCommand.SaveState
import cmt.core.execution.Executable
import cmt.{Helpers, toConsoleGreen}
import sbt.io.IO as sbtio
import sbt.io.syntax.fileToRichFile
import sbt.io.syntax.singleFileFinder

import java.nio.charset.StandardCharsets

given Executable[SaveState] with
  extension (cmd: SaveState)
    def execute(): Either[String, String] = {
      val currentExercise =
        sbtio.readLines(cmd.config.bookmarkFile, StandardCharsets.UTF_8).head
      val savedStatesFolder = cmd.config.studentifiedSavedStatesFolder
      sbtio.delete(savedStatesFolder / currentExercise)
      val filesInScope = getCurrentExerciseState(cmd.studentifiedRepo.value)(cmd.config)

      for {
        file <- filesInScope
        f <- file.relativeTo(cmd.config.activeExerciseFolder)
        dest = savedStatesFolder / currentExercise / f.getPath
      } {
        sbtio.touch(dest)
        sbtio.copyFile(file, dest)
      }

      zipAndDeleteOriginal(baseFolder = savedStatesFolder, zipToFolder = savedStatesFolder, exercise = currentExercise)

      Right(toConsoleGreen(s"Saved state for $currentExercise"))
    }
