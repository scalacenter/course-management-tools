package com.lightbend.coursegentools

import java.io.File

object MasterAdm {

  def main(args: Array[String]): Unit = {
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
                            configurationFile) = cmdOptions.get

    implicit val config: MasterSettings = new MasterSettings(masterRepo, configurationFile)

    val exercises: Vector[String] = getExerciseNames(masterRepo)
    val exerciseNumbers = exercises.map(extractExerciseNr)

    (regenBuildFile, duplicateInsertBefore, deleteExerciseNr, renumberExercises, renumberExercisesBase, renumberExercisesStep) match {
      case (true, None, None, false, _, _) =>
        createMasterBuildFile(exercises, masterRepo, multiJVM)

      case (false, Some(dibExNr), None, false, _, _) if exerciseNumbers.contains(dibExNr) =>
        val exercise = getExerciseName(exercises, dibExNr).get
        if (exerciseNumbers.contains(dibExNr - 1)) {
          duplicateExercise(masterRepo, exercise, dibExNr)
          shiftExercisesUp(masterRepo, exercises, dibExNr, exerciseNumbers)
        } else {
          duplicateExercise(masterRepo, exercise, dibExNr - 1)
        }
        createMasterBuildFile(getExerciseNames(masterRepo), masterRepo, multiJVM)

      case (false, Some(dibExNr), None, false, _, _) =>
        println(toConsoleRed(s"Duplicate and insert before: no exercise with number $dibExNr"))
        System.exit(-1)

      case (false, None, Some(dibExNr), false, _, _) if exerciseNumbers.contains(dibExNr) =>
        import sbt.io.{IO => sbtio}
        val relativeSourceFolder = new File(masterRepo, config.relativeSourceFolder)
        val exercise = getExerciseName(exercises, dibExNr).get
        sbtio.delete(new File(relativeSourceFolder, exercise))
        createMasterBuildFile(getExerciseNames(masterRepo), masterRepo, multiJVM)

      case (false, None, Some(dibExNr), false, _, _) =>
        println(toConsoleRed(s"Delete exercise: no exercise with number $dibExNr"))
        System.exit(-1)

      case (false, None, None, true, offset, step) =>
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

      case (false, None, None, false, _, _) => println(toConsoleGreen(s"Nothing to do..."))

      case _ => println(toConsoleRed(s"Invalid combination of options"))

    }

  }


}
