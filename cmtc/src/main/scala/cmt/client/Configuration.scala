package cmt.client

import cmt.client.Domain.StudentifiedRepo
import com.typesafe.config.ConfigFactory
import sbt.io.IO.write
import sbt.io.syntax.{File, file}

import scala.io.Source

final case class Configuration(coursesDirectory: CoursesDirectory, currentCourse: CurrentCourse)
final case class CoursesDirectory(value: File)
object CoursesDirectory:
  def default(): CoursesDirectory =
    CoursesDirectory(Configuration.CmtCourseHome)
final case class CurrentCourse(value: StudentifiedRepo)

object Configuration:
  val UserHome = System.getProperty("user.home")
  val CmtHome = file(s"$UserHome/.cmt")
  val CmtGlobalConfig = file(s"$CmtHome/cmt.conf")
  val CmtCourseHome = file(s"$UserHome/Courses")
  val CoursesDirectoryToken = "COURSES_DIRECTORY"
  val CurrentCourseToken = "CURRENT_COURSE"
  val DefaultConfigFileName = "config.default.conf"

  private val configStringTemplate =
    """
      |cmtc {
      |
      |    courses-directory = COURSES_DIRECTORY
      |    current-course = CURRENT_COURSE
      |}
      |""".stripMargin

  def load(): Configuration = {
    createIfNotExists()
    readFromConfigFile(CmtGlobalConfig)
  }

  def save(configuration: Configuration): Unit = {
    writeGlobalConfig(
      _.replaceAll(CoursesDirectoryToken, s""""${configuration.coursesDirectory.value.getCanonicalPath}"""")
        .replaceAll(CurrentCourseToken, s""""${configuration.currentCourse.value.value.getCanonicalPath}""""))
  }

  private def readFromConfigFile(configFile: File): Configuration = {
    val config = ConfigFactory.parseFile(configFile)
    val coursesDirectory = CoursesDirectory(file(config.getString("cmtc.courses-directory")))
    val currentCourse = {
      val currentCourseString = config.getString("cmtc.current-course")
      val currentCourseFile = Option
        .when(currentCourseString == CurrentCourseToken)(file(System.getProperty("user.dir")))
        .getOrElse(file(currentCourseString))
      CurrentCourse(StudentifiedRepo(currentCourseFile))
    }
    Configuration(coursesDirectory, currentCourse)
  }

  private def createIfNotExists(): Unit = {
    createCourseHomeIfNotExists()
    createCmtHomeIfNotExists()
    copyDefaultConfigToCmtHome()
  }

  private def createCourseHomeIfNotExists(): Unit =
    if (!CmtCourseHome.exists()) {
      CmtCourseHome.mkdir()
    }

  private def createCmtHomeIfNotExists(): Unit =
    if (!CmtHome.exists()) {
      CmtHome.mkdir()
    }

  private def copyDefaultConfigToCmtHome(): Unit = {
    writeGlobalConfig(
      _.replaceAll(CoursesDirectoryToken, s""""${CmtCourseHome.getCanonicalPath}"""")
        .replaceAll(CurrentCourseToken, s""""${System.getProperty("user.dir")}""""))
  }

  private def writeGlobalConfig(replaceTokens: String => String): Unit = {
    val configStrToWrite = replaceTokens(configStringTemplate)
    write(CmtGlobalConfig, configStrToWrite)
  }
