package sbtstudent

/**
 * Copyright © 2014 - 2017 Lightbend, Inc. All rights reserved. [http://www.lightbend.com]
 */

import sbt.Keys._
import sbt.{Def, _}

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

  def extractCurrentExerciseDesc(state: State): String = {
    val currentExercise = IO.readLines(new sbt.File(new sbt.File(Project.extract(state).structure.root), ".bookmark")).head

    currentExercise
      .replaceFirst("""^.*_\d{3}_""", "")
      .replaceAll("_", " ")
  }

  def extractProjectName(state: State): String = {
    IO.readLines(new sbt.File(new sbt.File(Project.extract(state).structure.root), ".courseName")).head
  }

  override lazy val projectSettings: Seq[Def.Setting[State => String]] =
    Seq(
      shellPrompt := { state =>
        val promptCourseNameColor = SSettings.consoleColorMap(SSettings.promptCourseNameColor)
        val promptExerciseColor = SSettings.consoleColorMap(SSettings.promptExerciseColor)
        val promptManColor = SSettings.consoleColorMap(SSettings.promptManColor)
        val exercise = promptExerciseColor + extractCurrentExerciseDesc(state) + Console.RESET
        val manRmnd = promptManColor + "man [e]" + Console.RESET
        val prjNbrNme = promptCourseNameColor + extractProjectName(state) + Console.RESET

        s"$manRmnd > $prjNbrNme > $exercise > "
      }
    )
}