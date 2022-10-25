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

import cmt.Helpers.{deleteFileIfExists, fileSize, fileSha256Hex, withZipFile, writeStudentifiedCMTBookmark}
import cmt.client.command.ClientCommand.NextExercise
import cmt.core.execution.Executable
import cmt.{toConsoleGreen, toConsoleYellow}
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

//      cmd.config.readmeFilesMetaData.foreach { case (k, v) => println(s"$k ->\n${v.mkString("    ", "\n    ", "\n")}")}

      currentExerciseId match {
        case LastExerciseId => Left(toConsoleGreen(s"You're already at the last exercise: $currentExerciseId"))
        case _ =>
          val activeExerciseFolder = cMTcConfig.activeExerciseFolder
          val nextExercise = cMTcConfig.nextExercise(currentExerciseId)
          val currentReadmeFiles = cMTcConfig.readmeFilesMetaData(currentExerciseId).keys.to(Set)
          val nextReadmeFiles = cMTcConfig.readmeFilesMetaData(nextExercise).keys.to(Set)
          val currentTestCodeFiles = cMTcConfig.testCodeMetaData(currentExerciseId).keys.to(Set)
          val nextTestCodeFiles = cMTcConfig.testCodeMetaData(nextExercise).keys.to(Set)

          val readmefilesToBeDeleted = currentReadmeFiles &~ nextReadmeFiles
          val readmeFilesToBeCopied = nextReadmeFiles &~ readmefilesToBeDeleted
          val testCodeFilesToBeDeleted = currentTestCodeFiles &~ nextTestCodeFiles
          val testCodeFilesToBeCopied = nextTestCodeFiles &~ testCodeFilesToBeDeleted

          val (existingTestCodeFiles, deletedTestCodeFiles) =
            currentTestCodeFiles.partition(file => (activeExerciseFolder / file).exists())

          val modifiedTestCodeFiles = existingTestCodeFiles.foldLeft(List.empty[String]) { case (acc, file) =>
            if (
              (activeExerciseFolder / file).exists() &&
              (fileSize(activeExerciseFolder / file) !=
                cMTcConfig.testCodeMetaData(currentExerciseId)(file).size ||
                fileSha256Hex(activeExerciseFolder / file) !=
                cMTcConfig.testCodeMetaData(currentExerciseId)(file).sha256)
            )
              file +: acc
            else
              acc
          }

          if (!modifiedTestCodeFiles.isEmpty)
            Left(s"""next-exercise cancelled.
                 |You have modified the following file(s):
                 |${modifiedTestCodeFiles.mkString("\n   ", "\n   ", "\n")}

                    |""".stripMargin)
          else
            withZipFile(cmd.config.solutionsFolder, nextExercise) { solution =>
              for {
                file <- readmefilesToBeDeleted
              } deleteFileIfExists(activeExerciseFolder / file)
              for {
                file <- readmeFilesToBeCopied
              } sbtio.copyFile(solution / file, activeExerciseFolder / file)
              for {
                file <- testCodeFilesToBeDeleted
              } deleteFileIfExists(activeExerciseFolder / file)
              for {
                file <- testCodeFilesToBeCopied
              } sbtio.copyFile(solution / file, activeExerciseFolder / file)

              writeStudentifiedCMTBookmark(cMTcConfig.bookmarkFile, nextExercise)

              Right(s"${toConsoleGreen("Moved to ")} " + "" + s"${toConsoleYellow(
                  s"${cmd.config.nextExercise(currentExerciseId)}")}")
            }
      }
    }
