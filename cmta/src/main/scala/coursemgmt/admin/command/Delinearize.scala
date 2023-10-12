package coursemgmt.admin.command

import caseapp.{AppName, CommandName, ExtraName, HelpMessage, Recurse, RemainingArgs, ValueDescription}
import coursemgmt.*
import coursemgmt.Helpers.*
import coursemgmt.ProcessDSL.toProcessCmd
import coursemgmt.admin.Domain.LinearizeBaseDirectory
import coursemgmt.admin.cli.SharedOptions
import coursemgmt.core.execution.Executable
import coursemgmt.core.validation.Validatable
import sbt.io.IO as sbtio
import sbt.io.syntax.*
import coursemgmt.admin.cli.ArgParsers.linearizeBaseDirectoryArgParser
import coursemgmt.core.cli.CmtCommand

object Delinearize:

  @AppName("delinearize")
  @CommandName("delinearize")
  @HelpMessage("'Delinearizes' an existing master repository")
  final case class Options(
      @ExtraName("d")
      @ValueDescription("Folder which contains a 'linearized' repo of the main repo")
      linearizeBaseDirectory: LinearizeBaseDirectory,
      @Recurse shared: SharedOptions)

  given Validatable[Delinearize.Options] with
    extension (options: Delinearize.Options)
      def validated(): Either[CmtError, Delinearize.Options] =
        Right(options)
  end given

  case class ExerciseNameAndSHA(exName: String, exSHA: String)

  given Executable[Delinearize.Options] with
    extension (options: Delinearize.Options)
      def execute(): Either[CmtError, String] = {
        import DelinearizeHelpers.*

        val mainRepository = options.shared.mainRepository
        val config = new CMTaConfig(mainRepository.value, options.shared.maybeConfigFile.map(_.value))

        for {
          _ <- Right(()).withLeft[CmtError]

          mainRepoName = mainRepository.value.getName
          linearizedRootFolder = options.linearizeBaseDirectory.value / mainRepoName

          _ = println(
            s"De-linearizing ${toConsoleGreen(linearizedRootFolder.getPath)} to ${toConsoleGreen(mainRepository.value.getCanonicalPath)}")

          ExercisesMetadata(_, exercisesInMain, _) <- getExerciseMetadata(mainRepository.value)(config)

          exercisesAndSHAsInLinearized <- getExercisesAndSHAs(linearizedRootFolder)

          _ <- checkReposMatch(exercisesInMain, exercisesAndSHAsInLinearized)

          _ <- putBackToMain(mainRepository.value, linearizedRootFolder, exercisesAndSHAsInLinearized)(config)

          successMessage <- Right(s"\nSuccessfully delinearised ${options.linearizeBaseDirectory.value.getPath}")
        } yield successMessage
      }

  private object DelinearizeHelpers:
    def getExercisesAndSHAs(linearizedRootFolder: File): Either[CmtError, Vector[ExerciseNameAndSHA]] =
      "git log --oneline".toProcessCmd(linearizedRootFolder).runAndReadOutput().map(processGitLogOutput)
    end getExercisesAndSHAs

    def processGitLogOutput(gitLogOutput: String): Vector[ExerciseNameAndSHA] =
      gitLogOutput.split("""\n""").toVector.map(splitSHAandExName).map(convertToExNameAndSHA).reverse
    end processGitLogOutput

    def convertToExNameAndSHA(v: Vector[String]): ExerciseNameAndSHA =
      v match
        case sha +: name +: _ => ExerciseNameAndSHA(name, sha)
        case _                => ???
    end convertToExNameAndSHA

    def splitSHAandExName(shaAndExname: String): Vector[String] =
      shaAndExname.split("""\s+""").toVector

    def checkReposMatch(
        exercisesInMain: Seq[String],
        exercisesAndSHAs: Vector[ExerciseNameAndSHA]): Either[CmtError, Unit] =
      // TODO: in case repos are incompatible, print out the exercise list on both ends (if any)
      if exercisesInMain == exercisesAndSHAs.map(_.exName) then Right(())
      else Left(s"Cannot de-linearize: repositories are incompatible".toExecuteCommandErrorMessage)
    end checkReposMatch

    def putBackToMain(mainRepo: File, linearizedRepo: File, exercisesAndSHAs: Vector[ExerciseNameAndSHA])(
        config: CMTaConfig): Either[CmtError, Unit] =

      val mainRepoActiveExerciseFolder = mainRepo / config.mainRepoExerciseFolder
      val linearizedActiveExerciseFolder =
        linearizedRepo / config.linearizedRepoActiveExerciseFolder

      for {
        _ <- checkoutAndCopy(
          mainRepoActiveExerciseFolder,
          linearizedRepo,
          linearizedActiveExerciseFolder,
          exercisesAndSHAs)
        result <- s"git checkout main"
          .toProcessCmd(linearizedRepo)
          .runWithStatus(toConsoleRed(s"Unable to checkout main in linearized repo"))
      } yield result
    end putBackToMain

    @scala.annotation.tailrec
    def checkoutAndCopy(
        mainRepoActiveExerciseFolder: File,
        linearizedRepo: File,
        linearizedActiveExerciseFolder: File,
        exercisesAndSHAs: Seq[ExerciseNameAndSHA]): Either[CmtError, Unit] =
      exercisesAndSHAs match
        case ExerciseNameAndSHA(exercise, sha) +: remaining =>
          print(".")
          s"git checkout $sha"
            .toProcessCmd(linearizedRepo)
            .runWithStatus(toConsoleRed(s"Unable to checkout commit($sha) corresponding to exercise: $exercise")) match
            case l @ Left(_) => l
            case r @ Right(_) =>
              sbtio.delete(mainRepoActiveExerciseFolder / exercise)
              sbtio.copyDirectory(
                linearizedActiveExerciseFolder,
                mainRepoActiveExerciseFolder / exercise,
                preserveLastModified = true)
              checkoutAndCopy(mainRepoActiveExerciseFolder, linearizedRepo, linearizedActiveExerciseFolder, remaining)
        case Nil => Right(())
    end checkoutAndCopy
  end DelinearizeHelpers

  val command = new CmtCommand[Delinearize.Options] {
    def run(options: Delinearize.Options, args: RemainingArgs): Unit =
      options.validated().flatMap(_.execute()).printResult()
  }

end Delinearize
