package cmt

import com.typesafe.config.ConfigFactory

import scala.jdk.CollectionConverters.*

import java.nio.charset.StandardCharsets

import sbt.io.syntax.*
import sbt.io.{IO as sbtio}

final case class StudentOptions(exercises: List[String])

object CMTStudent {
  def pullSolution(studentifiedRepo: File, exerciseID: String): Unit =

    val bookmarkFile = studentifiedRepo / ".bookmark"
    val currentExercise = sbtio.readLines(bookmarkFile, StandardCharsets.UTF_8).head

    if exerciseID == currentExercise then
      println(toConsoleGreen(s"You're already at exercise $exerciseID"))
      System.exit(0)
      
    val cmtConfigFile = studentifiedRepo / ".cmt-config"
    if !cmtConfigFile.exists then
      printErrorAndExit(studentifiedRepo, "missing CMT configuration file")

    val cmtSettings = ConfigFactory.parseFile(cmtConfigFile)
    val exercises = cmtSettings.getStringList("exercises").asScala
    if !exercises.contains(exerciseID) then
      printErrorAndExit(studentifiedRepo, s"$exerciseID: no such exercise")
    val dontTouch = cmtSettings.getStringList("cmt-studentified-dont-touch").asScala.toSet
    val activeExerciseFolder = studentifiedRepo / cmtSettings.getString("active-exercise-folder")
    val solutionsFolder = studentifiedRepo / cmtSettings.getString("studentified-repo-solutions-folder")
    

    val x =
      Helpers.fileList(activeExerciseFolder)
        .map(f => (f, f))
        .map((fullFile, file) => (fullFile, file.relativeTo(activeExerciseFolder)))
        .collect{ case (ff, Some(f)) => (ff, f)}
        .filterNot{ (fileFull, file) =>
          dontTouch.exists(lead => file.getPath.startsWith(lead))
        }
    sbtio.delete(x.map(_._1))

    Helpers.withZipFile(activeExerciseFolder, solutionsFolder, exerciseID)(())

    Helpers.writeStudentifiedCMTBookmark(bookmarkFile, exerciseID)
  end pullSolution
      
  def printErrorAndExit(studentifiedRepo: File, message: String): Unit =
    System.err.println(s"${toConsoleRed(message)}")
    System.exit(1)

  def validateStudentifiedRepo(studentifiedRepo: File)(using config: CMTConfig): Unit =
    ()

}
