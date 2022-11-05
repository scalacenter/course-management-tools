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

import cmt.Helpers.{
  deleteFileIfExists,
  exerciseFileHasBeenModified,
  extractExerciseNr,
  fileSha256Hex,
  fileSize,
  pullTestCode,
  withZipFile,
  writeStudentifiedCMTBookmark,
  getFilesToCopyAndDelete
}
import cmt.client.command.ClientCommand.NextExercise
import cmt.core.execution.Executable
import cmt.{CMTcConfig, toConsoleGreen, toConsoleYellow}
import sbt.io.IO as sbtio
import sbt.io.syntax
import sbt.io.syntax.fileToRichFile
import com.typesafe.config.{Config, ConfigFactory, ConfigObject, ConfigValue}

//import java.io.File
import scala.jdk.CollectionConverters.*
import java.nio.charset.StandardCharsets

given Executable[NextExercise] with
  extension (cmd: NextExercise)
    def execute(): Either[String, String] = {
      val cMTcConfig = cmd.config
      val currentExerciseId = getCurrentExerciseId(cMTcConfig.bookmarkFile)
      val LastExerciseId = cMTcConfig.exercises.last

      currentExerciseId match {
        case LastExerciseId => Left(toConsoleGreen(s"You're already at the last exercise: $currentExerciseId"))
        case _ =>
          val activeExerciseFolder = cMTcConfig.activeExerciseFolder
          val toExerciseId = cMTcConfig.nextExercise(currentExerciseId)
          val currentReadmeFiles = cMTcConfig.readmeFilesMetaData(currentExerciseId).keys.to(Set)
          val nextReadmeFiles = cMTcConfig.readmeFilesMetaData(toExerciseId).keys.to(Set)
          val nextTestCodeFiles = cMTcConfig.testCodeMetaData(toExerciseId).keys.to(Set)

          val (
            currentTestCodeFiles,
            readmefilesToBeDeleted,
            readmeFilesToBeCopied,
            testCodeFilesToBeDeleted,
            testCodeFilesToBeCopied) =
            getFilesToCopyAndDelete(currentExerciseId, toExerciseId, cMTcConfig)

          val existingTestCodeFiles =
            currentTestCodeFiles.filter(file => (activeExerciseFolder / file).exists())

          val modifiedTestCodeFiles = existingTestCodeFiles.filter(
            exerciseFileHasBeenModified(activeExerciseFolder, currentExerciseId, _, cMTcConfig))

          if (modifiedTestCodeFiles.nonEmpty)
            Left(s"""next-exercise cancelled.
                 |You have modified the following file(s):
                 |${modifiedTestCodeFiles.mkString("\n   ", "\n   ", "\n")}
                 |""".stripMargin)
          else
            pullTestCode(
              toExerciseId,
              activeExerciseFolder,
              readmefilesToBeDeleted,
              readmeFilesToBeCopied,
              testCodeFilesToBeDeleted,
              testCodeFilesToBeCopied,
              cMTcConfig)
      }
    }
