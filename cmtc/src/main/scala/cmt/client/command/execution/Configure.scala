package cmt.client.command.execution

import cmt.client.command.ClientCommand.Configure
import cmt.client.command.execution.ConfigureHelper.{copyDefaultConfigToCmtHome, createCmtHomeIfNotExists, createCourseHomeIfNotExists}
import cmt.client.config.CmtcConfig
import cmt.core.execution.Executable
import com.typesafe.config.ConfigFactory
import sbt.io.IO as sbtio
import sbt.io.syntax.{File, file}
import sbt.io.IO.{copyFile, toFile, write}

import scala.io.Source

given Executable[Configure.type] with
  extension (cmd: Configure.type)
    def execute(): Either[String, String] = {
      createCourseHomeIfNotExists()
      createCmtHomeIfNotExists()
      copyDefaultConfigToCmtHome()
      Right("")
    }

object ConfigureHelper {
  private val UserHome = System.getProperty("user.home")
  private val CmtHome = file(s"$UserHome/.cmt")
  private val CmtGlobalConfig = file(s"$CmtHome/cmt.conf")
  private val CmtCourseHome = file(s"$UserHome/Courses")
  private val CoursesDirectoryToken = "COURSES_DIRECTORY"
  private val DefaultConfigFileName = "config.default.conf"

  def createCourseHomeIfNotExists(): Unit = {
    if (!CmtCourseHome.exists()) {
      CmtCourseHome.mkdir()
    }
  }

  def createCmtHomeIfNotExists(): Unit =
    if (!CmtHome.exists()) {
      CmtHome.mkdir()
    }

  def copyDefaultConfigToCmtHome(): Unit = {
    val defaultConfigAsString = Source
      .fromInputStream(Configure.getClass.getResourceAsStream(s"/$DefaultConfigFileName"))
      .getLines()
      .mkString("\n")
      .replaceAll(CoursesDirectoryToken, s""""${CmtCourseHome.getCanonicalPath}"""")
    write(CmtGlobalConfig, defaultConfigAsString)
  }
}


