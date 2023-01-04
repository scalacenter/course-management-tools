package cmt.client

import cmt.client.Configuration.CmtHome
import cmt.{CmtError, printErrorAndExit, printMessage, toConsoleGreen}
import cmt.client.Domain.StudentifiedRepo
import com.typesafe.config.{Config, ConfigFactory}
import sbt.io.IO.write
import sbt.io.syntax.*

import scala.jdk.CollectionConverters.*

final case class Configuration(homeDirectory: CmtHome, coursesDirectory: CoursesDirectory, currentCourse: CurrentCourse)
final case class CoursesDirectory(value: File)
final case class CurrentCourse(value: StudentifiedRepo)

object Configuration:

  final case class UserHome(value: File)
  final case class CmtHome(value: File)
  final case class CmtGlobalConfigFile(value: File) {
    def config(): Config =
      ConfigFactory.parseFile(value)
  }
  final case class CmtCoursesHome(value: File)

  val UserHomeDir = System.getProperty("user.home")
  val CmtHomeDirectoryName = ".cmt"
  val CmtGlobalConfigName = "cmt.conf"
  val DefaultCmtHome = s"$UserHomeDir/$CmtHomeDirectoryName"
  val CoursesDirectoryToken = "COURSES_DIRECTORY"
  val CurrentCourseToken = "CURRENT_COURSE"
  val DefaultConfigFileName = "config.default.conf"
  val CmtHomeEnvKey = "CMT_HOME"

  val DefaultCmtCoursesHome = s"$UserHomeDir/Courses"
  val CmtCoursesHomeEnvKey = "CMT_COURSES_HOME"

  private def globalConfigFile(cmtHome: CmtHome): CmtGlobalConfigFile =
    CmtGlobalConfigFile(cmtHome.value / CmtGlobalConfigName)

  private val configStringTemplate =
    """
      |cmtc {
      |    courses-directory = COURSES_DIRECTORY
      |    current-course = CURRENT_COURSE
      |}
      |""".stripMargin

  /** loads the configuration, if the configuration is not currently available (for instance, if this is the first time
    * `load` has run) then the default configuration is created and written to the default location. The default
    * location is $HOME/.cmt but this can be overridden with an environment variable `CMT_HOME`.
    *
    * The global configuration points to a directory where all the installed courses are kept. By default this is
    * located at `$HOME/Courses` but this can also be overridden with an environment variable `CMT_COURSE_HOME`
    * @return
    */
  def load(homeDirectory: Option[File] = None): Either[CmtError, Configuration] = {
    val cmtHomePath = homeDirectory
      .map(home => s"$home/$CmtHomeDirectoryName")
      .orElse(System.getenv().asScala.get(CmtHomeEnvKey))
      .getOrElse(DefaultCmtHome)
    val cmtHome = CmtHome(file(cmtHomePath))

    val cmtCourseDirectoryPath = homeDirectory
      .map(home => s"$home/Courses")
      .orElse(System.getenv().asScala.get(CmtCoursesHomeEnvKey))
      .getOrElse(DefaultCmtCoursesHome)
    val cmtCoursesHome = CmtCoursesHome(file(cmtCourseDirectoryPath))

    load(cmtHome, cmtCoursesHome)
  }

  private def load(cmtHome: CmtHome, cmtCoursesHome: CmtCoursesHome): Either[CmtError, Configuration] = {
    createIfNotExists(cmtHome, cmtCoursesHome)
    Right(readFromConfigFile(cmtHome))
  }

  private def readFromConfigFile(cmtHome: CmtHome): Configuration = {
    val configFile = globalConfigFile(cmtHome)
    val config = configFile.config()
    val coursesDirectory = CoursesDirectory(file(config.getString("cmtc.courses-directory")))
    val currentCourse = CurrentCourse(StudentifiedRepo(file(config.getString("cmtc.current-course"))))
    Configuration(cmtHome, coursesDirectory, currentCourse)
  }

  private def createIfNotExists(cmtHome: CmtHome, cmtCoursesHome: CmtCoursesHome): Unit = {
    createCourseHomeIfNotExists(cmtCoursesHome)
    createCmtHomeIfNotExists(cmtHome)
    createDefaultConfigIfNotExists(cmtHome, cmtCoursesHome)
  }

  private def createCourseHomeIfNotExists(cmtCoursesHome: CmtCoursesHome): Unit =
    if (!cmtCoursesHome.value.exists()) {
      printMessage(
        s"the CMT_COURSES_HOME directory at '${cmtCoursesHome.value.getAbsolutePath}' does not exist, creating it")
      cmtCoursesHome.value.mkdir()
    }

  private def createCmtHomeIfNotExists(cmtHome: CmtHome): Unit =
    if (!cmtHome.value.exists()) {
      printMessage(s"the CMT_HOME directory at '${cmtHome.value.getAbsolutePath}' does not exist, creating it")
      cmtHome.value.mkdir()
    }

  private def createDefaultConfigIfNotExists(cmtHome: CmtHome, cmtCoursesHome: CmtCoursesHome): Unit = {
    val configFile = globalConfigFile(cmtHome)
    if (!configFile.value.exists()) {
      printMessage(
        s"global configuration file is missing from '${configFile.value.getAbsolutePath}' creating it with default values")
      writeGlobalConfig(
        configFile,
        _.replaceAll(CoursesDirectoryToken, s""""${cmtCoursesHome.value}"""")
          .replaceAll(CurrentCourseToken, s""""${System.getProperty("user.dir")}""""))
    }
  }

  private def writeGlobalConfig(cmtGlobalConfigFile: CmtGlobalConfigFile, replaceTokens: String => String): Unit = {
    val configStrToWrite = replaceTokens(configStringTemplate)
    write(cmtGlobalConfigFile.value, configStrToWrite)
  }
