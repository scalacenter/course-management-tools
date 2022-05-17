package cmt.client

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
      case Right(options) => selectAndExecuteCommand(options).printResult()
      case Left(error)    => printErrorAndExit(s"Error(s): ${error.toErrorString()}")

  private def selectAndExecuteCommand(options: CliOptions): Either[String, String] =
    options.toCommand match
      case cmd: Configure         => cmd.execute()
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
