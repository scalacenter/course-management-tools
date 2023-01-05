package cmt.client.command

import caseapp.{AppName, CommandName, HelpMessage, Recurse, RemainingArgs}
import cmt.{CMTcConfig, CmtError, printResult, toConsoleGreen, toConsoleYellow, toExecuteCommandErrorMessage}
import cmt.Helpers.withZipFile
import cmt.client.Domain.TemplatePath
import cmt.client.cli.SharedOptions
import cmt.client.command.getCurrentExerciseId
import cmt.core.CmtCommand
import cmt.core.execution.Executable
import cmt.core.validation.Validatable
import sbt.io.CopyOptions
import sbt.io.IO as sbtio
import sbt.io.syntax.*
import cmt.client.cli.ArgParsers.templatePathArgParser

object PullTemplate:

  @AppName("pull-template")
  @CommandName("pull-template")
  @HelpMessage("Selectively pull in a given file or folder for the active exercise")
  final case class Options(template: TemplatePath, @Recurse shared: SharedOptions)

  given Validatable[PullTemplate.Options] with
    extension (options: PullTemplate.Options)
      def validated(): Either[CmtError, PullTemplate.Options] =
        Right(options)
      end validated
  end given

  given Executable[PullTemplate.Options] with
    extension (cmd: PullTemplate.Options)
      def execute(): Either[CmtError, String] = {
        val config = new CMTcConfig(cmd.shared.studentifiedRepo.value)
        val currentExerciseId = getCurrentExerciseId(config.bookmarkFile)

        withZipFile(config.solutionsFolder, currentExerciseId) { solution =>
          val fullTemplatePath = solution / cmd.template.value
          (fullTemplatePath.exists, fullTemplatePath.isDirectory) match
            case (false, _) =>
              Left(s"No such template: ${cmd.template.value}".toExecuteCommandErrorMessage)
            case (true, false) =>
              sbtio.copyFile(
                fullTemplatePath,
                config.activeExerciseFolder / cmd.template.value,
                CopyOptions(overwrite = true, preserveLastModified = true, preserveExecutable = true))
              Right(toConsoleGreen(s"Pulled template file: ") + toConsoleYellow(cmd.template.value))
            case (true, true) =>
              sbtio.copyDirectory(
                fullTemplatePath,
                config.activeExerciseFolder / cmd.template.value,
                CopyOptions(overwrite = true, preserveLastModified = true, preserveExecutable = true))
              Right(toConsoleGreen(s"Pulled template folder: ") + toConsoleYellow(cmd.template.value))
        }
      }

  val command = new CmtCommand[PullTemplate.Options] {

    def run(options: PullTemplate.Options, args: RemainingArgs): Unit = {
      enforceNoTrailingArguments(args)
      options.validated().flatMap(_.execute()).printResult()
    }
  }

end PullTemplate
