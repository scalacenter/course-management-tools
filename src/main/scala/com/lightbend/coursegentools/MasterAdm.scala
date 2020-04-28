package com.lightbend.coursegentools

import java.io.File
import GenTests.generateTestScript

object MasterAdm {

  def main(args: Array[String]): Unit = {
    import ProcessDSL._
    import Helpers._

    val cmdOptions = MasterAdmCmdLineOptParse.parse(args)
    if (cmdOptions.isEmpty) System.exit(-1)
    val MasterAdmCmdOptions(masterRepo,
                            multiJVM,
                            regenBuildFile,
                            duplicateInsertBefore,
                            deleteExerciseNr,
                            renumberExercises,
                            renumberExercisesBase,
                            renumberExercisesStep,
                            configurationFile,
                            checkMaster,
                            updateMasterCommands,
                            useConfigureForProjects,
                            genTests,
                            isADottyProject) = cmdOptions.get

    implicit val config: MasterSettings = new MasterSettings(masterRepo, configurationFile)
    implicit val exitOnFirstError: ExitOnFirstError = ExitOnFirstError(true)

    val exercises: Vector[String] = getExerciseNames(masterRepo)
    val exerciseNumbers = exercises.map(extractExerciseNr)

    (regenBuildFile,
     duplicateInsertBefore,
     deleteExerciseNr,
     renumberExercises, renumberExercisesBase, renumberExercisesStep,
     checkMaster,
     updateMasterCommands,
     genTests,
     isADottyProject) match {
      case (
        true,  // Re-generate master build file
        None,
        None,
        false, _, _,
        false,
        false,
        None,
        _) =>
          createMasterBuildFile(exercises, masterRepo, multiJVM, isADottyProject)

      case (
        false,
        Some(dibExNr), // Duplicate selected exercise and insert before
        None,
        false, _, _,
        false,
        false,
        None,
        _) if exerciseNumbers.contains(dibExNr) =>
          val exercise = getExerciseName(exercises, dibExNr).get
          if (exerciseNumbers.contains(dibExNr - 1) || dibExNr == 0) {
            duplicateExercise(masterRepo, exercise, dibExNr)
            shiftExercisesUp(masterRepo, exercises, dibExNr, exerciseNumbers)
          } else {
            duplicateExercise(masterRepo, exercise, dibExNr - 1)
          }
          createMasterBuildFile(getExerciseNames(masterRepo), masterRepo, multiJVM, isADottyProject)

      case (
        false,
        Some(dibExNr), // Try to duplicate a non-existing exercise
        None,
        false, _, _,
        false,
        false,
        None,
        _) =>
          printError(s"ERROR: Duplicate and insert before: no exercise with number $dibExNr")

      case (
        false,
        None,
        Some(dibExNr), // Delete selected exercise
        false, _, _,
        false,
        false,
        None,_) if exerciseNumbers.contains(dibExNr) =>
          import sbt.io.{IO => sbtio}
          val relativeSourceFolder = new File(masterRepo, config.relativeSourceFolder)
          val exercise = getExerciseName(exercises, dibExNr).get
          sbtio.delete(new File(relativeSourceFolder, exercise))
          createMasterBuildFile(getExerciseNames(masterRepo), masterRepo, multiJVM, isADottyProject)

      case (
        false,
        None,
        Some(dibExNr), // Try to delete a non-existing exercise
        false, _, _,
        false,
        false,
        None,_) =>
          printError(s"ERROR: Delete exercise: no exercise with number $dibExNr")

      case (
        false,
        None,
        None,
        true, offset, step, // Renumber all exercises with start offset & step size
        false,
        false,
        None,
        _) =>
          import sbt.io.{IO => sbtio}
          val relativeSourceFolder = new File(masterRepo, config.relativeSourceFolder)
          val moves = for {
            (exercise, index) <- exercises.zipWithIndex
            newIndex = offset + index * step
            oldExDir = new File(relativeSourceFolder, exercise)
            newExDir = new File(relativeSourceFolder, renumberExercise(exercise, newIndex))
              if oldExDir != newExDir
          } yield (oldExDir, newExDir)
          sbtio.move(moves)
          createMasterBuildFile(getExerciseNames(masterRepo), masterRepo, multiJVM, isADottyProject)

      case (
        false,
        None,
        None,
        false, _, _,
        true,   // Check sanity of master repository
        false,
        None,
        _) =>

          val tryGitVersion = "git --version".toProcessCmd(workingDir = masterRepo).run
          if (tryGitVersion != 0)
            printError(s"ERROR: git not found: makes sure git is properly installed")

          val gitStatus = "git status".toProcessCmd(workingDir = masterRepo).run
          if (gitStatus != 0)
            printError(s"ERROR: Master repository is not a git project")

          exitIfGitIndexOrWorkspaceIsntClean(masterRepo)
          val projectName = masterRepo.getName

          val tmpDir = cleanMasterViaGit(masterRepo, projectName)
          val cleanMasterRepo = new File(tmpDir, projectName)
          checkMasterRepo(cleanMasterRepo, exercises, exitOnFirstError = false)

      case (
        false,
        None,
        None,
        false, _, _,
        false,
        true, // update master commands in master repo
        None,
        _) =>

          addMasterCommands(masterRepo)


      case (
        false,
        None,
        None,
        false, _, _,
        false,
        false,
        Some(testFile), // generate test scripts
        _) =>
          generateTestScript(masterRepo, config.relativeSourceFolder, configurationFile ,testFile, exercises, exerciseNumbers)


      case (false, None, None, false, _, _, false, false, None, _) => println(toConsoleGreen(s"Nothing to do..."))

      case _ => printError(s"ERROR: Invalid combination of options")

    }

  }


}
