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
      exitIfGitIndexOrWorkspaceIsntClean(cmd.mainRepository.value)

      println(s"De-linearizing ${toConsoleGreen(cmd.mainRepository.value.getPath)} to ${toConsoleGreen(
          cmd.linearizeBaseDirectory.value.getPath)}")

      val mainRepoName = cmd.mainRepository.value.getName

      val ExercisePrefixesAndExerciseNames_TBR(prefixes, exercisesInMain) =
        getExercisePrefixAndExercises_TBR(cmd.mainRepository.value)(cmd.config)
      validatePrefixes(prefixes)

      val linearizedRootFolder = cmd.linearizeBaseDirectory.value / mainRepoName

      val exercisesAndSHAsInLinearized = getExercisesAndSHAs(linearizedRootFolder)

      checkReposMatch(exercisesInMain, exercisesAndSHAsInLinearized)

      putBackToMain(cmd.mainRepository.value, linearizedRootFolder, exercisesAndSHAsInLinearized)(cmd.config)

      Right(s"Successfully delinearised ${cmd.linearizeBaseDirectory.value.getPath}")
    }

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
