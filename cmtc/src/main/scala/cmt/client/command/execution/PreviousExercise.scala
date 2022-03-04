package cmt.client.command.execution

import cmt.Helpers.withZipFile
import cmt.client.command.ClientCommand.PreviousExercise
import cmt.core.execution.Executable
import cmt.{toConsoleGreen, toConsoleYellow}
import sbt.io.IO as sbtio

import java.nio.charset.StandardCharsets

given Executable[PreviousExercise] with
  extension (cmd: PreviousExercise)
    def execute(): Either[String, String] = {
      val currentExercise =
        sbtio.readLines(cmd.config.bookmarkFile, StandardCharsets.UTF_8).head

      if currentExercise == cmd.config.exercises.head
      then Left(toConsoleGreen(s"You're already at the first exercise: $currentExercise"))
      else
        withZipFile(cmd.config.solutionsFolder, cmd.config.previousExercise(currentExercise)) { solution =>
          copyTestCodeAndReadMeFiles(solution, cmd.config.previousExercise(currentExercise))(cmd.config)
          Right(s"${toConsoleGreen("Moved to ")} " + "" + s"${toConsoleYellow(
              s"${cmd.config.previousExercise(currentExercise)}")}")
        }
    }
