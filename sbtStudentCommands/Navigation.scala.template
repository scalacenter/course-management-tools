/**
  * Copyright © 2014, 2015 Typesafe, Inc. All rights reserved. [http://www.typesafe.com]
  */
package sbtstudent

import java.io.File

import StudentKeys._
import sbt.Keys._
import Zip.withZipFile

import sbt._
import scala.Console

import sbt.complete.DefaultParsers.{OptSpace, IntBasic}

object Navigation {
  def mapExercisesFromProjects(state: State, reverse: Boolean = false): Map[String, String] = {
    val refs = Project.extract(state).structure.allProjectRefs.toList.map(r => r.project).filter(_.startsWith("exercise_")).sorted
    val refsReordered = if (reverse) refs.reverse else refs
    val mapping = (refsReordered zip refsReordered.tail) :+ (refsReordered.last, refsReordered.last)
    mapping.toMap
  }

  def mapExercisesFromFolders(state: State, reverse: Boolean = false): Map[String, String] = {
    object FoldersOnly {
      def apply() = new FoldersOnly
    }
    class FoldersOnly extends java.io.FileFilter {
      override def accept(f: File): Boolean = f.isDirectory
    }

    val solF = new sbt.File(new sbt.File(Project.extract(state).structure.root), ".cue")
    val ExerciseNameSpec = """.*exercise_[0-9][0-9][0-9]_\w+\.zip$""".r

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

  def mapExercises(state: State, reverse: Boolean = false): Map[String, String] = {
    if (cueFolderExists(state)) {
      mapExercisesFromFolders(state, reverse)
    } else {
      mapExercisesFromProjects(state, reverse)
    }
  }


  val setupNavAttrs: (State) => State = (s: State) => {

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
        val mark: String = IO.read(bookmarkFile.get)
        val cmd: String = s"project $mark"
        val newState = Command.process(cmd, state)
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

  def listExercises: Command = Command.command("listExercises") { state =>
    val ExFmt =
      s"""exercise_([0-9]+)_(.*)""".r
    val exList = mapExercises(state).toList.map(_._1).sorted.zipWithIndex
        .foreach { case (exName, seq) =>
          val ExFmt(exNr, exDesc)= exName
          val ed = f"Exercise ${exNr.toInt}%d > ${exDesc.split("_").map(s => s"${s.head.toUpper}${s.tail}").mkString(" ")}"
          Console.println(Console.RED + s"${seq.toInt}. " + Console.RESET + s"$ed")
        }
    state
  }

  private lazy val ExerciseNr = OptSpace ~> IntBasic.?

  def gotoFirstExercise: Command = Command.command("gotoFirstExercise") { state =>
    val firstExercise = state.get(mapNext).get.keys.toVector.sorted.head
    withZipFile(state, firstExercise) { () =>
      val src = new sbt.File(new sbt.File(Project.extract(state).structure.root), "exercises")
      val newExerciseSrc = new sbt.File(new sbt.File(Project.extract(state).structure.root), s".cue/$firstExercise")
      for {
        f <- TestFolders.testFolders
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

  def gotoExerciseNr: Command = Command("gotoExerciseNr")(s => ExerciseNr) { (state, arg) =>
    val ExerciseFmt = """.*exercise_([0-9][0-9][0-9])_\w+$""".r
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
              val src = new sbt.File(new sbt.File(Project.extract(state).structure.root), "exercises")
              val newExerciseSrc = new sbt.File(new sbt.File(Project.extract(state).structure.root), s".cue/$exercise")
              for {
                f <- TestFolders.testFolders
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
    (cueFolderExists(state), toPrjNme, direction) match {
      case (_, `prjNme`, "prev") =>
        Console.println(Console.YELLOW + "[WARNING] " + Console.RESET + "You're already at the first exercise")
        state
      case (_, `prjNme`, "next") =>
        Console.println(Console.YELLOW + "[WARNING] " + Console.RESET + "You're already at the last exercise")
        state
      case (false, _, _) =>
        var cmd: String = s"project $toPrjNme"
        if (keyName.equals(mapNextKeyName)) cmd = moveNextCmd(prjNme, toPrjNme, state)
        val newState: State = Command.process(cmd, state)
        writeBookmark(toPrjNme, newState)
        newState
      case (true, toProjectName, _) =>
        withZipFile(state, toProjectName) { () =>
          //val src = Project.extract(state).get(sourceDirectory)
          val src = new sbt.File(new sbt.File(Project.extract(state).structure.root), "exercises")
          //println(s"=== src = $src ===")
          // update tests (can be previous or next...)
          val newExerciseSrc = new sbt.File(new sbt.File(Project.extract(state).structure.root), s".cue/$toProjectName")
          for {
            f <- TestFolders.testFolders
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

  def moveNextCmd(frmPrjNme: String, toPrjNme: String, state: State): String = {
    val frmPrjSrc: String = Project.extract(state).get(sourceDirectory) + "/main"
    val toPrjSrc: String = frmPrjSrc.toString.replaceAll(frmPrjNme, toPrjNme)
    val next: String = s"project $toPrjNme"
    val nextGroom: String = s";project $toPrjNme;groom"
    toPrjNme match {
      case ("base" | "common" | "entry_state") =>
        next
      case _ if new sbt.File(toPrjSrc) exists =>
        next
      case _ =>
        nextGroom
    }
  }

  def writeBookmark(toPrjNme: String, state: State): Unit = {
    val key: AttributeKey[File] = AttributeKey[File](bookmarkKeyName)
    val bookmarkFile: Option[File] = state get key
    IO.write(bookmarkFile.get, toPrjNme)
  }
}
