package cmt.client.command.execution

import cmt.Helpers.withZipFile
import cmt.Helpers.fileList
import cmt.client.command.ClientCommand.PullSolution
import cmt.core.execution.Executable

import java.nio.charset.StandardCharsets
import cmt.{toConsoleGreen, toConsoleYellow}
import sbt.io.IO as sbtio
import sbt.io.syntax.fileToRichFile
import sbt.io.syntax.singleFileFinder

given Executable[PullSolution] with
  extension (cmd: PullSolution)
    def execute(): Either[String, String] = {
      val currentExercise =
        sbtio.readLines(cmd.config.bookmarkFile, StandardCharsets.UTF_8).head

      deleteCurrentState(cmd.studentifiedRepo.value)(cmd.config)

      withZipFile(cmd.config.solutionsFolder, currentExercise) { solution =>
        val files = fileList(solution / currentExercise)
        sbtio.copyDirectory(
          cmd.config.solutionsFolder / currentExercise,
          cmd.config.activeExerciseFolder,
          preserveLastModified = true)
        Right(toConsoleGreen(s"Pulled solution for $currentExercise"))
      }
    }
