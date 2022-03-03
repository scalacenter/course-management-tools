package cmt.admin

import cmt.admin.cli.{AdminCliParser, CliOptions}
import cmt.admin.command.AdminCommand.*
import cmt.admin.command.execution.given
import cmt.version.BuildInfo
import cmt.{printErrorAndExit, printMessage, toConsoleRed}

object Main:

  def main(args: Array[String]): Unit =
    AdminCliParser.parse(args) match
      case Right(options) => selectAndExecuteCommand(options).printResult()
      case Left(error)    => printErrorAndExit(s"Error(s): ${error.toErrorString()}")

  private def selectAndExecuteCommand(options: CliOptions): Either[String, String] =
    options.toCommand match
      case cmd: Studentify            => cmd.execute()
      case cmd: RenumberExercises     => cmd.execute()
      case cmd: DuplicateInsertBefore => cmd.execute()
      case cmd: Linearize             => cmd.execute()
      case cmd: Delinearize           => cmd.execute()
      case Version                    => Right(BuildInfo.toString)
      case NoCommand                  => Left("KABOOM!!")

  extension (result: Either[String, String])
    def printResult(): Unit =
      result match
        case Left(errorMessage) =>
          System.err.println(toConsoleRed(s"Error: $errorMessage"))
          System.exit(1)
        case Right(message) =>
          printMessage(message)
