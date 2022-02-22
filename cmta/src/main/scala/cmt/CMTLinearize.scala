package cmt

import sbt.io.{IO as sbtio}
import sbt.io.syntax.*

import Helpers.{
  getExercisePrefixAndExercises,
  ExercisePrefixesAndExerciseNames,
  validatePrefixes,
  exitIfGitIndexOrWorkspaceIsntClean
}
import ProcessDSL.toProcessCmd

object CMTLinearize:
  def linearize(
      mainRepo: File,
      linBase: File,
      forceDeleteExistingDestinationFolder: Boolean
  )(using config: CMTaConfig): Unit =

    exitIfGitIndexOrWorkspaceIsntClean(mainRepo)

    println(
      s"Linearizing ${toConsoleGreen(mainRepo.getPath)} to ${toConsoleGreen(linBase.getPath)}"
    )

    val mainRepoName = mainRepo.getName

    val tmpFolder = sbtio.createTemporaryDirectory
    val cleanedMainRepo =
      ProcessDSL.copyCleanViaGit(mainRepo, tmpFolder, mainRepoName)

    val ExercisePrefixesAndExerciseNames(prefixes, exercises) =
      getExercisePrefixAndExercises(mainRepo)
    validatePrefixes(prefixes)

    val linearizedRootFolder = linBase / mainRepoName

    if linearizedRootFolder.exists && forceDeleteExistingDestinationFolder then
      sbtio.delete(linearizedRootFolder)
    sbtio.createDirectory(linearizedRootFolder)

    initializeGitRepo(linearizedRootFolder)

    for {
      exercise <- exercises
      from = cleanedMainRepo / config.mainRepoExerciseFolder / exercise
      linearizedCodeFolder =
        linearizedRootFolder / config.linearizedRepoActiveExerciseFolder
    } {
      println(s"Copying from $from to $linearizedCodeFolder")
      sbtio.delete(linearizedCodeFolder)
      sbtio.createDirectory(linearizedCodeFolder)
      sbtio.copyDirectory(
        from,
        linearizedCodeFolder,
        preserveLastModified = true
      )
      commitExercise(exercise, linearizedRootFolder)
    }

    sbtio.delete(tmpFolder)
  end linearize

  private def initializeGitRepo(linearizedProject: File): Unit =
    s"git init"
      .toProcessCmd(workingDir = linearizedProject)
      .runAndExitIfFailed(
        toConsoleRed(
          s"Failed to initialize linearized git repository in ${linearizedProject.getPath}"
        )
      )
  end initializeGitRepo

  private def commitExercise(exercise: String, linearizedProject: File): Unit =
    s"git add -A"
      .toProcessCmd(workingDir = linearizedProject)
      .runAndExitIfFailed(toConsoleRed(s"Failed to add first exercise files"))
    s"git commit -m $exercise"
      .toProcessCmd(workingDir = linearizedProject)
      .runAndExitIfFailed(
        toConsoleRed(s"Failed to add exercise files for exercise $exercise")
      )
  end commitExercise
