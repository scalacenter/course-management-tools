package cmt.client.command.execution

import cmt.Helpers.withZipFile
import cmt.client.command.ClientCommand.NextExercise
import cmt.core.execution.Executable
import cmt.{toConsoleGreen, toConsoleYellow}
import sbt.io.IO as sbtio

import java.nio.charset.StandardCharsets

given Executable[NextExercise] with
  extension (cmd: NextExercise)
    def execute(): Either[String, String] = {
      val currentExercise =
        sbtio.readLines(cmd.config.bookmarkFile, StandardCharsets.UTF_8).head

      if currentExercise == cmd.config.exercises.last
      then Left(toConsoleGreen(s"You're already at the last exercise: $currentExercise"))
      else
        withZipFile(cmd.config.solutionsFolder, cmd.config.nextExercise(currentExercise)) { solution =>
          copyTestCodeAndReadMeFiles(solution, cmd.config.nextExercise(currentExercise))(cmd.config)
          Right(
            s"${toConsoleGreen("Moved to ")} " + "" + s"${toConsoleYellow(s"${cmd.config.nextExercise(currentExercise)}")}")
        }
    }
