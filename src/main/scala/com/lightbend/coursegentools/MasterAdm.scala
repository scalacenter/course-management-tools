package com.lightbend.coursegentools

import com.lightbend.coursegentools.Helpers.getExerciseNames

object MasterAdm {

  def main(args: Array[String]): Unit = {
    import Helpers._
    import java.io.File
    import sbt.io.{IO => sbtio}

    val cmdOptions = MasterAdmCmdLineOptParse.parse(args)
    if (cmdOptions.isEmpty) System.exit(-1)
    val MasterAdmCmdOptions(masterRepo, multiJVM, regenBuildFile, configurationFile) = cmdOptions.get

    implicit val config: MasterSettings = new MasterSettings(masterRepo, configurationFile)

    val exercises: Seq[String] = getExerciseNames(masterRepo)
    val ExerciseNumberSpec = """exercise_(\d{3})_.*""".r
    val exerciseNumbers = exercises.map{ s => val ExerciseNumberSpec(d) = s; d.toInt }

    (regenBuildFile) match {
      case true =>
        createMasterBuildFile(exercises, masterRepo, multiJVM)


      case _ => println(s"Nothing to do...")

    }

  }


}
