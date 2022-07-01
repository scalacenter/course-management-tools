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

import cmt.Helpers.withZipFile
import cmt.client.command.ClientCommand.PreviousExercise
import cmt.core.execution.Executable
import cmt.{toConsoleGreen, toConsoleYellow}
import sbt.io.IO as sbtio

import java.nio.charset.StandardCharsets

given Executable[PreviousExercise] with
  extension (cmd: PreviousExercise)
    def execute(): Either[String, String] = {
      val currentExerciseId = getCurrentExerciseId(cmd.config.bookmarkFile)
      val FistExerciseId = cmd.config.exercises.head

      currentExerciseId match {
        case FistExerciseId => Left(toConsoleGreen(s"You're already at the first exercise: $currentExerciseId"))
        case _ =>
          withZipFile(cmd.config.solutionsFolder, cmd.config.previousExercise(currentExerciseId)) { solution =>
            copyTestCodeAndReadMeFiles(solution, cmd.config.previousExercise(currentExerciseId))(cmd.config)
            Right(s"${toConsoleGreen("Moved to ")} " + "" + s"${toConsoleYellow(
                s"${cmd.config.previousExercise(currentExerciseId)}")}")
          }
      }
    }
