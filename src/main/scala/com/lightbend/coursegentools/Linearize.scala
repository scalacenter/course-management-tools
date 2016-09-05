package com.lightbend.coursegentools

/**
  * Copyright © 2014, 2015, 2016 Lightbend, Inc. All rights reserved. [http://www.lightbend.com]
  */

object Linearize {

  def main(args: Array[String]): Unit = {

    import Helpers._
    import java.io.File

    val cmdOptions = LinearizeCmdLineOptParse.parse(args)
    if (cmdOptions.isEmpty) System.exit(-1)
    val LinearizeCmdOptions(masterRepo, linearizedOutputFolder, multiJVM) = cmdOptions.get

    val projectName = masterRepo.getName
    val exercises: Seq[String] = getExerciseNames(masterRepo)

    val tmpDir = cleanMasterViaGit(masterRepo, projectName)
    val cleanMasterRepo = new File(tmpDir, projectName)
    println(s"cleanMasterRepo @ ${cleanMasterRepo.getPath}")
    stageFirstExercise(exercises.head, cleanMasterRepo, cleanMasterRepo)
    removeExercisesFromCleanMaster(cleanMasterRepo, exercises)
    val linearizedProject = new File(linearizedOutputFolder, projectName)
    copyMaster(cleanMasterRepo, linearizedProject)
    sbt.IO.delete(tmpDir)
    createBuildFile(linearizedProject, multiJVM)
    cleanUp(List(".git", "navigation.sbt"), linearizedProject)
    initializeGitRepo(linearizedProject)
    commitFirstExercise(exercises.head, linearizedProject)
    commitRemainingExercises(exercises.tail, masterRepo, linearizedProject)
  }
}
