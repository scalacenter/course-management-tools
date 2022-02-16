package cmt

import com.typesafe.config.ConfigFactory

import scala.jdk.CollectionConverters.*

import java.nio.charset.StandardCharsets

import sbt.io.syntax.*
import sbt.io.{IO as sbtio}

import Helpers.*

final case class StudentOptions(exercises: List[String])

object CMTStudent {
  def moveToNextExercise(studentifiedRepo: File)(using config: CMTcConfig): Unit =
    import config.*

    val currentExercise = sbtio.readLines(bookmarkFile, StandardCharsets.UTF_8).head

    if (currentExercise == exercises.last) then
      println(toConsoleGreen(s"You're already at the last exercise: $currentExercise"))
    else
      withZipFile(activeExerciseFolder, solutionsFolder, nextExercise(currentExercise)) { solution =>
          copyTestCodeAndReadMeFiles(
          solution, 
          nextExercise(currentExercise), 
          s"${toConsoleGreen("Moved to ")} " + "" + s"${toConsoleYellow(s"${nextExercise(currentExercise)}")}"
        )
      }
  end moveToNextExercise

  def moveToPreviousExercise(studentifiedRepo: File)(using config: CMTcConfig): Unit =
    import config.*

    val currentExercise = sbtio.readLines(bookmarkFile, StandardCharsets.UTF_8).head

    if (currentExercise == exercises.head) then
      println(toConsoleGreen(s"You're already at the first exercise: $currentExercise"))
    else
      withZipFile(activeExerciseFolder, solutionsFolder, previousExercise(currentExercise)) { solution =>
        copyTestCodeAndReadMeFiles(
          solution, 
          previousExercise(currentExercise), 
          s"${toConsoleGreen("Moved to ")} " + "" + s"${toConsoleYellow(s"${previousExercise(currentExercise)}")}"
        )
      }
  end moveToPreviousExercise

  def copyTestCodeAndReadMeFiles(solution: File, prevOrNextExercise: String, message: String)(using config: CMTcConfig): Unit =
    import config.*
    for {
          testCodeFolder <- testCodeFolders
          fromFolder =  solution / testCodeFolder
          toFolder = activeExerciseFolder / testCodeFolder
        } {
          sbtio.delete(toFolder)
          sbtio.copyDirectory(fromFolder, toFolder)
        }
    for {
          readmeFile <- readMeFiles
        } sbtio.copyFile(solution / readmeFile, activeExerciseFolder / readmeFile)
    
    writeStudentifiedCMTBookmark(bookmarkFile, prevOrNextExercise)
    println(message)
  end copyTestCodeAndReadMeFiles

  def listExercises(studentifiedRepo: File)(using config: CMTcConfig): Unit =
    import config.*
    val currentExercise = sbtio.readLines(bookmarkFile, StandardCharsets.UTF_8).head
    exercises
      .zipWithIndex
      .foreach { case (exName, index) =>
        println(toConsoleGreen(f"${index + 1}%3d. ${starCurrentExercise(currentExercise, exName)}  $exName"))
      }
  end listExercises

  def pullSolution(studentifiedRepo: File, exerciseID: String)(using config: CMTcConfig): Unit =

    import config.*

    val currentExercise = sbtio.readLines(bookmarkFile, StandardCharsets.UTF_8).head

    if exerciseID == currentExercise then
      println(toConsoleGreen(s"You're already at exercise $exerciseID"))
      System.exit(0)
      
    val cmtConfigFile = studentifiedRepo / ".cmt-config"
    if !cmtConfigFile.exists then
      printErrorAndExit(studentifiedRepo, "missing CMT configuration file")

    val filesToBeDeleted =
      Helpers.fileList(activeExerciseFolder)
        .map(fileAbsolute => (fileAbsolute, fileAbsolute.relativeTo(activeExerciseFolder)))
        .collect{ case (fileAbsolute, Some(fileRelative)) => (fileAbsolute, fileRelative)}
        .filterNot{ (_, fileRelative) =>
          dontTouch.exists(lead => fileRelative.getPath.startsWith(lead))
        }
        .map{ case (fileAbsolute, _) => fileAbsolute}
    sbtio.delete(filesToBeDeleted)

    Helpers.withZipFile(activeExerciseFolder, solutionsFolder, exerciseID) { solution =>
      val files = Helpers.fileList(solution / s"$exerciseID")
      sbtio.copyDirectory(solutionsFolder / s"$exerciseID", activeExerciseFolder)
    }

    Helpers.writeStudentifiedCMTBookmark(bookmarkFile, exerciseID)

    println(toConsoleGreen( s"Pulled solution for $exerciseID"))
  end pullSolution

  def validateStudentifiedRepo(studentifiedRepo: File)(using config: CMTcConfig): Unit =
    //TODO 
    ()
  
  def starCurrentExercise(currentExercise: String, exercise: String): String = {
    if (currentExercise == exercise) " * " else "   "
  }

}
