package cmt

import sbt.io.{IO as sbtio}
import sbt.io.syntax.*

import Helpers.{
  getExercisePrefixAndExercises,
  ExercisePrefixesAndExerciseNames,
  validatePrefixes,
  exitIfGitIndexOrWorkspaceIsntClean
}

import Helpers.{initializeGitRepo, commitToGit}

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
      commitToGit(exercise, linearizedRootFolder)
    }

    sbtio.delete(tmpFolder)
  end linearize
