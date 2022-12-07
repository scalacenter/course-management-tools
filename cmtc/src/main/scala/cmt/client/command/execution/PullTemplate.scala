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
import cmt.client.command.ClientCommand.PullTemplate
import cmt.core.execution.Executable
import cmt.{
  CmtError,
  FailedToExecuteCommand,
  toConsoleGreen,
  toConsoleYellow,
  ErrorMessage,
  toExecuteCommandErrorMessage
}
import sbt.io.syntax.{fileToRichFile, singleFileFinder}
import sbt.io.{CopyOptions, IO as sbtio}

import java.nio.charset.StandardCharsets

given Executable[PullTemplate] with
  extension (cmd: PullTemplate)
    def execute(): Either[CmtError, String] = {
      val currentExerciseId = getCurrentExerciseId(cmd.config.bookmarkFile)

      withZipFile(cmd.config.solutionsFolder, currentExerciseId) { solution =>
        val fullTemplatePath = solution / cmd.templatePath.value
        (fullTemplatePath.exists, fullTemplatePath.isDirectory) match
          case (false, _) =>
            Left(s"No such template: ${cmd.templatePath.value}".toExecuteCommandErrorMessage)
          case (true, false) =>
            sbtio.copyFile(
              fullTemplatePath,
              cmd.config.activeExerciseFolder / cmd.templatePath.value,
              CopyOptions(overwrite = true, preserveLastModified = true, preserveExecutable = true))
            Right(toConsoleGreen(s"Pulled template file: ") + toConsoleYellow(cmd.templatePath.value))
          case (true, true) =>
            sbtio.copyDirectory(
              fullTemplatePath,
              cmd.config.activeExerciseFolder / cmd.templatePath.value,
              CopyOptions(overwrite = true, preserveLastModified = true, preserveExecutable = true))
            Right(toConsoleGreen(s"Pulled template folder: ") + toConsoleYellow(cmd.templatePath.value))
      }
    }
