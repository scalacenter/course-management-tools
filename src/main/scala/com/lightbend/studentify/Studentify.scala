package com.lightbend.studentify

/**
  * Copyright © 2014, 2015, 2016 Lightbend, Inc. All rights reserved. [http://www.lightbend.com]
  */

object Studentify {

  def main(args: Array[String]): Unit = {

    import Exercises._
    val cmdOptions = CmdLineOptParse.parse(args)
    if (cmdOptions.isEmpty) System.exit(-1)
    val CmdOptions(masterRepo, targetFolder) = cmdOptions.get


    val exercises: Seq[String] = getExerciseNames(masterRepo)
    stageFirstExercise(exercises.head, masterRepo, targetFolder)
    copyMaster(masterRepo, targetFolder)
    val solutionPaths = hideExerciseSolutions(targetFolder)
    createBookmarkFile(exercises, targetFolder)
    createBuildFile(targetFolder)
    cleanUp(List(".DS_Store", ".idea"), targetFolder)

  }

}
