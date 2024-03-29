package coursemgmt.client.command

import caseapp.{AppName, CommandName, ExtraName, HelpMessage, RemainingArgs}
import coursemgmt.{CMTcConfig, CmtError, printResult, toConsoleGreen, toConsoleYellow, toExecuteCommandErrorMessage}
import coursemgmt.Helpers.withZipFile
import coursemgmt.client.Configuration
import coursemgmt.Domain.StudentifiedRepo
import coursemgmt.client.Domain.TemplatePath
import coursemgmt.core.validation.Validatable
import sbt.io.CopyOptions
import sbt.io.IO as sbtio
import sbt.io.syntax.*
import coursemgmt.client.cli.ArgParsers.templatePathArgParser
import coursemgmt.core.cli.ArgParsers.studentifiedRepoArgParser
import coursemgmt.client.cli.CmtcCommand
import coursemgmt.core.cli.enforceTrailingArgumentCount
import coursemgmt.core.command.Package.getCurrentExerciseId

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
