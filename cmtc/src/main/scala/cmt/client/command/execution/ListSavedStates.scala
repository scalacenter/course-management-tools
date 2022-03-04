package cmt.client.command.execution

import cmt.client.command.ClientCommand.ListSavedStates
import cmt.core.execution.Executable
import cmt.{toConsoleGreen, toConsoleYellow}
import sbt.io.IO as sbtio

given Executable[ListSavedStates] with
  extension (cmd: ListSavedStates)
    def execute(): Either[String, String] = {
      val MatchDotzip = ".zip".r
      val savedStates =
        sbtio
          .listFiles(cmd.config.studentifiedSavedStatesFolder)
          .to(List)
          .sorted
          .map(_.getName)
          .map(item => MatchDotzip.replaceAllIn(item, ""))
          .filter(cmd.config.exercises.contains(_))

      Right(
        toConsoleGreen(s"Saved states available for exercises:\n") + toConsoleYellow(
          s"${savedStates.mkString("\n   ", "\n   ", "\n")}"))
    }
