package sbtstudent

/**
 * Copyright © 2014 - 2017 Lightbend, Inc. All rights reserved. [http://www.lightbend.com]
 */

import sbt.Keys._
import sbt._

import scala.Console

object StudentCommandsPlugin extends AutoPlugin {
  override val requires = sbt.plugins.JvmPlugin
  override val trigger = allRequirements
  object autoImport {
  }
  override lazy val globalSettings =
    Seq(
      commands in Global ++=
          Seq(
            Man.man,
            Navigation.nextExercise, Navigation.prevExercise, Navigation.gotoExerciseNr,
            Navigation.listExercises, Navigation.gotoFirstExercise, Navigation.gotoExercise,
            Pssr.pullSolution, Pssr.pullTemplate, Pssr.restoreState,
            Pssr.savedStates, Pssr.saveState, Pssr.showExerciseId
          ),
      onLoad in Global := {
        val state = (onLoad in Global).value
        state andThen Navigation.loadBookmark andThen Navigation.setupNavAttrs andThen Navigation.getAllExercises
      }
    )

  override lazy val projectSettings =
    Seq(
      shellPrompt := { state =>
        val promptCourseNameColor = SSettings.consoleColorMap(SSettings.promptCourseNameColor)
        val promptExerciseColor = SSettings.consoleColorMap(SSettings.promptExerciseColor)
        val promptManColor = SSettings.consoleColorMap(SSettings.promptManColor)
        val base: File = Project.extract(state).get(sourceDirectory)
        val basePath: String = base + "/test/resources/README.md"
        val exercise = promptExerciseColor + IO.readLines(new sbt.File(basePath)).head + Console.RESET
        val manRmnd = promptManColor + "man [e]" + Console.RESET
        val prjNbrNme = promptCourseNameColor + IO.readLines(new sbt.File(new sbt.File(Project.extract(state).structure.root), ".courseName")).head + Console.RESET
        s"$manRmnd > $prjNbrNme > $exercise > "
      }
    )
}