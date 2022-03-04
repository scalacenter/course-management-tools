package cmt.client.command.execution

import cmt.Helpers
import cmt.client.command.ClientCommand.RestoreState
import cmt.core.execution.Executable
import cmt.{toConsoleGreen, toConsoleYellow}
import sbt.io.IO as sbtio
import sbt.io.syntax.fileToRichFile
import sbt.io.syntax.singleFileFinder

given Executable[RestoreState] with
  extension (cmd: RestoreState)
    def execute(): Either[String, String] = {
      val savedState = cmd.config.studentifiedSavedStatesFolder / s"${cmd.exerciseId.value}.zip"
      if !savedState.exists
      then Left(s"No such saved state: ${cmd.exerciseId.value}")
      else {
        deleteCurrentState(cmd.studentifiedRepo.value)(cmd.config)

        Helpers.withZipFile(cmd.config.studentifiedSavedStatesFolder, cmd.exerciseId.value) { solution =>
          val files = Helpers.fileList(solution / cmd.exerciseId.value)
          sbtio.copyDirectory(
            cmd.config.studentifiedSavedStatesFolder / cmd.exerciseId.value,
            cmd.config.activeExerciseFolder,
            preserveLastModified = true)

          Helpers.writeStudentifiedCMTBookmark(cmd.config.bookmarkFile, cmd.exerciseId.value)
          Right(toConsoleGreen(s"Restored state for ${cmd.exerciseId.value}"))
        }
      }
    }
