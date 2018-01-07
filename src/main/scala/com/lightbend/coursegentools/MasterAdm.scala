package com.lightbend.coursegentools

import java.io.File

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
                            updateMasterCommands) = cmdOptions.get

    implicit val config: MasterSettings = new MasterSettings(masterRepo, configurationFile)
    implicit val exitOnFirstError: ExitOnFirstError = ExitOnFirstError(true)

    val exercises: Vector[String] = getExerciseNames(masterRepo)
    val exerciseNumbers = exercises.map(extractExerciseNr)

    println(s"Exercises: $exercises")

    (regenBuildFile,
     duplicateInsertBefore,
     deleteExerciseNr,
     renumberExercises, renumberExercisesBase, renumberExercisesStep,
     checkMaster,
     updateMasterCommands) match {
      case (true,
            None,
            None,
            false, _, _,
            false,
            false) =>
        createMasterBuildFile(exercises, masterRepo, multiJVM)

      case (false,
            Some(dibExNr),
            None,
            false, _, _,
            false,
            false) if exerciseNumbers.contains(dibExNr) =>
        val exercise = getExerciseName(exercises, dibExNr).get
        if (exerciseNumbers.contains(dibExNr - 1) || dibExNr == 0) {
          duplicateExercise(masterRepo, exercise, dibExNr)
          shiftExercisesUp(masterRepo, exercises, dibExNr, exerciseNumbers)
        } else {
          duplicateExercise(masterRepo, exercise, dibExNr - 1)
        }
        createMasterBuildFile(getExerciseNames(masterRepo), masterRepo, multiJVM)

      case (false,
            Some(dibExNr),
            None,
            false, _, _,
            false,
            false) =>
        printError(s"ERROR: Duplicate and insert before: no exercise with number $dibExNr")

      case (false,
            None,
            Some(dibExNr),
            false, _, _,
            false,
            false) if exerciseNumbers.contains(dibExNr) =>
        import sbt.io.{IO => sbtio}
        val relativeSourceFolder = new File(masterRepo, config.relativeSourceFolder)
        val exercise = getExerciseName(exercises, dibExNr).get
        sbtio.delete(new File(relativeSourceFolder, exercise))
        createMasterBuildFile(getExerciseNames(masterRepo), masterRepo, multiJVM)

      case (false,
            None,
            Some(dibExNr),
            false, _, _,
            false,
            false) =>
        printError(s"ERROR: Delete exercise: no exercise with number $dibExNr")

      case (false,
            None,
            None,
            true, offset, step,
            false,
            false) =>
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
        createMasterBuildFile(getExerciseNames(masterRepo), masterRepo, multiJVM)

      case (false,
            None,
            None,
            false, _, _,
            true,
            false) =>

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

      case (false,
            None,
            None,
            false, _, _,
            false,
            true) =>
        addMasterCommands(masterRepo)

      case (false, None, None, false, _, _, false, false) => println(toConsoleGreen(s"Nothing to do..."))

      case _ => printError(s"ERROR: Invalid combination of options")

    }

  }


}
