package coursemgmt.client.command

import caseapp.{AppName, CommandName, ExtraName, HelpMessage, RemainingArgs}
import coursemgmt.{CMTcConfig, CmtError, printResult, toConsoleGreen, toConsoleYellow}
import coursemgmt.Helpers.{adaptToNixSeparatorChar, exerciseFileHasBeenModified, withZipFile}
import coursemgmt.client.Configuration
import coursemgmt.Domain.StudentifiedRepo
import coursemgmt.client.cli.CmtcCommand
import coursemgmt.core.validation.Validatable
import sbt.io.IO as sbtio
import sbt.io.syntax.*
import coursemgmt.core.cli.enforceNoTrailingArguments
import coursemgmt.core.cli.ArgParsers.studentifiedRepoArgParser
import coursemgmt.core.command.Package.getCurrentExerciseId

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

        val ExerciseFiles(_, currentFiles) = getCurrentExerciseStateExceptDontTouch(studentifiedRepo.value)(config)

        val currentFilesSet = currentFiles.map(f => adaptToNixSeparatorChar(f.getPath)).to(Set)
        val solutionFilesSet = config.codeMetaData(currentExerciseId).keys.to(Set)

        val filesToDelete = currentFilesSet &~ solutionFilesSet
        val filesToOverwrite = (currentFilesSet & solutionFilesSet).filter(f =>
          exerciseFileHasBeenModified(config.activeExerciseFolder, f, config.codeMetaData(currentExerciseId)))
        val filesToCopyFromSolution = solutionFilesSet &~ currentFilesSet

        withZipFile(config.solutionsFolder, currentExerciseId) { solution =>
          sbtio.deleteFilesEmptyDirs(filesToDelete.map(f => config.activeExerciseFolder / f))
          for {
            f <- filesToOverwrite ++ filesToCopyFromSolution
          } sbtio.copyFile(solution / f, config.activeExerciseFolder / f)

          Right(toConsoleGreen(s"Pulled solution for ${toConsoleYellow(currentExerciseId)}"))
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
