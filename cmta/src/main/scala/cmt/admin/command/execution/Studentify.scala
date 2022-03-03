package cmt.admin.command.execution

import cmt.Helpers.*
import cmt.admin.command.AdminCommand.Studentify
import cmt.core.execution.Executable
import cmt.{ProcessDSL, StudentifiedSkelFolders, toConsoleGreen}
import sbt.io.IO as sbtio
import sbt.io.syntax.*

given Executable[Studentify] with
  extension (cmd: Studentify)
    def execute(): Either[String, String] = {
      exitIfGitIndexOrWorkspaceIsntClean(cmd.mainRepository.value)

      println(s"Studentifying ${toConsoleGreen(cmd.mainRepository.value.getPath)} to ${toConsoleGreen(
          cmd.studentifyBaseDirectory.value.getPath)}")

      val mainRepoName = cmd.mainRepository.value.getName

      val tmpFolder = sbtio.createTemporaryDirectory
      val cleanedMainRepo =
        ProcessDSL.copyCleanViaGit(cmd.mainRepository.value, tmpFolder, mainRepoName)

      val ExercisePrefixesAndExerciseNames_TBR(prefixes, exercises) =
        getExercisePrefixAndExercises_TBR(cmd.mainRepository.value)(cmd.config)
      validatePrefixes(prefixes)
      val studentifiedRootFolder = cmd.studentifyBaseDirectory.value / mainRepoName

      if studentifiedRootFolder.exists && cmd.forceDeleteDestinationDirectory.value
      then sbtio.delete(studentifiedRootFolder)

      val StudentifiedSkelFolders(solutionsFolder) =
        createStudentifiedFolderSkeleton(cmd.studentifyBaseDirectory.value, studentifiedRootFolder)(cmd.config)

      addFirstExercise(cleanedMainRepo, exercises.head, studentifiedRootFolder)(cmd.config)

      hideExercises(cleanedMainRepo, solutionsFolder, exercises)(cmd.config)

      writeStudentifiedCMTConfig(studentifiedRootFolder / cmd.config.cmtStudentifiedConfigFile, exercises)(cmd.config)
      writeStudentifiedCMTBookmark(studentifiedRootFolder / ".bookmark", exercises.head)

      if cmd.initializeAsGitRepo.value then
        val dotIgnoreFile = cleanedMainRepo / ".gitignore"
        if dotIgnoreFile.exists then sbtio.copyFile(dotIgnoreFile, studentifiedRootFolder / ".gitignore")
        initializeGitRepo(studentifiedRootFolder)
        commitToGit("Initial commit", studentifiedRootFolder)

      sbtio.delete(tmpFolder)

      Right(exercises.mkString("Processed exercises:\n  ", "\n  ", "\n"))
    }
