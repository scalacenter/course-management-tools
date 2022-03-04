package cmt.client

import cmt.client.cli.{CliOptions, ClientCliParser}
import cmt.printMessage
import cmt.printErrorAndExit
import cmt.toConsoleRed
import cmt.client.command.ClientCommand.*
import cmt.client.command.execution.given
import cmt.version.BuildInfo

object Main:

  def main(args: Array[String]): Unit =
    ClientCliParser.parse(args) match
      case Right(options) => selectAndExecuteCommand(options)
      case Left(error)    => printErrorAndExit(s"Error(s): ${error.toErrorString()}")

  private def selectAndExecuteCommand(options: CliOptions): Either[String, String] =
    options.toCommand match
      case cmd: GotoFirstExercise => cmd.execute()
      case cmd: ListExercises     => cmd.execute()
      case cmd: ListSavedStates   => cmd.execute()
      case cmd: NextExercise      => cmd.execute()
      case cmd: PreviousExercise  => cmd.execute()
      case cmd: PullSolution      => cmd.execute()
      case cmd: SaveState         => cmd.execute()
      case cmd: GotoExercise      => cmd.execute()
      case cmd: PullTemplate      => cmd.execute()
      case cmd: RestoreState      => cmd.execute()
      case Version                => Right(BuildInfo.toString)
      case NoCommand              => Left("KABOOM!!")

  extension (result: Either[String, String])
    def printResult(): Unit =
      result match
        case Left(errorMessage) =>
          System.err.println(toConsoleRed(s"Error: $errorMessage"))
          System.exit(1)
        case Right(message) =>
          printMessage(message)
