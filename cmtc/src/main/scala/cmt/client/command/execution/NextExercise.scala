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

import cmt.Helpers.*
import cmt.client.command.ClientCommand.NextExercise
import cmt.core.execution.Executable
import cmt.{CMTcConfig, CmtError, FailedToExecuteCommand, toConsoleGreen, toConsoleYellow, ErrorMessage}
import com.typesafe.config.{Config, ConfigFactory, ConfigObject, ConfigValue}
import sbt.io.IO as sbtio
import sbt.io.syntax.*

import cmt.toExecuteCommandErrorMessage

import java.nio.charset.StandardCharsets
import scala.jdk.CollectionConverters.*

given Executable[NextExercise] with
  extension (cmd: NextExercise)
    def execute(): Either[CmtError, String] = {
      import cmt.client.Domain.ForceMoveToExercise
      val cMTcConfig = cmd.config
      val currentExerciseId = getCurrentExerciseId(cMTcConfig.bookmarkFile)
      val LastExerciseId = cMTcConfig.exercises.last

      val activeExerciseFolder = cMTcConfig.activeExerciseFolder
      val toExerciseId = cMTcConfig.nextExercise(currentExerciseId)

      val (currentTestCodeFiles, filesToBeDeleted, filesToBeCopied) =
        getFilesToCopyAndDelete(currentExerciseId, toExerciseId, cMTcConfig)

      (currentExerciseId, cmd.forceMoveToExercise) match {
        case (LastExerciseId, _) =>
          Left(toConsoleGreen(s"You're already at the last exercise: $currentExerciseId").toExecuteCommandErrorMessage)

        case (_, ForceMoveToExercise(true)) =>
          pullTestCode(toExerciseId, activeExerciseFolder, filesToBeDeleted, filesToBeCopied, cMTcConfig)

        case _ =>
          val existingTestCodeFiles =
            currentTestCodeFiles.filter(file => (activeExerciseFolder / file).exists())

          val modifiedTestCodeFiles = existingTestCodeFiles.filter(
            exerciseFileHasBeenModified(activeExerciseFolder, currentExerciseId, _, cMTcConfig))

          if (modifiedTestCodeFiles.nonEmpty)
            // TODO: need to add a suggested fix when this case triggers:
            // Either:
            //   - overwrite modifications by repeating the command and using the force (-f) option
            //     maybe in combination with cmtc save-state in case the modifications should be
            //     retrieveable later
            //   - rename modified files (and probably change class names as well)
            //
            // This needs to be added to the `previous-exercise`, `goto-exercise`, and `goto-first-exercise`
            // commands too.
            Left(s"""next-exercise cancelled.
                 |
                 |${toConsoleYellow("You have modified the following file(s):")}
                 |${toConsoleGreen(modifiedTestCodeFiles.mkString("\n   ", "\n   ", "\n"))}
                 |""".stripMargin.toExecuteCommandErrorMessage)
          else
            pullTestCode(toExerciseId, activeExerciseFolder, filesToBeDeleted, filesToBeCopied, cMTcConfig)
      }
    }
