package cmt.client.command

import caseapp.{AppName, CommandName, ExtraName, HelpMessage, RemainingArgs}
import cmt.{CMTcConfig, CmtError, printResult, toConsoleGreen}
import cmt.Helpers.{fileList, withZipFile}
import cmt.client.Configuration
import cmt.client.Domain.StudentifiedRepo
import cmt.client.command.{deleteCurrentState, getCurrentExerciseId}
import cmt.client.cli.CmtcCommand
import cmt.client.command.Executable
import cmt.core.validation.Validatable
import sbt.io.IO as sbtio
import sbt.io.syntax.*
import cmt.core.cli.enforceNoTrailingArguments
import cmt.client.cli.ArgParsers.studentifiedRepoArgParser

object PullSolution:

  @AppName("pull-solution")
  @CommandName("pull-solution")
  @HelpMessage("Pull in all code for the active exercise. All local changes are discarded")
  final case class Options(
      @ExtraName("s")
      studentifiedRepo: Option[StudentifiedRepo] = None)

  given Validatable[PullSolution.Options] with
    extension (options: PullSolution.Options)
      def validated(): Either[CmtError, PullSolution.Options] =
        Right(options)
      end validated
  end given

  given Executable[PullSolution.Options] with
    extension (options: PullSolution.Options)
      def execute(configuration: Configuration): Either[CmtError, String] = {
        val studentifiedRepo = options.studentifiedRepo.getOrElse(configuration.currentCourse.value)
        val config = new CMTcConfig(studentifiedRepo.value)
        val currentExerciseId = getCurrentExerciseId(config.bookmarkFile)

        deleteCurrentState(studentifiedRepo.value)(config)

        withZipFile(config.solutionsFolder, currentExerciseId) { solution =>
          val files = fileList(solution / currentExerciseId)
          sbtio.copyDirectory(
            config.solutionsFolder / currentExerciseId,
            config.activeExerciseFolder,
            preserveLastModified = true)
          Right(toConsoleGreen(s"Pulled solution for $currentExerciseId"))
        }
      }

  val command = new CmtcCommand[PullSolution.Options] {

    def run(options: PullSolution.Options, args: RemainingArgs): Unit =
      args
        .enforceNoTrailingArguments()
        .flatMap(_ => options.validated().flatMap(_.execute(configuration)))
        .printResult()
  }

end PullSolution
