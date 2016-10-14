package com.lightbend.coursegentools

import sbt.{IO => sbtio}
import java.io.File
import scala.sys.process.Process

/**
  * Copyright © 2014, 2015, 2016 Lightbend, Inc. All rights reserved. [http://www.lightbend.com]
  */

object Helpers {

  import ProcessDSL._

  val ExerciseNameSpec = """.*/exercise_[0-9][0-9][0-9]_\w+$""".r

  def putBackToMaster(masterRepo: File, linearizedRepo: File, exercisesAndSHAs: Vector[ExNameAndSHA]): Unit = {

    for (ExNameAndSHA(exercise, sha) <- exercisesAndSHAs) {
      s"git checkout $sha"
        .toProcessCmd(linearizedRepo)
        .runAndExitIfFailed(s"Unable to checkout commit($sha) corresponding to exercise: $exercise")

      sbtio.delete(new File(masterRepo, exercise))
      sbtio.copyDirectory(new File(linearizedRepo, "exercises"), new File(masterRepo, exercise), preserveLastModified = true)
    }

    s"git checkout master".toProcessCmd(linearizedRepo).runAndExitIfFailed(s"Unable to checkout master in linearized repo")
  }

  def getExercisesAndSHAs(linearizedOutputFolder: File): Vector[ExNameAndSHA] = {
    def convertToExNameAndSHA(v: Vector[String]): ExNameAndSHA = {
      v match {
        case sha +: name +: _ => ExNameAndSHA(name, sha)
      }
    }
    def splitSHAandExName(shaAndExname: String): Vector[String] = {
      shaAndExname.split("""\s+""").toVector
    }

    Process(Seq("git", "log", "--oneline"), linearizedOutputFolder).!!
      .split("""\n""").toVector
      .map(splitSHAandExName)
      .map(convertToExNameAndSHA)
      .reverse
  }

  def checkReposMatch(exercisesInMaster: Vector[String], exercisesAndSHAs: Vector[ExNameAndSHA]): Unit = {
    if (exercisesInMaster != exercisesAndSHAs.map(_.exName)) {
      println(s"Repos are incompatible")
      System.exit(-1)
    }
  }

  def commitRemainingExercises(exercises: Seq[String], masterRepo: File, linearizedProject: File): Unit = {
    val exercisesDstFolder = new File(linearizedProject, "exercises")
    for { exercise <- exercises } {
      val from = new File(masterRepo, exercise)
      sbtio.delete(exercisesDstFolder)
      sbtio.copyDirectory(from, exercisesDstFolder, preserveLastModified = true)

      s"git add -A"
        .toProcessCmd(workingDir = linearizedProject)
        .runAndExitIfFailed(s"Failed to add exercise files for exercise $exercise")

      s"git commit -m $exercise"
        .toProcessCmd(workingDir = linearizedProject)
        .runAndExitIfFailed(s"Failed to add exercise files for exercise $exercise")
     }
  }

  def commitFirstExercise(exercise: String, linearizedProject: File): Unit = {
    s"git add -A"
      .toProcessCmd(workingDir = linearizedProject)
      .runAndExitIfFailed(s"Failed to add first exercise files")

    s"git commit -m $exercise"
      .toProcessCmd(linearizedProject)
      .runAndExitIfFailed(s"Failed to commit exercise $exercise files")
  }

  def initializeGitRepo(linearizedProject: File): Unit = {
    s"git init"
      .toProcessCmd(linearizedProject)
      .runAndExitIfFailed(s"Failed to initialize linearized git repository in ${linearizedProject.getPath}")
  }

  def removeExercisesFromCleanMaster(cleanMasterRepo: File, exercises: Seq[String]): Unit = {
    for {
      exercise <- exercises
    } {
      val exerciseFolder = new File(cleanMasterRepo, exercise)
      if (exerciseFolder.exists()) {
        sbtio.delete(exerciseFolder)
      } else {
        println(s"Error in removeExercisesFromCleanMaster, bailing out")
        System.exit(-1)
      }
    }
  }

  def cleanMasterViaGit(srcFolder: File, projectName: String): File = {
    val projectName = srcFolder.getName
    val tmpDir = sbtio.createTemporaryDirectory
    val curDir = new File(System.getProperty("user.dir"))

    val status = Process(Seq("./cpCleanViaGit.sh", srcFolder.getPath, tmpDir.getPath, projectName), new File(System.getProperty("user.dir"))).!
    tmpDir
  }

  def isExerciseFolder(folder: File): Boolean = {
    ExerciseNameSpec.findFirstIn(folder.getPath).isDefined
  }

  def copyMaster(masterRepo: File, targetFolder: File): Unit = {
    sbtio.copyDirectory(masterRepo, targetFolder, overwrite = false, preserveLastModified = true)
  }

  def getExerciseNames(masterRepo: File): Vector[String] = {
    val exerciseFolders = sbtio.listFiles(masterRepo, FoldersOnly()).filter(isExerciseFolder)
    exerciseFolders.map(folder => folder.getName).toVector
  }

  def hideExerciseSolutions(targetFolder: File, selectedExercises: Seq[String]): Unit = {
    val hiddenFolder = new File(targetFolder, Settings.solutionsFolder)
    sbtio.createDirectory(hiddenFolder)
    val exercises = sbtio.listFiles(targetFolder, FoldersOnly()).filter(isExerciseFolder)
    exercises.foreach { exercise =>
      if (selectedExercises contains exercise.getName) {
        sbtio.move(exercise, new File(hiddenFolder, exercise.getName))
      } else {
        sbtio.delete(exercise)
      }
    }
  }

  def getInitialExercise(selectedFirstOpt: Option[String], selectedExercises: Seq[String]): String = {
    val selectedExercise = selectedFirstOpt.getOrElse(selectedExercises.head)
    if (selectedExercises contains selectedExercise)
      selectedExercise
    else {
      println(s"Exercise on start not in selected range of exercises")
      System.exit(-1)
      selectedExercise
    }
  }
  def stageFirstExercise(firstEx: String, masterRepo: File, targetFolder: File): Unit = {
    val firstExercise = new File(masterRepo, firstEx)
    sbtio.copyDirectory(firstExercise, new File(targetFolder, Settings.studentBaseProject), preserveLastModified = true)
  }

  def createBookmarkFile(firstExercise: String, targetFolder: File): Unit = {
    //val firstExercise = exSolutionPaths.sorted.head
    dumpStringToFile(firstExercise, new File(targetFolder, ".bookmark").getPath)
  }

  def createSbtRcFile(targetFolder: File): Unit = {
    dumpStringToFile("alias boot = ;reload ;project exercises ;iflast shell", new File(targetFolder, ".sbtrc").getPath)
  }

  def addSbtStudentCommands(sbtStudentCommandsTemplateFolder: File, targetCourseFolder: File): Unit = {
    val projectFolder = new File(targetCourseFolder, "project")
    val moves = for {
      template <- sbtio.listFiles(sbtStudentCommandsTemplateFolder)
      target = new File(projectFolder, template.getName.replaceAll(".scala.template", ".scala"))
    } yield (template, target)
    // Don't overwrite already existing target files. Used specifically in the case when master project
    // contains definitions for sbt command aliases and/or sbt console initial commands
    val selectedMoves = moves.filterNot { case (_, target) => target.exists()}
    sbtio.copy(selectedMoves)
  }

  def getSelectedExercises(exercises: Seq[String], firstOpt: Option[String], lastOpt: Option[String]): Seq[String] = {
    val (firstExercise, lastExercise) = (exercises.head, exercises.last)
    val selExcs = (firstOpt.getOrElse(exercises.head), lastOpt.getOrElse(exercises.last)) match {
      case (`firstExercise`, `lastExercise`) =>
        exercises
      case (first, last) =>
        exercises.dropWhile(_ != first).reverse.dropWhile(_ != last).reverse

    }
    if (selExcs isEmpty) {
      println(s"Invalid exercise selection")
      System.exit(-1)
    }
    selExcs
  }

  def createBuildFile(targetFolder: File, multiJVM: Boolean): Unit = {

    val buildFileTemplate =
      if (multiJVM) {
        "build-mjvm.sbt.template"
      } else {
        "build.sbt.template"
      }
    sbtio.copyFile(new File(buildFileTemplate), new File(targetFolder, "build.sbt"))

//    val templateFiles = sbtio.listFiles(new File("."), SbtTemplateFile()).filterNot(_.getName startsWith("build"))
//    for {
//      sbtTemplateFile <- templateFiles
//      sbtFileName = sbtTemplateFile.getName.replaceAll(".template", "")
//      sbtFile = new File(targetFolder, sbtFileName)
//    } {
//      sbtio.copyFile(sbtTemplateFile, sbtFile)
//    }
  }

  def cleanUp(files: Seq[String], targetFolder: File): Unit = {
    for (file <- files) {
      sbtio.delete(new File(targetFolder, file))
    }
  }
}
