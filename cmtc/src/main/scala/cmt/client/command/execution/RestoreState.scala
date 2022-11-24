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

import cmt.client.command.ClientCommand.RestoreState
import cmt.core.execution.Executable
import cmt.{Helpers, toConsoleGreen, toConsoleYellow}
import sbt.io.IO as sbtio
import sbt.io.syntax.{fileToRichFile, singleFileFinder}

given Executable[RestoreState] with
  extension (cmd: RestoreState)
    def execute(): Either[String, String] = {
      val savedState = cmd.config.studentifiedSavedStatesFolder / s"${cmd.exerciseId.value}.zip"
      if !savedState.exists
      then Left(s"No such saved state: ${cmd.exerciseId.value}")
      else {
        deleteCurrentState(cmd.studentifiedRepo.value)(cmd.config)

        Helpers.withZipFile(cmd.config.studentifiedSavedStatesFolder, cmd.exerciseId.value) { solution =>
          val files = Helpers.fileList(solution / cmd.exerciseId.value)
          sbtio.copyDirectory(
            cmd.config.studentifiedSavedStatesFolder / cmd.exerciseId.value,
            cmd.config.activeExerciseFolder,
            preserveLastModified = true)

          Helpers.writeStudentifiedCMTBookmark(cmd.config.bookmarkFile, cmd.exerciseId.value)
          Right(toConsoleGreen(s"Restored state for ${cmd.exerciseId.value}"))
        }
      }
    }
