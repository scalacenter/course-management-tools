package cmt

import sbt.io.IO as sbtio
import sbt.io.syntax.*

import Helpers.{
  exitIfGitIndexOrWorkspaceIsntClean,
  getExercisePrefixAndExercises,
  validatePrefixes,
  ExercisePrefixesAndExerciseNames
}
import ProcessDSL.toProcessCmd

case class ExerciseNameAndSHA(exName: String, exSHA: String)

object CMTDeLinearize:
  def delinearize(mainRepo: File, linBase: File)(config: CMTaConfig): Unit =

    exitIfGitIndexOrWorkspaceIsntClean(mainRepo)

    println(s"De-linearizing ${toConsoleGreen(mainRepo.getPath)} to ${toConsoleGreen(linBase.getPath)}")

    val mainRepoName = mainRepo.getName

    val ExercisePrefixesAndExerciseNames(prefixes, exercisesInMain) =
      getExercisePrefixAndExercises(mainRepo)(config)
    validatePrefixes(prefixes)

    val linearizedRootFolder = linBase / mainRepoName

    val exercisesAndSHAsInLinearized = getExercisesAndSHAs(linearizedRootFolder)

    checkReposMatch(exercisesInMain, exercisesAndSHAsInLinearized)

    putBackToMain(mainRepo, linearizedRootFolder, exercisesAndSHAsInLinearized)(config)
  end delinearize

  def getExercisesAndSHAs(linearizedRootFolder: File): Vector[ExerciseNameAndSHA] =
    "git log --oneline"
      .toProcessCmd(linearizedRootFolder)
      .runAndReadOutput()
      .fold[Vector[ExerciseNameAndSHA]](handleError, processGitLogOutput)
  end getExercisesAndSHAs

  def handleError(gitLogOutput: String): Vector[ExerciseNameAndSHA] =
    printErrorAndExit(
      s"Couldn't obtain exercise info from linearized repository. Check your path to the latter\n$gitLogOutput")
    ???
  end handleError

  def processGitLogOutput(gitLogOutput: String): Vector[ExerciseNameAndSHA] =
    gitLogOutput.split("""\n""").toVector.map(splitSHAandExName).map(convertToExNameAndSHA).reverse
  end processGitLogOutput

  def convertToExNameAndSHA(v: Vector[String]): ExerciseNameAndSHA =
    v match
      case sha +: name +: _ => ExerciseNameAndSHA(name, sha)
      case _                => ???

  def splitSHAandExName(shaAndExname: String): Vector[String] =
    shaAndExname.split("""\s+""").toVector

  def checkReposMatch(exercisesInMain: Seq[String], exercisesAndSHAs: Vector[ExerciseNameAndSHA]): Unit =
    // TODO: in case repos are incompatible, print out the exercise list on both ends (if any)
    if exercisesInMain != exercisesAndSHAs.map(_.exName) then
      printErrorAndExit(s"Cannot de-linearize: repositories are incompatible")
  end checkReposMatch

  def putBackToMain(mainRepo: File, linearizedRepo: File, exercisesAndSHAs: Vector[ExerciseNameAndSHA])(
      config: CMTaConfig): Unit =

    val mainRepoActiveExerciseFolder = mainRepo / config.mainRepoExerciseFolder
    val linearizedActiveExerciseFolder =
      linearizedRepo / config.linearizedRepoActiveExerciseFolder

    for (ExerciseNameAndSHA(exercise, sha) <- exercisesAndSHAs) {
      s"git checkout $sha"
        .toProcessCmd(linearizedRepo)
        .runAndExitIfFailed(toConsoleRed(s"Unable to checkout commit($sha) corresponding to exercise: $exercise"))

      sbtio.delete(mainRepoActiveExerciseFolder / exercise)
      sbtio.copyDirectory(
        linearizedActiveExerciseFolder,
        mainRepoActiveExerciseFolder / exercise,
        preserveLastModified = true)
    }

    s"git checkout main"
      .toProcessCmd(linearizedRepo)
      .runAndExitIfFailed(toConsoleRed(s"Unable to checkout main in linearized repo"))
  end putBackToMain
