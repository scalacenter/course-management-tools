package sbtstudent

/**
  * Copyright © 2014 - 2017 Lightbend, Inc. All rights reserved. [http://www.lightbend.com]
  */

import java.io.File

import sbt.Keys._
import sbt._
import sbt.complete.DefaultParsers.{IntBasic, Space}
import sbt.complete.Parser
import sbtstudent.StudentKeys._
import sbtstudent.Zip.withZipFile

import scala.Console

object Navigation {

  def mapExercisesFromFolders(state: State, reverse: Boolean = false): Map[String, String] = {
    object FoldersOnly {
      def apply() = new FoldersOnly
    }
    class FoldersOnly extends java.io.FileFilter {
      override def accept(f: File): Boolean = f.isDirectory
    }

    val solF = new sbt.File(new sbt.File(Project.extract(state).structure.root), ".cue")
    val ExerciseNameSpec = """.*_\d{3}_\w+\.zip$""".r

    def isExerciseFolder(folder: File): Boolean = {
      ExerciseNameSpec.findFirstIn(folder.getPath).isDefined
    }
    val refs = IO.listFiles(solF).filter(isExerciseFolder).map(_.getName.replaceAll(".zip", "")).toList.sorted
    val refsReordered = if (reverse) refs.reverse else refs
    val mapping = (refsReordered zip refsReordered.tail) :+ (refsReordered.last, refsReordered.last)
    mapping.toMap
  }

  def cueFolderExists(state: State): Boolean = {
    val cueFolder = new sbt.File(new sbt.File(Project.extract(state).structure.root), ".cue")
    cueFolder.exists()
  }

  def mapExercises(state: State, reverse: Boolean = false): Map[String, String] =
    mapExercisesFromFolders(state, reverse)

  val getAllExercises: (State) => State = (s: State) => {
    val ex = mapExercises(s).keys.toList.map(str => str: Parser[String])
    s.put(allExercises, Space ~ Parser.oneOf(ex))
  }

  val setupNavAttrs: State => State = (s: State) => {

    val mark: File = s get bookmark getOrElse new sbt.File(new sbt.File(Project.extract(s).structure.root), ".bookmark")
    val prev: Map[String, String] = s get mapPrev getOrElse mapExercises(s, reverse = true)
    val next: Map[String, String] = s get mapNext getOrElse mapExercises(s)
    s.put(bookmark, mark).put(mapPrev, prev).put(mapNext, next)
  }
  val loadBookmark: (State) => State = (state: State) => {
    if (cueFolderExists(state)) {
      state
    } else {
      val key: AttributeKey[File] = AttributeKey[File](bookmarkKeyName)
      val bookmarkFile: Option[File] = state get key
      try {
        val mark: String = IO.read(bookmarkFile.get).trim()
        val cmd: String = s"project $mark"
        val newState = cmd :: state
        newState
      } catch {
        case e: java.io.FileNotFoundException => state
      }
    }
  }

  def nextExercise = Command.command("nextExercise") { state =>
    val newState: State = move(mapNextKeyName, state, "next")
    newState
  }

  def prevExercise = Command.command("prevExercise") { state =>
    val newState: State = move(mapPrevKeyName, state, "prev")
    newState
  }

  def starCurrentExercise(currentExercise: String, exercise: String): String = {
    if (currentExercise == exercise) " * " else "   "
  }

  def listExercises: Command = Command.command("listExercises") { state =>
    val currentExercise = getExerciseName(state)
    mapExercises(state).toList.map(_._1).sorted.zipWithIndex
      .foreach { case (exName, seq) =>
        Console.println(Console.GREEN + f"${seq.toInt + 1}%3d." + starCurrentExercise(currentExercise, exName) + Console.RESET + s"$exName")
      }
    state
  }

  def copyExerciseReadme(newExerciseSrc: sbt.File, src: sbt.File): Unit = {
    if (! SSettings.readmeInTestResources)
      IO.copyFile(new File(newExerciseSrc, "README.md"), new File(src, "README.md"))
  }

  private lazy val ExerciseNr = Space ~> IntBasic.?

  def gotoFirstExercise: Command = Command.command("gotoFirstExercise") { state =>
    val firstExercise = state.get(mapNext).get.keys.toVector.sorted.head
    withZipFile(state, firstExercise) { () =>
      val src = new sbt.File(new sbt.File(Project.extract(state).structure.root), SSettings.studentifiedBaseFolder)
      val newExerciseSrc = new sbt.File(new sbt.File(Project.extract(state).structure.root), s".cue/$firstExercise")
      copyExerciseReadme(newExerciseSrc, src)
      for {
        f <- SSettings.testFolders
        fromFolder = new File(newExerciseSrc, f)
        toFolder = new File(src, f)
      } {
        IO.delete(toFolder)
        IO.copyDirectory(fromFolder, toFolder)
      }
      writeBookmark(firstExercise, state)
      Console.println(Console.GREEN + "[INFO] " + Console.RESET + s"Moved to first exercise in course")
      state
    }
  }

  val exerciseNameParser: (State) => Parser[(Seq[Char], String)] =
      (s: State) => s.get(AttributeKey[Parser[(Seq[Char], String)]](allExercisesKeyName)).get

  def gotoExercise: Command = Command("gotoExercise")(exerciseNameParser) { (state, arg) =>
    val (_, exercise) = arg
    withZipFile(state, exercise) { () =>
      val src = new sbt.File(new sbt.File(Project.extract(state).structure.root), SSettings.studentifiedBaseFolder)
      val newExerciseSrc = new sbt.File(new sbt.File(Project.extract(state).structure.root), s".cue/$exercise")
      copyExerciseReadme(newExerciseSrc, src)
      for {
        f <- SSettings.testFolders
        fromFolder = new File(newExerciseSrc, f)
        toFolder = new File(src, f)
      } {
        IO.delete(toFolder)
        IO.copyDirectory(fromFolder, toFolder)
      }
      writeBookmark(exercise, state)
      Console.println(s"""${Console.GREEN}[INFO]${Console.RESET} Moved to ${Console.YELLOW}${exercise}${Console.RESET}""")
      state
    }

  }

  def gotoExerciseNr: Command = Command("gotoExerciseNr")(s => ExerciseNr) { (state, arg) =>
    val ExerciseFmt = """.*_(\d{3})_\w+$""".r
    arg match {
      case Some(exerciseNr) =>
        val exercises =
          mapExercises(state)
            .toList
            .map(_._1)
            .filter { ex =>
              val ExerciseFmt(nr) = ex
              nr.toInt == exerciseNr
            }

        exercises match {
          case exercise +: Nil =>
            withZipFile(state, exercise) { () =>
              val src = new sbt.File(new sbt.File(Project.extract(state).structure.root), SSettings.studentifiedBaseFolder)
              val newExerciseSrc = new sbt.File(new sbt.File(Project.extract(state).structure.root), s".cue/$exercise")
              copyExerciseReadme(newExerciseSrc, src)
              for {
                f <- SSettings.testFolders
                fromFolder = new File(newExerciseSrc, f)
                toFolder = new File(src, f)
              } {
                IO.delete(toFolder)
                IO.copyDirectory(fromFolder, toFolder)
              }
              writeBookmark(exercise, state)
              Console.println(s"""${Console.GREEN}[INFO]${Console.RESET} Moved to ${Console.YELLOW}${exercise}${Console.RESET}""")
              state
            }

          case _ =>
            Console.println(Console.RED + "[ERROR] " + Console.YELLOW + s"$exerciseNr" + Console.RESET + s": No exercise with that number")
        }
        state

      case None =>
        Console.println(Console.YELLOW + "[WARNING] " + Console.RESET + s"Usage: gotoExerciseNr <ddd>")
        state
    }
  }

  def getExerciseName(state: State): String = {
    if (cueFolderExists(state)) {
      val key: AttributeKey[File] = AttributeKey[File](bookmarkKeyName)
      val bookmarkFile: Option[File] = state get key
      val mark: String = IO.readLines(bookmarkFile.get).head
      mark
    } else {
      Project.extract(state).get(name)
    }
  }

  def move(keyName: String, state: State, direction: String): State = {
    val attrKey = AttributeKey[Map[String, String]](keyName)
    val prjNme: String = getExerciseName(state)
    val moveMap: Option[Map[String, String]] = state get attrKey
    val toPrjNme: String = moveMap.get(prjNme)
    (toPrjNme, direction) match {
      case (`prjNme`, "prev") =>
        Console.println(Console.YELLOW + "[WARNING] " + Console.RESET + "You're already at the first exercise")
        state
      case (`prjNme`, "next") =>
        Console.println(Console.YELLOW + "[WARNING] " + Console.RESET + "You're already at the last exercise")
        state
      case (toProjectName, _) =>
        withZipFile(state, toProjectName) { () =>
          val src = new sbt.File(new sbt.File(Project.extract(state).structure.root), SSettings.studentifiedBaseFolder)
          val newExerciseSrc = new sbt.File(new sbt.File(Project.extract(state).structure.root), s".cue/$toProjectName")
          copyExerciseReadme(newExerciseSrc, src)
          for {
            f <- SSettings.testFolders
            fromFolder = new File(newExerciseSrc, f)
            toFolder = new File(src, f)
          } {
            IO.delete(toFolder)
            IO.copyDirectory(fromFolder, toFolder)
          }
          writeBookmark(toProjectName, state)
          Console.println(s"""${Console.GREEN}[INFO]${Console.RESET} Moved to ${Console.YELLOW}${toProjectName}${Console.RESET}""")
          state
        }
    }
  }

  def writeBookmark(toPrjNme: String, state: State): Unit = {
    val key: AttributeKey[File] = AttributeKey[File](bookmarkKeyName)
    val bookmarkFile: Option[File] = state get key
    IO.write(bookmarkFile.get, toPrjNme)
  }
}