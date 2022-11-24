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

import cmt.Helpers.{exerciseFileHasBeenModified, getFilesToCopyAndDelete, pullTestCode, withZipFile}
import cmt.client.command.ClientCommand.GotoExercise
import cmt.core.execution.Executable
import cmt.{Helpers, toConsoleGreen, toConsoleYellow}
import sbt.io.IO as sbtio
import sbt.io.syntax.*

given Executable[GotoExercise] with
  extension (cmd: GotoExercise)
    def execute(): Either[String, String] = {
      import cmt.client.Domain.ForceMoveToExercise
      val cMTcConfig = cmd.config
      val currentExerciseId = getCurrentExerciseId(cMTcConfig.bookmarkFile)

      val activeExerciseFolder = cMTcConfig.activeExerciseFolder
      val toExerciseId = cmd.exerciseId.value

      if (!cmd.config.exercises.contains(toExerciseId))
        Left(toConsoleGreen(s"No such exercise: ${cmd.exerciseId.value}"))
      else
        val (currentTestCodeFiles, filesToBeDeleted, filesToBeCopied) =
          getFilesToCopyAndDelete(currentExerciseId, toExerciseId, cMTcConfig)

        (cmd.forceMoveToExercise, currentExerciseId) match {
          case (_, `toExerciseId`) =>
            Left(toConsoleGreen(s"You're already at exercise ${toExerciseId}"))

          case (ForceMoveToExercise(true), _) =>
            pullTestCode(toExerciseId, activeExerciseFolder, filesToBeDeleted, filesToBeCopied, cMTcConfig)

          case _ =>
            val existingTestCodeFiles =
              currentTestCodeFiles.filter(file => (activeExerciseFolder / file).exists())

            val modifiedTestCodeFiles = existingTestCodeFiles.filter(
              exerciseFileHasBeenModified(activeExerciseFolder, currentExerciseId, _, cMTcConfig))

            if (modifiedTestCodeFiles.nonEmpty)
              Left(s"""goto-exercise cancelled.
                   |
                   |${toConsoleYellow("You have modified the following file(s):")}
                   |${toConsoleGreen(modifiedTestCodeFiles.mkString("\n   ", "\n   ", "\n"))}
                   |""".stripMargin)
            else
              pullTestCode(toExerciseId, activeExerciseFolder, filesToBeDeleted, filesToBeCopied, cMTcConfig)
        }
    }
