package sbtstudent

import sbt._
import Keys._
import sbt.complete.DefaultParsers._
import scala.Console
import StudentKeys._
import Zip.withZipFile

object Pssr {
  lazy val restoreArg = OptSpace ~> StringBasic.?

  def getCurrentExercise(state: State): String = {
    val key: AttributeKey[File] = AttributeKey[File](bookmarkKeyName)
    val bookmarkFile: Option[File] = state get key
    val mark: String = IO.readLines(bookmarkFile.get).head
    mark
  }

  def bookmark(toPrjNme: String, state: State): Unit = {
    val key: AttributeKey[File] = AttributeKey[File](bookmarkKeyName)
    val bookmarkFile: Option[File] = state get key
    IO.write(bookmarkFile.get, toPrjNme)
  }

  def getRootFolder(state: State): File = {
    new sbt.File(Project.extract(state).structure.root)
  }

  def getCueFolder(state: State): File = {
    new sbt.File(getRootFolder(state), ".cue")
  }

  def getSavedStateFolder(state: State): File = {
    val savedExercises = new sbt.File(getCueFolder(state), ".savedExercises")
    if (!savedExercises.exists()) IO.createDirectory(savedExercises)
    savedExercises
  }

  def pullSolution: Command = Command.command("pullSolution") { state =>
    val currentExercise = getCurrentExercise(state)
    withZipFile(state, currentExercise) { () =>
      val currentExerciseMain = new sbt.File(getRootFolder(state), SSettings.studentifiedBaseFolder)
      val cueFolder = getCueFolder(state)
      val solFolder = new sbt.File(cueFolder, currentExercise)
      IO.delete(currentExerciseMain)
      IO.copyDirectory(solFolder, currentExerciseMain)
      Console.println(Console.GREEN + "[INFO] " + Console.RESET + s"Solution for exercise ${Console.YELLOW}$currentExercise ${Console.RESET}pulled successfully")
      state
    }
  }

  def saveState: Command = Command.command("saveState") { state =>
    val currentExercise = getCurrentExercise(state)
    val saveFolder = new sbt.File(getSavedStateFolder(state), currentExercise)
    val currentExerciseSrc = new sbt.File(getRootFolder(state), SSettings.studentifiedBaseFolder)
    if (saveFolder exists) IO.delete(saveFolder)
    IO.copyDirectory(currentExerciseSrc, saveFolder)
    Console.println(Console.GREEN + "[INFO] " + Console.RESET + s"State for exercise " + Console.YELLOW + currentExercise + Console.RESET + " saved successfully")
    state
  }

  def restoreState: Command = Command("restoreState")(s => restoreArg) { (state, arg) =>
    arg match {
      case Some(exercise) =>
        val savedFolder = new sbt.File(getSavedStateFolder(state), exercise)
        if (savedFolder.exists()) {
          val savedStateFolder = savedFolder
          val currentExerciseSrc = new sbt.File(getRootFolder(state), SSettings.studentifiedBaseFolder)
          IO.delete(currentExerciseSrc)
          IO.copyDirectory(savedStateFolder, currentExerciseSrc)
          bookmark(exercise, state)
          Console.println(Console.GREEN + "[INFO] " + Console.RESET + s"Exercise " + Console.YELLOW + exercise + Console.RESET + " restored")
        } else {
          Console.println(Console.RED + "[ERROR] " + Console.YELLOW + s"$exercise" + Console.RESET + s": no saved state with this name")
        }
        state
      case None =>
        Console.println(Console.YELLOW + "[WARN] " + Console.RESET + s"Provide name of state to restore")
        state
    }
  }

  def savedStates: Command = Command.command("savedStates") { state =>
    val savedState = getSavedStateFolder(state)
    val savedExercises = IO.listFiles(savedState).map(_.getName).toList.sorted
    if (savedExercises.isEmpty) {
      Console.println(Console.YELLOW + "[WARN] " + Console.RESET + s"No previously saved exercise states found")
    } else {
      Console.println(
        s"""${Console.GREEN}[INFO]${Console.RESET} Saved exercise states are available for the following exercise(s):${Console.YELLOW}
          |${savedExercises.mkString("        ", "\n        ", "")}${Console.RESET}""".stripMargin)
    }

    val newState: State = state
    newState
  }

  def showExerciseId: Command = Command.command("showExerciseId") { state =>
    val currentExercise = getCurrentExercise(state)
    Console.println(s"""${Console.GREEN}[INFO]${Console.RESET} Currently at ${Console.YELLOW}${currentExercise}${Console.RESET}""")
    state
  }

  def pullTemplate: Command = Command("pullTemplate")(s => restoreArg) { (state, arg) =>
    arg match {
      case Some(templateFilePath) =>
        //Todo: make this function more user-friendly allowing for specifiying just the (unique) filename iso package/filename
        val currentExercise = getCurrentExercise(state)
        withZipFile(state, currentExercise) { () =>
          val currentExerciseMain = new sbt.File(getRootFolder(state), SSettings.studentifiedBaseFolder + "/src/main")
          val cueFolder = getCueFolder(state)
          val solFolder = new sbt.File(cueFolder, currentExercise + "/src/main/")
          val templateFileSrc = new File(solFolder, templateFilePath)
          val templateFileDst = new File(currentExerciseMain, templateFilePath)
          if (templateFileSrc.exists && templateFileSrc.isFile) {
            IO.copyFile(templateFileSrc, templateFileDst)
            Console.println(Console.GREEN + "[INFO] " + Console.RESET + s"Exercise " + Console.YELLOW + templateFilePath + Console.RESET + " pulled")
          } else {
            Console.println(Console.RED + "[ERROR] " + Console.YELLOW + s"$templateFilePath" + Console.RESET + s": no such template file")
          }
          state
        }
      case None =>
        Console.println(Console.YELLOW + "[WARN] " + Console.RESET + s"Provide name of template file to pull")
        state
    }
  }
}
