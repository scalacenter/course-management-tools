package cmt

import sbt.io.IO as sbtio
import sbt.io.syntax.*

import Helpers.{
  getExercisePrefixAndExercises_TBR,
  validatePrefixes,
  ExercisePrefixesAndExerciseNames_TBR,
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
      initializeAsGitRepo: Boolean)(config: CMTaConfig): Unit =

    exitIfGitIndexOrWorkspaceIsntClean(mainRepo)

    println(s"Studentifying ${toConsoleGreen(mainRepo.getPath)} to ${toConsoleGreen(stuBase.getPath)}")

    val mainRepoName = mainRepo.getName

    val tmpFolder = sbtio.createTemporaryDirectory
    val cleanedMainRepo =
      ProcessDSL.copyCleanViaGit(mainRepo, tmpFolder, mainRepoName)

    val ExercisePrefixesAndExerciseNames_TBR(prefixes, exercises) =
      getExercisePrefixAndExercises_TBR(mainRepo)(config)
    validatePrefixes(prefixes)
    val studentifiedRootFolder = stuBase / mainRepoName

    if studentifiedRootFolder.exists && forceDeleteExistingDestinationFolder
    then sbtio.delete(studentifiedRootFolder)

    val StudentifiedSkelFolders(solutionsFolder) =
      createStudentifiedFolderSkeleton(stuBase, studentifiedRootFolder)(config)

    addFirstExercise(cleanedMainRepo, exercises.head, studentifiedRootFolder)(config)

    hideExercises(cleanedMainRepo, solutionsFolder, exercises)(config)

    writeStudentifiedCMTConfig(studentifiedRootFolder / config.cmtStudentifiedConfigFile, exercises)(config)
    writeStudentifiedCMTBookmark(studentifiedRootFolder / ".bookmark", exercises.head)

    if initializeAsGitRepo then
      val dotIgnoreFile = cleanedMainRepo / ".gitignore"
      if dotIgnoreFile.exists then sbtio.copyFile(dotIgnoreFile, studentifiedRootFolder / ".gitignore")
      initializeGitRepo(studentifiedRootFolder)
      commitToGit("Initial commit", studentifiedRootFolder)

    sbtio.delete(tmpFolder)

    println(toConsoleGreen(exercises.mkString("Processed exercises:\n  ", "\n  ", "\n")))
  end studentify
