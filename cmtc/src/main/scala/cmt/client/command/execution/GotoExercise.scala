package cmt.client.command.execution

import cmt.Helpers
import cmt.Helpers.withZipFile
import cmt.client.command.ClientCommand.GotoExercise
import cmt.core.execution.Executable
import cmt.{toConsoleGreen, toConsoleYellow}

given Executable[GotoExercise] with
  extension (cmd: GotoExercise)
    def execute(): Either[String, String] = {
      if !cmd.config.exercises.contains(cmd.exerciseId.value)
      then Left(s"No such exercise: ${cmd.exerciseId.value}")
      else
        withZipFile(cmd.config.solutionsFolder, cmd.exerciseId.value) { solution =>
          copyTestCodeAndReadMeFiles(solution, cmd.exerciseId.value)(cmd.config)
          Right(s"${toConsoleGreen("Moved to ")} " + "" + s"${toConsoleYellow(s"${cmd.exerciseId.value}")}")
        }
    }
