package com.lightbend.studentify

import sbt.{IO => sbtio}
import java.io.File

/**
  * Copyright © 2014, 2015, 2016 Lightbend, Inc. All rights reserved. [http://www.lightbend.com]
  */

object Exercises {

  val ExerciseNameSpec = """.*/exercise_[0-9][0-9][0-9]_\w+$""".r

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
    sbtio.copyDirectory(firstExercise, new File(targetFolder, Settings.studentBaseProject))
  }

  def createBookmarkFile(exSolutionPaths: Seq[String], targetFolder: File): Unit = {
    val firstExercise = exSolutionPaths.sorted.head
    dumpStringToFile(firstExercise, new File(targetFolder, ".bookmark").getPath)
  }

  def createBuildFile(targetFolder: File): Unit = {

    val build =
      s"""
         |lazy val base = (project in file("."))
         |  .aggregate(
         |    common,
         |    exercises
         |  )
         |  .settings(CommonSettings.commonSettings: _*)
         |lazy val common = project.settings(CommonSettings.commonSettings: _*)
         |
         |lazy val exercises = project
         |  .settings(CommonSettings.commonSettings: _*)
         |  .dependsOn(common % "test->test;compile->compile")
         |
         |onLoad in Global := { state => Command.process("project exercises", state) }
       """.stripMargin

    val buildFile = "build.sbt"
    dumpStringToFile(build, new File(targetFolder, buildFile).getPath)
  }
}
