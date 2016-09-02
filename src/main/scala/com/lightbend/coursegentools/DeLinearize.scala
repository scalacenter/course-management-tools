package com.lightbend.coursegentools

/**
  * Copyright © 2014, 2015, 2016 Lightbend, Inc. All rights reserved. [http://www.lightbend.com]
  */

object DeLinearize {
  def main(args: Array[String]): Unit = {

    import Helpers._

    val cmdOptions = DeLinearizeCmdLineOptParse.parse(args)
    if (cmdOptions.isEmpty) System.exit(-1)
    val DeLinearizeCmdOptions(linearizedRepo, masterRepo) = cmdOptions.get

    val exercisesInMaster = getExerciseNames(masterRepo)
    val exercisesAndSHAs = getExercisesAndSHAs(linearizedRepo)
    checkReposMatch(exercisesInMaster, exercisesAndSHAs)
    putBackToMaster(masterRepo, linearizedRepo, exercisesAndSHAs)
  }
}
