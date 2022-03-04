package cmt.client.command.execution

import cmt.client.command.ClientCommand.ListExercises
import cmt.core.execution.Executable
import cmt.{toConsoleGreen, toConsoleYellow}
import sbt.io.IO as sbtio

import java.nio.charset.StandardCharsets

given Executable[ListExercises] with
  extension (cmd: ListExercises)
    def execute(): Either[String, String] = {
      val currentExercise =
        sbtio.readLines(cmd.config.bookmarkFile, StandardCharsets.UTF_8).head
      val messages = cmd.config.exercises.zipWithIndex
        .map { case (exName, index) =>
          toConsoleGreen(f"${index + 1}%3d. ${starCurrentExercise(currentExercise, exName)}  $exName")
        }
        .mkString("\n")
      Right(messages)
    }
