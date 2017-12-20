package com.lightbend.coursegentools

import sbt.io.{IO => sbtio}
import java.io.File
import scala.sys.process.Process
import scala.Console

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

  def putBackToMaster(masterRepo: File, linearizedRepo: File, exercisesAndSHAs: Vector[ExNameAndSHA])(implicit config: MasterSettings): Unit = {

    val masterRepoRelative = new File(masterRepo, config.relativeSourceFolder)

    for (ExNameAndSHA(exercise, sha) <- exercisesAndSHAs) {
      s"git checkout $sha"
        .toProcessCmd(linearizedRepo)
        .runAndExitIfFailed(toConsoleRed(s"Unable to checkout commit($sha) corresponding to exercise: $exercise"))

      sbtio.delete(new File(masterRepoRelative, exercise))
      sbtio.copyDirectory(new File(linearizedRepo, config.studentifyModeClassic.studentifiedBaseFolder), new File(masterRepoRelative, exercise), preserveLastModified = true)
    }

    s"git checkout master".toProcessCmd(linearizedRepo).runAndExitIfFailed(toConsoleRed(s"Unable to checkout master in linearized repo"))
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
      println(toConsoleRed(s"Repos are incompatible"))
      System.exit(-1)
    }
  }

  def commitRemainingExercises(exercises: Seq[String], masterRepo: File, linearizedProject: File)(implicit config: MasterSettings): Unit = {
    val exercisesDstFolder = new File(linearizedProject, config.studentifyModeClassic.studentifiedBaseFolder)
    val masterRepoRelative = new File(masterRepo, config.relativeSourceFolder)
    for { exercise <- exercises } {
      val from = new File(masterRepoRelative, exercise)
      sbtio.delete(exercisesDstFolder)
      sbtio.copyDirectory(from, exercisesDstFolder, preserveLastModified = true)

      s"git add -A"
        .toProcessCmd(workingDir = linearizedProject)
        .runAndExitIfFailed(toConsoleRed(s"Failed to add exercise files for exercise $exercise"))

      s"git commit -m $exercise"
        .toProcessCmd(workingDir = linearizedProject)
        .runAndExitIfFailed(toConsoleRed(s"Failed to add exercise files for exercise $exercise"))
     }
  }

  def commitFirstExercise(exercise: String, linearizedProject: File): Unit = {
    s"git add -A"
      .toProcessCmd(workingDir = linearizedProject)
      .runAndExitIfFailed(toConsoleRed(s"Failed to add first exercise files"))

    s"git commit -m $exercise"
      .toProcessCmd(linearizedProject)
      .runAndExitIfFailed(toConsoleRed(s"Failed to commit exercise $exercise files"))
  }

  def initializeGitRepo(linearizedProject: File): Unit = {
    s"git init"
      .toProcessCmd(linearizedProject)
      .runAndExitIfFailed(toConsoleRed(s"Failed to initialize linearized git repository in ${linearizedProject.getPath}"))
  }

  def removeExercisesFromCleanMaster(cleanMasterRepo: File, exercises: Seq[String])(implicit config: MasterSettings): Unit = {
    val cleanMasterRepoRelative = new File(cleanMasterRepo, config.relativeSourceFolder)
    for {
      exercise <- exercises
    } {
      val exerciseFolder = new File(cleanMasterRepoRelative, exercise)
      if (exerciseFolder.exists()) {
        sbtio.delete(exerciseFolder)
      } else {
        println(toConsoleRed(s"Error in removeExercisesFromCleanMaster, bailing out"))
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

  def copyMaster(masterRepo: File, targetFolder: File)(implicit config: MasterSettings): Unit = {
    val relativeSourceFolder = new File(masterRepo, config.relativeSourceFolder)
    sbtio.copyDirectory(relativeSourceFolder, targetFolder, overwrite = false, preserveLastModified = true)
  }

  def getExerciseNames(cleanMasterRepo: File, masterRepo: Option[File] = None)(implicit config: MasterSettings): Vector[String] = {
    val relativeSourceFolder = new File(cleanMasterRepo, config.relativeSourceFolder)
    val exerciseFolders = sbtio.listFiles(relativeSourceFolder, FoldersOnly()).filter(isExerciseFolder)
    val exercisesNames = exerciseFolders.map(folder => folder.getName).toVector.sorted
    if (exercisesNames.isEmpty) {
      val repo = masterRepo.getOrElse(cleanMasterRepo)
      println(s"${Console.RED}ERROR: No exercises found in ${new File(repo, config.relativeSourceFolder)}${Console.RESET}")
      System.exit(-1)
    }
    exercisesNames
  }

  def hideExerciseSolutions(targetFolder: File, selectedExercises: Seq[String])(implicit config: MasterSettings): Unit = {
    val hiddenFolder = new File(targetFolder, config.solutionsFolder)
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
      println(toConsoleRed(s"Exercise on start not in selected range of exercises"))
      System.exit(-1)
      selectedExercise
    }
  }
  def stageFirstExercise(firstEx: String, masterRepo: File, targetFolder: File)(implicit config: MasterSettings): Unit = {
    //val relativeSourceFolder = new File(masterRepo, config.relativeSourceFolder)
    val firstExercise = new File(masterRepo, firstEx)
    sbtio.copyDirectory(firstExercise, new File(targetFolder, config.studentifyModeClassic.studentifiedBaseFolder), preserveLastModified = true)
  }

  def createBookmarkFile(firstExercise: String, targetFolder: File): Unit = {
    println(s"Setting student repository bookmark to $firstExercise")
    dumpStringToFile(firstExercise, new File(targetFolder, ".bookmark").getPath)
  }

  def createSbtRcFile(targetFolder: File)(implicit config: MasterSettings): Unit = {
    dumpStringToFile(s"alias boot = ;reload ;project ${config.studentifyModeClassic.studentifiedBaseFolder} ;iflast shell", new File(targetFolder, ".sbtrc").getPath)
  }

  def addSbtStudentCommands(sbtStudentCommandsTemplateFolder: File, targetCourseFolder: File): Unit = {
    val projectFolder = new File(targetCourseFolder, "project")
    val moves = for {
      template <- sbtio.listFiles(sbtStudentCommandsTemplateFolder)
      target = new File(projectFolder, template.getName.replaceAll(".scala.template", ".scala"))
    } yield (template, target)
    sbtio.copy(moves, overwrite = true, preserveLastModified = true, preserveExecutable = false)

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
      println(toConsoleRed(s"Invalid exercise selection"))
      System.exit(-1)
    }
    selExcs
  }

  def createMasterBuildFile(exercises: Seq[String],
                            masterRepo: File,
                            multiJVM: Boolean)(implicit config: MasterSettings): Unit = {

    val targetFolder = new File(masterRepo, config.relativeSourceFolder)

    def exerciseDep(exercise: String): String = {
      s"""lazy val $exercise = project
         |  .settings(CommonSettings.commonSettings: _*)
         |  .dependsOn(common % "test->test;compile->compile")""".stripMargin
    }

    def exerciseMJvmDep(exercise: String): String = {
      s"""lazy val $exercise = project
         |  .settings(SbtMultiJvm.multiJvmSettings: _*)
         |  .settings(CommonSettings.commonSettings: _*)
         |  .dependsOn(common % "test->test;compile->compile")
         |  .configs(MultiJvm)""".stripMargin
    }
    val exerciseList = exercises.mkString(",\n    ")

    val buildDefinition = if (multiJVM) {
      s"""
         |lazy val ${config.masterBaseProjectName} = (project in file("."))
         |  .aggregate(
         |    common,
         |    $exerciseList
         | ).settings(SbtMultiJvm.multiJvmSettings: _*)
         |  .settings(CommonSettings.commonSettings: _*)
         |  .configs(MultiJvm)
         |
         |lazy val common = project
         |  .settings(SbtMultiJvm.multiJvmSettings: _*)
         |  .settings(CommonSettings.commonSettings: _*)
         |  .configs(MultiJvm)
         |
         |${exercises.map{exrc => exerciseMJvmDep(exrc)}.mkString("\n\n")}
       """.stripMargin
    } else {
      s"""
         |lazy val ${config.masterBaseProjectName} = (project in file("."))
         |  .aggregate(
         |    common,
         |    $exerciseList
         | ).settings(CommonSettings.commonSettings: _*)
         |
         |lazy val common = project.settings(CommonSettings.commonSettings: _*)
         |
         |${exercises.map{exrc => exerciseDep(exrc)}.mkString("\n\n")}
       """.stripMargin
    }

    dumpStringToFile(buildDefinition, new File(targetFolder, "build.sbt").getPath)

  }

  def createBuildFile(targetFolder: File, multiJVM: Boolean)(implicit config: MasterSettings): Unit = {
    val buildDef =
      s"""
         |import sbt._
         |
         |lazy val ${config.studentifiedProjectName} = (project in file("."))
         |  .aggregate(
         |    common,
         |    ${config.studentifyModeClassic.studentifiedBaseFolder}
         |  )
         |  .settings(CommonSettings.commonSettings: _*)
         |lazy val common = project.settings(CommonSettings.commonSettings: _*)
         |
         |lazy val ${config.studentifyModeClassic.studentifiedBaseFolder} = project
         |  .settings(CommonSettings.commonSettings: _*)
         |  .dependsOn(common % "test->test;compile->compile")
       """.stripMargin

    val mJvmBuildDef =
      s"""
         |import sbt._
         |
         |lazy val ${config.studentifiedProjectName} = (project in file("."))
         |  .aggregate(
         |    common,
         |    ${config.studentifyModeClassic.studentifiedBaseFolder}
         |  )
         |  .settings(SbtMultiJvm.multiJvmSettings: _*)
         |  .settings(CommonSettings.commonSettings: _*)
         |  .configs(MultiJvm)
         |
         |lazy val common = project
         |  .settings(SbtMultiJvm.multiJvmSettings: _*)
         |  .settings(CommonSettings.commonSettings: _*)
         |  .configs(MultiJvm)
         |
         |lazy val ${config.studentifyModeClassic.studentifiedBaseFolder} = project
         |  .settings(SbtMultiJvm.multiJvmSettings: _*)
         |  .settings(CommonSettings.commonSettings: _*)
         |  .configs(MultiJvm)
         |  .dependsOn(common % "test->test;compile->compile")
       """.stripMargin

      if (multiJVM) {
        dumpStringToFile(mJvmBuildDef, new File(targetFolder, "build.sbt").getPath)
      } else {
        dumpStringToFile(buildDef, new File(targetFolder, "build.sbt").getPath)
      }
  }

  def cleanUp(files: Seq[String], targetFolder: File): Unit = {
    for (file <- files) {
      sbtio.delete(new File(targetFolder, file))
    }
  }

  def exitIfGitIndexOrWorkspaceIsntClean(masterRepo: File): Unit = {
    """git diff-index --quiet HEAD --"""
      .toProcessCmd(workingDir = masterRepo)
      .runAndExitIfFailed(toConsoleRed(s"YOU HAVE UNCOMMITTED CHANGES IN YOUR GIT INDEX. COMMIT CHANGES AND RE-RUN STUDENTIFY"))

    s"""./checkIfWorkspaceClean.sh ${masterRepo.getPath}"""
      .toProcessCmd(workingDir = new File("."))
      .runAndExitIfFailed(toConsoleRed(s"YOU HAVE CHANGES IN YOUR GIT WORKSPACE. COMMIT CHANGES AND RE-RUN STUDENTIFY"))
  }

  def printErrorMsgAndExit(masterConfigurationFile: File, lineNr: Option[Int], setting: String): Unit = {
    val LineNrInfo = if (lineNr.isDefined) s"on line ${lineNr.get+1}" else ""
    println(
      s"""Invalid setting syntax $LineNrInfo in ${masterConfigurationFile.getName}:
         |  $setting
               """.stripMargin)
    System.exit(-1)
  }

  def loadStudentSettings(masterRepo: File, targetFolder: File)(implicit masterSettings: MasterSettings): Unit = {

    dumpStringToFile(
      s"""package sbtstudent
         |
         |object SSettings {
         |  import scala.Console._
         |  val consoleColorMap: Map[String, String] =
         |    Map("RESET" -> RESET, "GREEN" -> GREEN, "RED" -> RED, "BLUE" -> BLUE, "CYAN" -> CYAN, "YELLOW" -> YELLOW, "WHITE" -> WHITE, "BLACK" -> BLACK, "MAGENTA" -> MAGENTA)
         |
         |  val testFolders = List(${masterSettings.testCodeFolders.map(s => s""""$s"""").mkString(", ")})
         |
         |  val studentifiedBaseFolder = "${masterSettings.studentifyModeClassic.studentifiedBaseFolder}"
         |
         |  val promptManColor         = "${masterSettings.Colors.promptManColor}"
         |  val promptExerciseColor    = "${masterSettings.Colors.promptExerciseColor}"
         |  val promptCourseNameColor = "${masterSettings.Colors.promptCourseNameColor}"
         |}
       """.stripMargin, new File(targetFolder, "project/SSettings.scala").getPath)
  }
}
