package cmt.client.command.execution

import cmt.Helpers.withZipFile
import cmt.client.command.ClientCommand.PullTemplate
import cmt.core.execution.Executable
import sbt.io.CopyOptions
import cmt.{toConsoleGreen, toConsoleYellow}
import sbt.io.IO as sbtio
import sbt.io.syntax.fileToRichFile
import sbt.io.syntax.singleFileFinder

import java.nio.charset.StandardCharsets

given Executable[PullTemplate] with
  extension (cmd: PullTemplate)
    def execute(): Either[String, String] = {
      val currentExercise =
        sbtio.readLines(cmd.config.bookmarkFile, StandardCharsets.UTF_8).head

      withZipFile(cmd.config.solutionsFolder, currentExercise) { solution =>
        val fullTemplatePath = solution / cmd.templatePath.value
        (fullTemplatePath.exists, fullTemplatePath.isDirectory) match
          case (false, _) =>
            Left(s"No such template: ${cmd.templatePath.value}")
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
