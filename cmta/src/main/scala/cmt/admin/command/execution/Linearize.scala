package cmt.admin.command.execution

import cmt.Helpers.*
import cmt.admin.command.AdminCommand.Linearize
import cmt.core.execution.Executable
import cmt.{ProcessDSL, toConsoleGreen}
import sbt.io.IO as sbtio
import sbt.io.syntax.*

given Executable[Linearize] with
  extension (cmd: Linearize)
    def execute(): Either[String, String] = {
      exitIfGitIndexOrWorkspaceIsntClean(cmd.mainRepository.value)

      println(s"Linearizing ${toConsoleGreen(cmd.mainRepository.value.getPath)} to ${toConsoleGreen(
          cmd.linearizeBaseDirectory.value.getPath)}")

      val mainRepoName = cmd.mainRepository.value.getName

      val tmpFolder = sbtio.createTemporaryDirectory
      val cleanedMainRepo =
        ProcessDSL.copyCleanViaGit(cmd.mainRepository.value, tmpFolder, mainRepoName)

      val ExercisePrefixesAndExerciseNames_TBR(prefixes, exercises) =
        getExercisePrefixAndExercises_TBR(cmd.mainRepository.value)(cmd.config)
      validatePrefixes(prefixes)

      val linearizedRootFolder = cmd.linearizeBaseDirectory.value / mainRepoName

      if linearizedRootFolder.exists && cmd.forceDeleteDestinationDirectory.value then
        sbtio.delete(linearizedRootFolder)
      sbtio.createDirectory(linearizedRootFolder)

      initializeGitRepo(linearizedRootFolder)

      for {
        exercise <- exercises
        from = cleanedMainRepo / cmd.config.mainRepoExerciseFolder / exercise
        linearizedCodeFolder =
          linearizedRootFolder / cmd.config.linearizedRepoActiveExerciseFolder
      } {
        println(s"Copying from $from to $linearizedCodeFolder")
        sbtio.delete(linearizedCodeFolder)
        sbtio.createDirectory(linearizedCodeFolder)
        sbtio.copyDirectory(from, linearizedCodeFolder, preserveLastModified = true)
        commitToGit(exercise, linearizedRootFolder)
      }

      sbtio.delete(tmpFolder)

      Right(s"Successfully linearized ${cmd.mainRepository.value.getPath}")
    }
