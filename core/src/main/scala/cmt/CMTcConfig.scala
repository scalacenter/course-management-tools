package cmt

import sbt.io.{IO as sbtio}
import sbt.io.syntax.*

import com.typesafe.config.ConfigFactory

import scala.jdk.CollectionConverters.*

import java.nio.charset.StandardCharsets

import Helpers.*

class CMTcConfig(studentifiedRepo: File):
  val bookmarkFile = studentifiedRepo / ".bookmark"

  private val cmtConfigFile = studentifiedRepo / ".cmt-config"
  if !cmtConfigFile.exists then
    printErrorAndExit(studentifiedRepo, "missing CMT configuration file")

  val cmtSettings = ConfigFactory.parseFile(cmtConfigFile)
  val exercises = cmtSettings.getStringList("exercises").asScala
  val dontTouch =
    cmtSettings.getStringList("cmt-studentified-dont-touch").asScala.toSet
  val testCodeFolders =
    cmtSettings.getStringList("test-code-folders").asScala.toSet
  val readMeFiles = cmtSettings.getStringList("read-me-files").asScala.toSet
  val activeExerciseFolder =
    studentifiedRepo / cmtSettings.getString("active-exercise-folder")
  val solutionsFolder = studentifiedRepo / cmtSettings.getString(
    "studentified-repo-solutions-folder"
  )

  val nextExercise: Map[String, String] = exercises.zip(exercises.tail).to(Map)
  val previousExercise: Map[String, String] =
    exercises.tail.zip(exercises).to(Map)

end CMTcConfig
