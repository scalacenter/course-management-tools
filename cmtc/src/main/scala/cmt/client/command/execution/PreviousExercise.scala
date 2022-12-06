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
import cmt.client.command.ClientCommand.PreviousExercise
import cmt.core.execution.Executable
import cmt.{CmtError, FailedToExecuteCommand, toConsoleGreen, toConsoleYellow, ErrorMessage}
import sbt.io.IO as sbtio
import sbt.io.syntax.*

import java.nio.charset.StandardCharsets

given Executable[PreviousExercise] with
  extension (cmd: PreviousExercise)
    def execute(): Either[CmtError, String] = {
      import cmt.client.Domain.ForceMoveToExercise
      val cMTcConfig = cmd.config
      val currentExerciseId = getCurrentExerciseId(cMTcConfig.bookmarkFile)
      val FirstExerciseId = cmd.config.exercises.head

      val activeExerciseFolder = cMTcConfig.activeExerciseFolder
      val toExerciseId = cMTcConfig.previousExercise(currentExerciseId)

      val (currentTestCodeFiles, filesToBeDeleted, filesToBeCopied) =
        getFilesToCopyAndDelete(currentExerciseId, toExerciseId, cMTcConfig)

      (currentExerciseId, cmd.forceMoveToExercise) match {
        case (FirstExerciseId, _) =>
          Left(FailedToExecuteCommand(ErrorMessage(toConsoleGreen(s"You're already at the first exercise: $currentExerciseId"))))

        case (_, ForceMoveToExercise(true)) =>
          pullTestCode(toExerciseId, activeExerciseFolder, filesToBeDeleted, filesToBeCopied, cMTcConfig)

        case _ =>
          val existingTestCodeFiles =
            currentTestCodeFiles.filter(file => (activeExerciseFolder / file).exists())

          val modifiedTestCodeFiles = existingTestCodeFiles.filter(
            exerciseFileHasBeenModified(activeExerciseFolder, currentExerciseId, _, cMTcConfig))

          if (modifiedTestCodeFiles.nonEmpty)
            Left(FailedToExecuteCommand(ErrorMessage(s"""previous-exercise cancelled.
                 |
                 |${toConsoleYellow("You have modified the following file(s):")}
                 |${toConsoleGreen(modifiedTestCodeFiles.mkString("\n   ", "\n   ", "\n"))}
                 |""".stripMargin)))
          else
            pullTestCode(toExerciseId, activeExerciseFolder, filesToBeDeleted, filesToBeCopied, cMTcConfig)
      }
    }
