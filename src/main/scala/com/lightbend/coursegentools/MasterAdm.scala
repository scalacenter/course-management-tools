package com.lightbend.coursegentools

object MasterAdm {
  def main(args: Array[String]): Unit = {

    import Helpers._
    import java.io.File
    import sbt.{IO => sbtIO}

    def changeExerciseNr(exerciseName: String, newNr: Int): String = {
      val ExerciseNameSpec(pre, d, post) = exerciseName
      f"$pre%s${newNr}%03d$post"
    }


    val cmdOptions = MasterAdmCmdLineOptParse.parse(args)
    if (cmdOptions.isEmpty) System.exit(-1)
    val MasterAdmCmdOption(masterRepo, multiJVM, renumber, renumberOffset, renumberDelta) = cmdOptions.get

    val tmpDir = sbtIO.createUniqueDirectory(masterRepo)
    val exercises: Vector[String] = getExerciseNames(masterRepo)
    val ExerciseNameSpec(_, firstExerciseNr, _) = exercises.head
    val (exercisesRenumbered: Vector[String], _) =
      (exercises foldLeft(Vector.empty[String], renumberOffset)) {
        case ((tally, newNr), exNameOld) =>
          val ExerciseNameSpec(pre, oldNr, post) = exNameOld
          (tally :+ f"$pre%s$newNr%03d$post%s", newNr + renumberDelta)
      }
    exercises.foreach { exercise => sbtIO.move(new File(masterRepo, exercise), new File(tmpDir, exercise)) }
    (exercises zip exercisesRenumbered).foreach {
      case (prevName, newName) =>
        sbtIO.move(new File(tmpDir, prevName), new File(masterRepo, newName))
    }

    sbtIO.delete(tmpDir)

    def exerciseProjects(exercises: Vector[String], multiJvm: Boolean): String = {
      (multiJVM) match {
        case (false) =>
          exercises.map {exercise =>
            s"""lazy val $exercise = project
               |  .settings(CommonSettings.commonSettings: _*)
               |  .dependsOn(common % "test->test;compile->compile")""".stripMargin
          }.mkString("", "\n", "\n")
        case (true) =>
          exercises.map {exercise =>
            s"""lazy val $exercise = project
               |  .settings(SbtMultiJvm.multiJvmSettings: _*)
               |  .settings(CommonSettings.commonSettings: _*)
               |  .configs(MultiJvm)
               |  .dependsOn(common % "test->test;compile->compile")
               |""".stripMargin
          }.mkString("", "\n", "\n")
      }
    }

    val buildDef =
      s"""
       |lazy val base = (project in file("."))
       |  .aggregate(
       |    common,
       |${exercisesRenumbered.mkString("    ", ",\n    ", "")}
       | )
       |  .settings(CommonSettings.commonSettings: _*)
       |${if (multiJVM)
         s"""  .settings(SbtMultiJvm.multiJvmSettings: _*)
            |  .configs(MultiJvm)""".stripMargin else ""}
            |
            |lazy val common = project.settings(CommonSettings.commonSettings: _*)
            |
            |${exerciseProjects(exercisesRenumbered, multiJVM)}""".stripMargin

    dumpStringToFile(buildDef, new File(masterRepo, "build.sbt") getPath)

    }
}
