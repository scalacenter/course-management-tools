package cmt.admin.command.execution

import cmt.*
import cmt.Helpers.*
import cmt.ProcessDSL.toProcessCmd
import cmt.admin.command.AdminCommand.Delinearize
import cmt.core.execution.Executable
import sbt.io.IO as sbtio
import sbt.io.syntax.*

case class ExerciseNameAndSHA(exName: String, exSHA: String)

given Executable[Delinearize] with
  extension (cmd: Delinearize)
    def execute(): Either[String, String] = {
      import DelinearizeHelpers.*

      for {
        _ <- Right(()).withLeft[String]
        _ = println(s"De-linearizing ${toConsoleGreen(cmd.mainRepository.value.getPath)} to ${toConsoleGreen(
            cmd.linearizeBaseDirectory.value.getPath)}")

        mainRepoName = cmd.mainRepository.value.getName

        ExercisesMetadata(_, exercisesInMain, _) <- getExerciseMetadata(cmd.mainRepository.value)(cmd.config)

        linearizedRootFolder = cmd.linearizeBaseDirectory.value / mainRepoName

        exercisesAndSHAsInLinearized <- getExercisesAndSHAs(linearizedRootFolder)

        _ <- checkReposMatch(exercisesInMain, exercisesAndSHAsInLinearized)

        _ <- putBackToMain(cmd.mainRepository.value, linearizedRootFolder, exercisesAndSHAsInLinearized)(cmd.config)

        successMessage <- Right(s"Successfully delinearised ${cmd.linearizeBaseDirectory.value.getPath}")
      } yield successMessage
    }

private object DelinearizeHelpers:
  def getExercisesAndSHAs(linearizedRootFolder: File): Either[String, Vector[ExerciseNameAndSHA]] =
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
      exercisesAndSHAs: Vector[ExerciseNameAndSHA]): Either[String, Unit] =
    // TODO: in case repos are incompatible, print out the exercise list on both ends (if any)
    if exercisesInMain == exercisesAndSHAs.map(_.exName) then Right(())
    else Left(s"Cannot de-linearize: repositories are incompatible")
  end checkReposMatch

  def putBackToMain(mainRepo: File, linearizedRepo: File, exercisesAndSHAs: Vector[ExerciseNameAndSHA])(
      config: CMTaConfig): Either[String, Unit] =

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
      exercisesAndSHAs: Seq[ExerciseNameAndSHA]): Either[String, Unit] =
    exercisesAndSHAs match
      case ExerciseNameAndSHA(exercise, sha) +: remaining =>
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
