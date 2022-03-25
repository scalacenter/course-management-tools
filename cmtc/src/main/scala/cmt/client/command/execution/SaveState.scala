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

import cmt.Helpers.zipAndDeleteOriginal
import cmt.client.command.ClientCommand.SaveState
import cmt.core.execution.Executable
import cmt.{Helpers, toConsoleGreen}
import sbt.io.IO as sbtio
import sbt.io.syntax.fileToRichFile
import sbt.io.syntax.singleFileFinder

import java.nio.charset.StandardCharsets

given Executable[SaveState] with
  extension (cmd: SaveState)
    def execute(): Either[String, String] = {
      val currentExercise =
        sbtio.readLines(cmd.config.bookmarkFile, StandardCharsets.UTF_8).head
      val savedStatesFolder = cmd.config.studentifiedSavedStatesFolder
      sbtio.delete(savedStatesFolder / currentExercise)
      val filesInScope = getCurrentExerciseState(cmd.studentifiedRepo.value)(cmd.config)

      for {
        file <- filesInScope
        f <- file.relativeTo(cmd.config.activeExerciseFolder)
        dest = savedStatesFolder / currentExercise / f.getPath
      } {
        sbtio.touch(dest)
        sbtio.copyFile(file, dest)
      }

      zipAndDeleteOriginal(baseFolder = savedStatesFolder, zipToFolder = savedStatesFolder, exercise = currentExercise)

      Right(toConsoleGreen(s"Saved state for $currentExercise"))
    }
