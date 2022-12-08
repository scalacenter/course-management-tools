package cmt.admin

/** Copyright 2022 - Eric Loots - eric.loots@gmail.com / Trevor Burton-McCreadie - trevor@thinkmorestupidless.com
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *
  * See the License for the specific language governing permissions and limitations under the License.
  */

import cmt.admin.cli.{AdminCliParser, CliOptions}
import cmt.admin.command.AdminCommand.*
import cmt.admin.command.execution.given
import cmt.version.BuildInfo
import cmt.{
  CmtError,
  printErrorAndExit,
  printMessage,
  toConsoleRed,
  FailedToExecuteCommand,
  ErrorMessage,
  toExecuteCommandErrorMessage
}

object Main:

  def main(args: Array[String]): Unit =
    AdminCliParser.parse(args) match
      case Right(options) => selectAndExecuteCommand(options).printResult()
      case Left(error)    => printErrorAndExit(s"Error(s): ${error.toErrorString()}")

  private def selectAndExecuteCommand(options: CliOptions): Either[CmtError, String] =
    options.toCommand match
      case cmd: Studentify            => cmd.execute()
      case cmd: RenumberExercises     => cmd.execute()
      case cmd: DuplicateInsertBefore => cmd.execute()
      case cmd: Linearize             => cmd.execute()
      case cmd: Delinearize           => cmd.execute()
      case Version                    => Right(BuildInfo.toString)
      case NoCommand                  => Left("KABOOM!!".toExecuteCommandErrorMessage)

  extension (result: Either[CmtError, String])
    def printResult(): Unit =
      result match
        case Left(errorMessage) =>
          System.err.println(toConsoleRed(s"Error: ${errorMessage.toDisplayString}"))
          System.exit(1)
        case Right(message) =>
          printMessage(message)
