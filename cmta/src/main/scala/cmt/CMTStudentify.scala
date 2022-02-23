package cmt

import sbt.io.{IO as sbtio}
import sbt.io.syntax.*

import Helpers.{
  getExercisePrefixAndExercises,
  validatePrefixes,
  ExercisePrefixesAndExerciseNames,
  exitIfGitIndexOrWorkspaceIsntClean,
  createStudentifiedFolderSkeleton,
  addFirstExercise,
  hideExercises,
  writeStudentifiedCMTBookmark,
  writeStudentifiedCMTConfig
}

import Helpers.{initializeGitRepo, commitToGit}

object CMTStudentify:
  def studentify(
      mainRepo: File,
      stuBase: File,
      forceDeleteExistingDestinationFolder: Boolean,
      initializeAsGitRepo: Boolean
  )(using
      config: CMTaConfig
  ): Unit =

    exitIfGitIndexOrWorkspaceIsntClean(mainRepo)

    println(
      s"Studentifying ${toConsoleGreen(mainRepo.getPath)} to ${toConsoleGreen(stuBase.getPath)}"
    )

    val mainRepoName = mainRepo.getName

    val tmpFolder = sbtio.createTemporaryDirectory
    val cleanedMainRepo =
      ProcessDSL.copyCleanViaGit(mainRepo, tmpFolder, mainRepoName)

    val ExercisePrefixesAndExerciseNames(prefixes, exercises) =
      getExercisePrefixAndExercises(mainRepo)
    validatePrefixes(prefixes)
    val studentifiedRootFolder = stuBase / mainRepoName

    if studentifiedRootFolder.exists && forceDeleteExistingDestinationFolder
    then sbtio.delete(studentifiedRootFolder)

    val StudentifiedSkelFolders(solutionsFolder) =
      createStudentifiedFolderSkeleton(stuBase, studentifiedRootFolder)

    addFirstExercise(cleanedMainRepo, exercises.head, studentifiedRootFolder)

    hideExercises(cleanedMainRepo, solutionsFolder, exercises)

    writeStudentifiedCMTConfig(
      studentifiedRootFolder / config.cmtStudentifiedConfigFile,
      exercises
    )
    writeStudentifiedCMTBookmark(
      studentifiedRootFolder / ".bookmark",
      exercises.head
    )

    if initializeAsGitRepo then
      val dotIgnoreFile = cleanedMainRepo / ".gitignore"
      if dotIgnoreFile.exists then
        sbtio.copyFile(dotIgnoreFile, studentifiedRootFolder / ".gitignore")
      initializeGitRepo(studentifiedRootFolder)
      commitToGit("Initial commit", studentifiedRootFolder)

    sbtio.delete(tmpFolder)

    println(
      toConsoleGreen(
        exercises.mkString("Processed exercises:\n  ", "\n  ", "\n")
      )
    )
  end studentify
