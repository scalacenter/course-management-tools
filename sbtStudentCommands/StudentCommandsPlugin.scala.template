package sbtstudent

import sbt._
import Keys._
import scala.Console
import scala.util.matching._

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
            Navigation.listExercises, Navigation.gotoFirstExercise,
            Pssr.pullSolution, Pssr.pullTemplate, Pssr.restoreState,
            Pssr.savedStates, Pssr.saveState, Pssr.showExerciseId
          ),
      onLoad in Global := {
        val state = (onLoad in Global).value
        Navigation.loadBookmark compose(Navigation.setupNavAttrs compose state)
      }
    )

  override lazy val projectSettings =
    Seq(
      shellPrompt := { state =>
        val base: File = Project.extract(state).get(sourceDirectory)
        val basePath: String = base + "/test/resources/README.md"
        val exercise = Console.GREEN + IO.readLines(new sbt.File(basePath)).head + Console.RESET
        val manRmnd = Console.RED + "man [e]" + Console.RESET
        val prjNbrNme = IO.readLines(new sbt.File(new sbt.File(Project.extract(state).structure.root), ".courseName")).head
        s"$manRmnd > $prjNbrNme > $exercise > "
      }
    )
}