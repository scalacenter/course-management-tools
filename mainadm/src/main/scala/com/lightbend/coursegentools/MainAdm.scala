package com.lightbend.coursegentools

import java.io.File

import com.lightbend.coursegentools.GenTests.generateTestScript

object MainAdm {

  def main(args: Array[String]): Unit = {
    import Helpers._
    import ProcessDSL._

    val cmdOptions = MainAdmCmdLineOptParse.parse(args)
    if (cmdOptions.isEmpty) System.exit(-1)
    val MainAdmCmdOptions(mainRepo,
                          multiJVM,
                          regenBuildFile,
                          duplicateInsertBefore,
                          deleteExerciseNr,
                          renumberExercises,
                          renumberExercisesBase,
                          renumberExercisesStep,
                          configurationFile,
                          checkMain,
                          updateMainCommands,
                          useConfigureForProjects,
                          genTests,
                          initStudentifiedRepoAsGit,
                          isADottyProject,
                          autoReloadOnBuildDefChange,
                          initCmdOptions
    ) = cmdOptions.get

    implicit val exitOnFirstError: ExitOnFirstError = ExitOnFirstError(true)
    implicit val config: MainSettings = new MainSettings(mainRepo, configurationFile)

    if (initCmdOptions.isDefined) {
      MainAdmInit.initCourseRepo(initCmdOptions, config)
    } else {

      val exercises: Vector[String] = getExerciseNames(mainRepo)
      val exerciseNumbers = exercises.map(extractExerciseNr)

      // Option `-g` implies `-t`
      if (initStudentifiedRepoAsGit && genTests.isEmpty)
        printError(s"Setting option -g is only valid in combination with option -t")

      (regenBuildFile,
        duplicateInsertBefore,
        deleteExerciseNr,
        renumberExercises,
        renumberExercisesBase,
        renumberExercisesStep,
        checkMain,
        updateMainCommands,
        genTests,
        isADottyProject
      ) match {
        case (true, // Re-generate main build file
        None,
        None,
        false,
        _,
        _,
        false,
        false,
        None,
        _
          ) =>
          createMainBuildFile(exercises,
            mainRepo,
            multiJVM,
            isADottyProject,
            autoReloadOnBuildDefChange
          )

        case (false,
        Some(dibExNr), // Duplicate selected exercise and insert before
        None,
        false,
        _,
        _,
        false,
        false,
        None,
        _
          ) if exerciseNumbers.contains(dibExNr) =>
          val exercise = getExerciseName(exercises, dibExNr).get
          if (exerciseNumbers.contains(dibExNr - 1) || dibExNr == 0) {
            duplicateExercise(mainRepo, exercise, dibExNr)
            shiftExercisesUp(mainRepo, exercises, dibExNr, exerciseNumbers)
          }
          else
            duplicateExercise(mainRepo, exercise, dibExNr - 1)
          createMainBuildFile(getExerciseNames(mainRepo),
            mainRepo,
            multiJVM,
            isADottyProject,
            autoReloadOnBuildDefChange
          )

        case (false,
        Some(dibExNr), // Try to duplicate a non-existing exercise
        None,
        false,
        _,
        _,
        false,
        false,
        None,
        _
          ) =>
          printError(s"ERROR: Duplicate and insert before: no exercise with number $dibExNr")

        case (false,
        None,
        Some(dibExNr), // Delete selected exercise
        false,
        _,
        _,
        false,
        false,
        None,
        _
          ) if exerciseNumbers.contains(dibExNr) =>
          import sbt.io.{IO => sbtio}
          val relativeSourceFolder = new File(mainRepo, config.relativeSourceFolder)
          val exercise = getExerciseName(exercises, dibExNr).get
          sbtio.delete(new File(relativeSourceFolder, exercise))
          createMainBuildFile(getExerciseNames(mainRepo),
            mainRepo,
            multiJVM,
            isADottyProject,
            autoReloadOnBuildDefChange
          )

        case (false,
        None,
        Some(dibExNr), // Try to delete a non-existing exercise
        false,
        _,
        _,
        false,
        false,
        None,
        _
          ) =>
          printError(s"ERROR: Delete exercise: no exercise with number $dibExNr")

        case (false,
        None,
        None,
        true,
        offset,
        step, // Renumber all exercises with start offset & step size
        false,
        false,
        None,
        _
          ) =>
          import sbt.io.{IO => sbtio}
          val relativeSourceFolder = new File(mainRepo, config.relativeSourceFolder)
          val moves = for {
            (exercise, index) <- exercises.zipWithIndex
            newIndex = offset + index * step
            oldExDir = new File(relativeSourceFolder, exercise)
            newExDir = new File(relativeSourceFolder, renumberExercise(exercise, newIndex))
            if oldExDir != newExDir
          } yield (oldExDir, newExDir)
          sbtio.move(moves)
          createMainBuildFile(getExerciseNames(mainRepo),
            mainRepo,
            multiJVM,
            isADottyProject,
            autoReloadOnBuildDefChange
          )

        case (false,
        None,
        None,
        false,
        _,
        _,
        true, // Check sanity of main repository
        false,
        None,
        _
          ) =>
          val tryGitVersion = "git --version".toProcessCmd(workingDir = mainRepo).run
          if (tryGitVersion != 0)
            printError(s"ERROR: git not found: makes sure git is properly installed")

          val gitStatus = "git status".toProcessCmd(workingDir = mainRepo).run
          if (gitStatus != 0)
            printError(s"ERROR: Main repository is not a git project")

          exitIfGitIndexOrWorkspaceIsntClean(mainRepo)
          val projectName = mainRepo.getName

          val tmpDir = cleanMainViaGit(mainRepo, projectName)
          val cleanMainRepo = new File(tmpDir, projectName)
          checkMainRepo(cleanMainRepo, exercises, exitOnFirstError = false)

        case (false,
        None,
        None,
        false,
        _,
        _,
        false,
        true, // update main commands in main repo
        None,
        _
          ) =>
          addMainCommands(mainRepo)

        case (false,
        None,
        None,
        false,
        _,
        _,
        false,
        false,
        Some(testFile), // generate test scripts
        _
          ) =>
          generateTestScript(mainRepo,
            config.relativeSourceFolder,
            configurationFile,
            testFile,
            exercises,
            exerciseNumbers,
            isADottyProject,
            initStudentifiedRepoAsGit
          )

        case (false, None, None, false, _, _, false, false, None, _) =>
          println(toConsoleGreen(s"Nothing to do..."))

        case _ => printError(s"ERROR: Invalid combination of options")

      }
    }


  }

}
