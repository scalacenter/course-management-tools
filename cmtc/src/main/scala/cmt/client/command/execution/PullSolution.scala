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
import cmt.Helpers.fileList
import cmt.client.command.ClientCommand.PullSolution
import cmt.core.execution.Executable

import java.nio.charset.StandardCharsets
import cmt.{toConsoleGreen, toConsoleYellow}
import sbt.io.IO as sbtio
import sbt.io.syntax.fileToRichFile
import sbt.io.syntax.singleFileFinder

given Executable[PullSolution] with
  extension (cmd: PullSolution)
    def execute(): Either[String, String] = {
      val currentExercise =
        sbtio.readLines(cmd.config.bookmarkFile, StandardCharsets.UTF_8).head

      deleteCurrentState(cmd.studentifiedRepo.value)(cmd.config)

      withZipFile(cmd.config.solutionsFolder, currentExercise) { solution =>
        val files = fileList(solution / currentExercise)
        sbtio.copyDirectory(
          cmd.config.solutionsFolder / currentExercise,
          cmd.config.activeExerciseFolder,
          preserveLastModified = true)
        Right(toConsoleGreen(s"Pulled solution for $currentExercise"))
      }
    }
