package cmt

import com.typesafe.config.ConfigFactory

import scala.jdk.CollectionConverters.*

import java.nio.charset.StandardCharsets

import sbt.io.syntax.*
import sbt.io.{IO as sbtio}

import Helpers.*

object CMTStudent:
  def moveToNextExercise(studentifiedRepo: File)(config: CMTcConfig): Unit =

    val currentExercise = sbtio.readLines(config.bookmarkFile, StandardCharsets.UTF_8).head

    if (currentExercise == config.exercises.last) then
      println(toConsoleGreen(s"You're already at the last exercise: $currentExercise"))
    else
      withZipFile(config.solutionsFolder, config.nextExercise(currentExercise)) { solution =>
          copyTestCodeAndReadMeFiles(
          solution, 
          config.nextExercise(currentExercise), 
          s"${toConsoleGreen("Moved to ")} " + "" + s"${toConsoleYellow(s"${config.nextExercise(currentExercise)}")}"
        )(config)
      }
  end moveToNextExercise

  def moveToPreviousExercise(studentifiedRepo: File)(config: CMTcConfig): Unit =

    val currentExercise = sbtio.readLines(config.bookmarkFile, StandardCharsets.UTF_8).head

    if (currentExercise == config.exercises.head) then
      println(toConsoleGreen(s"You're already at the first exercise: $currentExercise"))
    else
      withZipFile(config.solutionsFolder, config.previousExercise(currentExercise)) { solution =>
        copyTestCodeAndReadMeFiles(
          solution, 
          config.previousExercise(currentExercise), 
          s"${toConsoleGreen("Moved to ")} " + "" + s"${toConsoleYellow(s"${config.previousExercise(currentExercise)}")}"
        )
      }
  end moveToPreviousExercise

  def copyTestCodeAndReadMeFiles(solution: File, prevOrNextExercise: String, message: String)(config: CMTcConfig): Unit =

    for {
          testCodeFolder <- config.testCodeFolders
          fromFolder =  solution / testCodeFolder
          toFolder = config.activeExerciseFolder / testCodeFolder
        } {
          sbtio.delete(toFolder)
          sbtio.copyDirectory(fromFolder, toFolder)
        }
    for {
          readmeFile <- config.readMeFiles
        } sbtio.copyFile(solution / readmeFile, config.activeExerciseFolder / readmeFile)
    
    writeStudentifiedCMTBookmark(config.bookmarkFile, prevOrNextExercise)
    println(message)
  end copyTestCodeAndReadMeFiles

  def listExercises(studentifiedRepo: File)(config: CMTcConfig): Unit =

    val currentExercise = sbtio.readLines(config.bookmarkFile, StandardCharsets.UTF_8).head
    config.exercises
      .zipWithIndex
      .foreach { case (exName, index) =>
        println(toConsoleGreen(f"${index + 1}%3d. ${starCurrentExercise(currentExercise, exName)}  $exName"))
      }
  end listExercises

  def pullSolution(studentifiedRepo: File, exerciseID: String)(config: CMTcConfig): Unit =

    val currentExercise = sbtio.readLines(config.bookmarkFile, StandardCharsets.UTF_8).head
      
    val cmtConfigFile = studentifiedRepo / ".cmt-config"
    if !cmtConfigFile.exists then
      printErrorAndExit(studentifiedRepo, "missing CMT configuration file")

    final case class PathARO(absolutePath: File, maybeRelativePath: Option[File])
    final case class PathAR(absolutePath: File, relativePath: File)

    val filesToBeDeleted =
      Helpers.fileList(config.activeExerciseFolder)
        .map(fileAbsolute => PathARO(fileAbsolute, fileAbsolute.relativeTo(config.activeExerciseFolder)))
        .collect{ case PathARO(fileAbsolute, Some(fileRelative)) => PathAR(fileAbsolute, fileRelative)}
        .filterNot{ case PathAR(_, fileRelative) =>
          config.dontTouch.exists(lead => fileRelative.getPath.startsWith(lead))
        }
        .map{ _.absolutePath}
    sbtio.delete(filesToBeDeleted)

    Helpers.withZipFile(config.solutionsFolder, exerciseID) { solution =>
      val files = Helpers.fileList(solution / s"$exerciseID")
      sbtio.copyDirectory(config.solutionsFolder / s"$exerciseID", config.activeExerciseFolder)
    }

    Helpers.writeStudentifiedCMTBookmark(config.bookmarkFile, exerciseID)

    println(toConsoleGreen( s"Pulled solution for $exerciseID"))
  end pullSolution

  def validateStudentifiedRepo(studentifiedRepo: File)(using config: CMTcConfig): Unit =
    //TODO 
    ()
  
  def starCurrentExercise(currentExercise: String, exercise: String): String = {
    if (currentExercise == exercise) " * " else "   "
  }

end CMTStudent