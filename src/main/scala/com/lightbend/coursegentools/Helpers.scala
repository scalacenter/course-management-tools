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

  def commitRemainingExercises(exercises: Seq[String], masterRepo: File, linearizedProject: File): Unit = {
    val exercisesDstFolder = new File(linearizedProject, "exercises")
    for {
      exercise <- exercises
    } {
      val from = new File(masterRepo, exercise)
      sbtio.delete(exercisesDstFolder)
      sbtio.copyDirectory(from, exercisesDstFolder, preserveLastModified = true)
      ProcessCmd(Seq("git", "add", "-A"), linearizedProject) runAndExitIfFailed(s"Failed to add exercise files for exercise $exercise")
      ProcessCmd(Seq("git", "commit", "-m", exercise), linearizedProject) runAndExitIfFailed(s"Failed to commit exercise $exercise")
     }
  }

  def commitFirstExercise(exercise: String, linearizedProject: File): Unit = {
    ProcessCmd(Seq("git", "add", "-A"), linearizedProject) runAndExitIfFailed(s"Failed to add first exercise files")
    ProcessCmd(Seq("git", "commit", "-m", exercise), linearizedProject) runAndExitIfFailed(s"Failed to commit exercise $exercise files")
  }

  def initializeGitRepo(linearizedProject: File): Unit = {
    ProcessCmd(Seq("git", "init"), linearizedProject) runAndExitIfFailed(s"Failed to initialize linearized git repository in ${linearizedProject.getPath}")
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

  def getExerciseNames(masterRepo: File): Seq[String] = {
    val exerciseFolders = sbtio.listFiles(masterRepo, FoldersOnly()).filter(isExerciseFolder)
    exerciseFolders.map(folder => folder.getName).toList
  }

  def hideExerciseSolutions(targetFolder: File): Unit = {
    val hiddenFolder = new File(targetFolder, Settings.solutionsFolder)
    sbtio.createDirectory(hiddenFolder)
    val exercises = sbtio.listFiles(targetFolder, FoldersOnly()).filter(isExerciseFolder)
    exercises.foreach { exercise =>
      sbtio.move(exercise, new File(hiddenFolder, exercise.getName))
    }
  }

  def stageFirstExercise(firstEx: String, masterRepo: File, targetFolder: File): Unit = {
    val firstExercise = new File(masterRepo, firstEx)
    sbtio.copyDirectory(firstExercise, new File(targetFolder, Settings.studentBaseProject), preserveLastModified = true)
  }

  def createBookmarkFile(exSolutionPaths: Seq[String], targetFolder: File): Unit = {
    val firstExercise = exSolutionPaths.sorted.head
    dumpStringToFile(firstExercise, new File(targetFolder, ".bookmark").getPath)
  }

  def createBuildFile(targetFolder: File): Unit = {

    for {
      sbtTemplateFile <- sbtio.listFiles(new File("."), SbtTemplateFile())
      sbtFileName = sbtTemplateFile.getName.replaceAll(".template", "")
      sbtFile = new File(targetFolder, sbtFileName)
    } {
      sbtio.copyFile(sbtTemplateFile, sbtFile)
    }
  }

  def cleanUp(files: Seq[String], targetFolder: File): Unit = {
    for (file <- files) {
      sbtio.delete(new File(targetFolder, file))
    }
  }
}
