package com.lightbend.coursegentools

import sbt.{IO => sbtio}
import java.io.File
import scala.sys.process.Process

/**
  * Copyright © 2016 Lightbend, Inc
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *
  * NO COMMERCIAL SUPPORT OR ANY OTHER FORM OF SUPPORT IS OFFERED ON
  * THIS SOFTWARE BY LIGHTBEND, Inc.
  *
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

object Helpers {

  import ProcessDSL._

  val ExerciseNameSpec = """.*/exercise_[0-9][0-9][0-9]_\w+$""".r

  def fileList(base: File): Vector[File] = {
    @scala.annotation.tailrec
    def fileList(filesSoFar: Vector[File], folders: Vector[File]): Vector[File] = {
      val subs = (folders foldLeft Vector.empty[File]) {
        case (tally, folder) =>
          tally ++ sbtio.listFiles(folder)
      }
      subs.partition(_.isDirectory) match {
        case (rem, result) if rem.isEmpty => filesSoFar ++ result
        case (rem, tally) => fileList(filesSoFar ++ tally, rem)
      }
    }

    val (seedFolders, seedFiles) = sbtio.listFiles(base).partition(_.isDirectory)
    fileList(seedFiles.toVector, seedFolders.toVector)
  }

  def zipSolution(exFolder: File, removeOriginal: Boolean = false): Unit = {
    val fl = fileList(exFolder).map(f => (f, sbtio.relativize(exFolder.getParentFile, f).get))
    val zipFile = new File(exFolder.getParentFile, s"${exFolder.getName}.zip")
    sbtio.zip(fl, zipFile)
    if (removeOriginal) sbtio.delete(exFolder)
  }

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
    val tmpDir = sbtio.createTemporaryDirectory
    val curDir = new File(System.getProperty("user.dir"))
    val status = Process(Seq("./cpCleanViaGit.sh", srcFolder.getPath, tmpDir.getPath, projectName), curDir).!
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
        val exerciseFolder = new File(hiddenFolder, exercise.getName)
        sbtio.move(exercise, exerciseFolder)
        zipSolution(exerciseFolder, removeOriginal = true)
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
    println(s"Setting student repository bookmark to $firstExercise")
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
    sbtio.copy(moves, overwrite = true)

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
  }

  def cleanUp(files: Seq[String], targetFolder: File): Unit = {
    for (file <- files) {
      sbtio.delete(new File(targetFolder, file))
    }
  }

  def exitIfGitIndexOrWorkspaceIsntClean(masterRepo: File): Unit = {
    """git diff-index --quiet HEAD --"""
      .toProcessCmd(workingDir = masterRepo)
      .runAndExitIfFailed(s"YOU HAVE UNCOMMITTED CHANGES IN YOUR GIT INDEX. COMMIT CHANGES AND RE-RUN STUDENTIFY")

    s"""./checkIfWorkspaceClean.sh ${masterRepo.getPath}"""
      .toProcessCmd(workingDir = new File("."))
      .runAndExitIfFailed(s"YOU HAVE CHANGES IN YOUR GIT WORKSPACE. COMMIT CHANGES AND RE-RUN STUDENTIFY")
  }

  def printErrorMsgAndExit(masterConfigurationFile: File, lineNr: Option[Int], setting: String): Unit = {
    val LineNrInfo = if (lineNr.isDefined) s"on line ${lineNr.get+1}" else ""
    println(
      s"""Invalid setting syntax $LineNrInfo in ${masterConfigurationFile.getName}:
         |  $setting
               """.stripMargin)
    System.exit(-1)
  }

  def writeTestCodeFolders(settings: String, targetFolder: File, defaultSettings: String): Unit = {
    val finalSettings = settings.split(":").toSet ++ defaultSettings.split(":")
    dumpStringToFile(
      s"""package sbtstudent
         |
         |object TestFolders {
         |  val testFolders = List(${finalSettings.map(s => s""""$s"""").mkString(", ")})
         |}
       """.stripMargin, new File(targetFolder, "project/TestFolders.scala").getPath)
  }

  def loadStudentSettings(masterRepo: File, targetFolder: File): Map[String, String] = {
    val DefaultStudentSettings = Map(
      "TestCodeFolders" -> "src/test"
    )

    val studentSettingsFile = new File(masterRepo, Settings.studentSettingsFile)
    val settings = if (studentSettingsFile.exists()) {
      val SettingsLine = """([^=\s]+)\s*=\s*([^=\s]+)\s*""".r
      sbtio.readLines(studentSettingsFile).zipWithIndex.map { case (setting, lineNr) =>
        try {
          val SettingsLine(key, value) = setting
          (key, value)
        } catch {
          case me: MatchError =>
            printErrorMsgAndExit(studentSettingsFile, Some(lineNr), setting)
            ("key", "value") // Make this type-check
        }
      }.toMap
    } else {
      Map.empty[String, String]
    }
    for {
      (settingsKey, setting) <- DefaultStudentSettings
    } {
      settingsKey match {
        case key @ "TestCodeFolders" =>
          val s = settings.getOrElse(key, DefaultStudentSettings(key))
          writeTestCodeFolders(s, targetFolder, DefaultStudentSettings(key))
      }
    }
    // TODO: check for mistyped setting keys in student settings file...
    settings
  }
}
