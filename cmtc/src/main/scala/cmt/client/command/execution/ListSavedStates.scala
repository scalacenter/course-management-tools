package cmt.client.command.execution

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
          .listFiles(cmd.studentifiedRepo.studentifiedSavedStatesFolder)
          .to(List)
          .sorted
          .map(_.getName)
          .map(item => MatchDotzip.replaceAllIn(item, ""))
          .filter(cmd.studentifiedRepo.exercises.contains(_))

      Right(
        toConsoleGreen(s"Saved states available for exercises:\n") + toConsoleYellow(
          s"${savedStates.mkString("\n   ", "\n   ", "\n")}"))
    }
