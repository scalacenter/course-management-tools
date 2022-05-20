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

import cmt.Helpers
import cmt.Helpers.withZipFile
import cmt.client.command.ClientCommand.GotoExercise
import cmt.core.execution.Executable
import cmt.{toConsoleGreen, toConsoleYellow}

given Executable[GotoExercise] with
  extension (cmd: GotoExercise)
    def execute(): Either[String, String] = {
      if !cmd.studentifiedRepo.exercises.contains(cmd.exerciseId.value)
      then Left(s"No such exercise: ${cmd.exerciseId.value}")
      else
        withZipFile(cmd.studentifiedRepo.solutionsFolder, cmd.exerciseId.value) { solution =>
          copyTestCodeAndReadMeFiles(cmd.studentifiedRepo, solution, cmd.exerciseId.value)
          Right(s"${toConsoleGreen("Moved to ")} " + "" + s"${toConsoleYellow(s"${cmd.exerciseId.value}")}")
        }
    }
