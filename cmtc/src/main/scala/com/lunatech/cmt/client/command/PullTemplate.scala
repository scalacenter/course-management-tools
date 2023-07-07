package com.lunatech.cmt.client.command

import caseapp.{AppName, CommandName, ExtraName, HelpMessage, RemainingArgs}
import com.lunatech.cmt.{
  CMTcConfig,
  CmtError,
  printResult,
  toConsoleGreen,
  toConsoleYellow,
  toExecuteCommandErrorMessage
}
import com.lunatech.cmt.Helpers.withZipFile
import com.lunatech.cmt.client.Configuration
import com.lunatech.cmt.Domain.StudentifiedRepo
import com.lunatech.cmt.client.Domain.TemplatePath
import com.lunatech.cmt.client.command.getCurrentExerciseId
import com.lunatech.cmt.core.validation.Validatable
import sbt.io.CopyOptions
import sbt.io.IO as sbtio
import sbt.io.syntax.*
import com.lunatech.cmt.client.cli.ArgParsers.{templatePathArgParser, studentifiedRepoArgParser}
import com.lunatech.cmt.client.cli.CmtcCommand
import com.lunatech.cmt.core.cli.enforceTrailingArgumentCount

object PullTemplate:

  @AppName("pull-template")
  @CommandName("pull-template")
  @HelpMessage("Selectively pull in a given file or folder for the active exercise")
  final case class Options(
      @ExtraName("t")
      template: Option[TemplatePath] = None,
      @ExtraName("s")
      studentifiedRepo: Option[StudentifiedRepo] = None)

  given Validatable[PullTemplate.Options] with
    extension (options: PullTemplate.Options)
      def validated(): Either[CmtError, PullTemplate.Options] =
        Right(options)
      end validated
  end given

  given Executable[PullTemplate.Options] with
    extension (options: PullTemplate.Options)
      def execute(configuration: Configuration): Either[CmtError, String] = {
        val config = new CMTcConfig(options.studentifiedRepo.getOrElse(configuration.currentCourse.value).value)
        val currentExerciseId = getCurrentExerciseId(config.bookmarkFile)

        options.template
          .map { template =>
            withZipFile(config.solutionsFolder, currentExerciseId) { solution =>
              val fullTemplatePath = solution / template.value
              (fullTemplatePath.exists, fullTemplatePath.isDirectory) match
                case (false, _) =>
                  Left(s"No such template: ${template.value}".toExecuteCommandErrorMessage)
                case (true, false) =>
                  sbtio.copyFile(
                    fullTemplatePath,
                    config.activeExerciseFolder / template.value,
                    CopyOptions(overwrite = true, preserveLastModified = true, preserveExecutable = true))
                  Right(toConsoleGreen(s"Pulled template file: ") + toConsoleYellow(template.value))
                case (true, true) =>
                  sbtio.copyDirectory(
                    fullTemplatePath,
                    config.activeExerciseFolder / template.value,
                    CopyOptions(overwrite = true, preserveLastModified = true, preserveExecutable = true))
                  Right(toConsoleGreen(s"Pulled template folder: ") + toConsoleYellow(template.value))
            }
          }
          .getOrElse(Left("No template name supplied".toExecuteCommandErrorMessage))
      }

  val command = new CmtcCommand[PullTemplate.Options] {

    def run(options: PullTemplate.Options, args: RemainingArgs): Unit =
      args
        .enforceTrailingArgumentCount(expectedCount = 1)
        .flatMap(
          _.remaining.headOption
            .map(template => options.copy(template = Some(TemplatePath(template))))
            .getOrElse(options)
            .validated()
            .flatMap(_.execute(configuration)))
        .printResult()
  }

end PullTemplate
