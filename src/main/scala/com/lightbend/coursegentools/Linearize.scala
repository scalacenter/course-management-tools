package com.lightbend.coursegentools

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

object Linearize {

  def main(args: Array[String]): Unit = {

    import Helpers._
    import java.io.File
    import sbt.io.{ IO => sbtio }

    implicit val eofe: ExitOnFirstError = ExitOnFirstError(true)

    val cmdOptions = LinearizeCmdLineOptParse.parse(args)
    if (cmdOptions.isEmpty) System.exit(-1)
    val LinearizeCmdOptions(masterRepo, linearizedOutputFolder, multiJVM, forceDeleteExistingDestinationFolder, configurationFile) = cmdOptions.get

    implicit val config: MasterSettings = new MasterSettings(masterRepo, configurationFile)

    exitIfGitIndexOrWorkspaceIsntClean(masterRepo)

    val projectName = masterRepo.getName
    val exercises: Seq[String] = getExerciseNames(masterRepo)

    val destinationFolder = new File(linearizedOutputFolder, projectName)

    (destinationFolder.exists(), forceDeleteExistingDestinationFolder) match {
      case (true, false) =>
        printError(
          """
             |Destination folder ${destinationFolder.getPath} exists: Either remove this folder
             |manually or use the '-f' command-line option to delete it automatically
             |""".stripMargin)
      case (true, true) =>
        sbtio.delete(destinationFolder)
      case _ =>

    }

    val tmpDir = cleanMasterViaGit(masterRepo, projectName)
    val cleanMasterRepo = new File(tmpDir, projectName)
    printNotification(s"Cleaned master repo: $cleanMasterRepo")
    val relativeCleanMasterRepo = new File(cleanMasterRepo, config.relativeSourceFolder)
    val linearizedProject = new File(linearizedOutputFolder, projectName)
    val sbtStudentCommandsTemplateFolder = new File("sbtStudentCommands")

    copyMaster(cleanMasterRepo, linearizedProject)
    createStudentifiedBuildFile(linearizedProject, multiJVM)
    createBookmarkFile(exercises.head, linearizedProject)
    addSbtStudentCommands(sbtStudentCommandsTemplateFolder, linearizedProject)
    loadStudentSettings(masterRepo, linearizedProject)
    cleanUp(List(".git", "navigation.sbt"), linearizedProject)

    removeExercisesFromCleanMaster(linearizedProject, exercises)
    stageFirstExercise(exercises.head, relativeCleanMasterRepo, linearizedProject)
    initializeGitRepo(linearizedProject)
    commitFirstExercise(exercises.head, linearizedProject)
    commitRemainingExercises(exercises.tail, cleanMasterRepo, linearizedProject)

    sbtio.delete(tmpDir)
  }
}
