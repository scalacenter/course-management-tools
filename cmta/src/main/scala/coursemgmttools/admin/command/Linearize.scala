package coursemgmttools.admin.command

import caseapp.{AppName, CommandName, ExtraName, HelpMessage, Recurse, RemainingArgs, ValueDescription}
import coursemgmttools.Helpers.*
import coursemgmttools.{CMTaConfig, CmtError, printResult, toConsoleGreen}
import coursemgmttools.admin.Domain.{ForceDeleteDestinationDirectory, LinearizeBaseDirectory, MainRepository}
import coursemgmttools.admin.cli.ArgParsers.{forceDeleteDestinationDirectoryArgParser, linearizeBaseDirectoryArgParser}
import coursemgmttools.admin.cli.SharedOptions
import coursemgmttools.admin.validateDestinationFolder
import coursemgmttools.core.cli.CmtCommand
import coursemgmttools.core.execution.Executable
import coursemgmttools.core.validation.Validatable
import sbt.io.IO as sbtio
import sbt.io.syntax.*

object Linearize:

  @AppName("linearize")
  @CommandName("linearize")
  @HelpMessage(
    "'Linearizes' a 'main' repository in the target directory where the linearized repo has one commit per exercise")
  final case class Options(
      @ExtraName("d")
      @ValueDescription("Folder in which the 'linearized' repo will be created")
      linearizeBaseDirectory: LinearizeBaseDirectory,
      @ExtraName("f")
      @ValueDescription("Force-delete a pre-existing 'linearized' repo")
      forceDelete: ForceDeleteDestinationDirectory = ForceDeleteDestinationDirectory(false),
      @Recurse shared: SharedOptions)

  given Validatable[Linearize.Options] with
    extension (options: Linearize.Options)
      def validated(): Either[CmtError, Linearize.Options] =
        for {
          mainRepository <- resolveMainRepoPath(options.shared.mainRepository.value)
          _ <- validateDestinationFolder(
            mainRepository = mainRepository,
            destination = options.linearizeBaseDirectory.value)
        } yield options
  end given

  given Executable[Linearize.Options] with
    extension (options: Linearize.Options)
      def execute(): Either[CmtError, String] = {
        import LinearizeHelpers.*

        resolveMainRepoPath(options.shared.mainRepository.value).flatMap { repository =>
          val mainRepository = MainRepository(repository)
          val config = new CMTaConfig(mainRepository.value, options.shared.maybeConfigFile.map(_.value))

          for {
            _ <- exitIfGitIndexOrWorkspaceIsntClean(mainRepository.value)

            mainRepoName = mainRepository.value.getName
            tmpFolder = sbtio.createTemporaryDirectory
            cleanedMainRepo = tmpFolder / mainRepository.value.getName
            ExercisesMetadata(_, exercises, _) <- getExerciseMetadata(mainRepository.value)(config)
            linearizedRootFolder = options.linearizeBaseDirectory.value / mainRepoName

            _ = {
              println(s"Linearizing ${toConsoleGreen(mainRepoName)} to ${toConsoleGreen(linearizedRootFolder.getPath)}")
            }
            _ <- checkpreExistingAndCreateArtifactRepo(
              options.linearizeBaseDirectory.value,
              linearizedRootFolder,
              options.forceDelete.value)

            _ <- copyCleanViaGit(mainRepository.value, tmpFolder, mainRepoName)

            _ <- initializeGitRepo(linearizedRootFolder)

            _ <- commitExercises(cleanedMainRepo, exercises, linearizedRootFolder, config)

            _ = sbtio.delete(tmpFolder)
            successMessage <- Right(s"\nSuccessfully linearized ${mainRepository.value.getPath}")

          } yield successMessage
        }
      }
  end given

  private object LinearizeHelpers:
    def commitExercises(
        cleanedMainRepo: File,
        exercises: Seq[String],
        linearizedRootFolder: File,
        config: CMTaConfig): Either[CmtError, Unit] =

      val dotIgnoreFile = cleanedMainRepo / ".gitignore"
      if dotIgnoreFile.exists then sbtio.copyFile(dotIgnoreFile, linearizedRootFolder / ".gitignore")

      exercises match
        case exercise +: remainingExercises =>
          print(".")
          val from = cleanedMainRepo / config.mainRepoExerciseFolder / exercise
          val linearizedCodeFolder = linearizedRootFolder / config.linearizedRepoActiveExerciseFolder
          sbtio.delete(linearizedCodeFolder)
          sbtio.createDirectory(linearizedCodeFolder)
          sbtio.copyDirectory(from, linearizedCodeFolder, preserveLastModified = true)
          val commitResult: Either[CmtError, Unit] = commitToGit(exercise, linearizedRootFolder)
          commitResult match
            case Right(_) => commitExercises(cleanedMainRepo, remainingExercises, linearizedRootFolder, config)
            case left     => left
        case Nil => Right(())
    end commitExercises
  end LinearizeHelpers

  val command = new CmtCommand[Linearize.Options] {
    def run(options: Linearize.Options, args: RemainingArgs): Unit =
      options.validated().flatMap(_.execute()).printResult()
  }

end Linearize
