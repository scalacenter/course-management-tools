package com.lunatech.cmt.client

import com.lunatech.cmt.client.Configuration.{CmtHome, CoursesDirectoryToken, CurrentCourseToken, GithubApiToken, globalConfigFile, writeGlobalConfig}
import com.lunatech.cmt.{CmtError, FailedToWriteGlobalConfiguration, printMessage}
import com.lunatech.cmt.client.Domain.StudentifiedRepo
import com.typesafe.config.{Config, ConfigFactory}
import sbt.io.IO.*
import sbt.io.syntax.*
import com.lunatech.cmt.Helpers.{adaptToNixSeparatorChar, adaptToOSSeparatorChar}

import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Success, Try}
import dev.dirs.ProjectDirectories

import java.nio.charset.StandardCharsets
import java.util.Base64

final case class Configuration(
    homeDirectory: CmtHome,
    coursesDirectory: CoursesDirectory,
    currentCourse: CurrentCourse,
    githubApiToken: GithubApiToken) {
  def flush(): Either[CmtError, Unit] = {
    val configFile = globalConfigFile(homeDirectory)

    Try(
      writeGlobalConfig(
        configFile,
        _.replaceAll(CoursesDirectoryToken, s""""${adaptToNixSeparatorChar(coursesDirectory.value.getAbsolutePath)}"""")
          .replaceAll(
            CurrentCourseToken,
            s""""${adaptToNixSeparatorChar(currentCourse.value.value.getAbsolutePath)}""""))) match {
      case Success(_)         => Right(())
      case Failure(exception) => Left(FailedToWriteGlobalConfiguration(exception))
    }
  }
}
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
  final case class GithubApiToken(value: String)
  object GithubApiToken {
    // If github detects an API token in a commit it deactivates the token so we'll Base64 encode the token in config
    def fromBase64EncodedString(base64EncodedString: String): GithubApiToken = {
      val encodedBytes = base64EncodedString.getBytes(StandardCharsets.UTF_8)
      val decodedBytes = Base64.getDecoder().decode(encodedBytes)
      val decodedString = new String(decodedBytes, StandardCharsets.UTF_8)
      GithubApiToken(decodedString)
    }
  }

  private val projectDirectories = ProjectDirectories.from("com", "lunatech", "cmt")
  val UserConfigDir = projectDirectories.configDir
  val CmtGlobalConfigName = "com.lunatech.cmt.conf"
  val CoursesDirectoryToken = "COURSES_DIRECTORY"
  val CurrentCourseToken = "CURRENT_COURSE"
  val githubApiTokenToken = "GITHUB_API_TOKEN"
  val CmtHomeEnvKey = "CMT_HOME"

  val DefaultCmtCoursesHome = s"${projectDirectories.cacheDir}/Courses"
  val CmtCoursesHomeEnvKey = "CMT_COURSES_HOME"

  val DefaultGithubApiToken = "Z2l0aHViX3BhdF8xMUFIS0w3Q1kwVGphVjVQY3RZcVpjX2ZzNDRIUldIZ3Q1MWcwb0ZZdTJHQzdqVWhFWnFWRFM4Rm5PWENqejdib2Q0RkdNM1VXWk9vR21LMFJE"

  private def globalConfigFile(cmtHome: CmtHome): CmtGlobalConfigFile =
    CmtGlobalConfigFile(cmtHome.value / CmtGlobalConfigName)

  private val configStringTemplate =
    s"""
      |cmtc {
      |    courses-directory = $CoursesDirectoryToken}
      |    current-course = $CurrentCourseToken
      |    github-api-token = $githubApiTokenToken
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
  def load(): Either[CmtError, Configuration] = {
    val cmtHomePath = System.getenv().asScala.getOrElse(CmtHomeEnvKey, UserConfigDir)
    val cmtHome = CmtHome(file(cmtHomePath))

    val cmtCourseDirectoryPath = System.getenv().asScala.getOrElse(CmtCoursesHomeEnvKey, DefaultCmtCoursesHome)
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
    val coursesDirectory = CoursesDirectory(file(adaptToOSSeparatorChar(config.getString("cmtc.courses-directory"))))
    val currentCourse = CurrentCourse(
      StudentifiedRepo(file(adaptToOSSeparatorChar(config.getString("cmtc.current-course")))))
    val githubApiToken = GithubApiToken.fromBase64EncodedString(config.getString("cmtc.github-api-token"))
    Configuration(cmtHome, coursesDirectory, currentCourse, githubApiToken)
  }

  private def createIfNotExists(cmtHome: CmtHome, cmtCoursesHome: CmtCoursesHome): Unit =
    createCourseHomeIfNotExists(cmtCoursesHome)
    createCmtHomeIfNotExists(cmtHome)
    createDefaultConfigIfNotExists(cmtHome, cmtCoursesHome)

  private def createCourseHomeIfNotExists(cmtCoursesHome: CmtCoursesHome): Unit =
    if (!cmtCoursesHome.value.exists()) {
      printMessage(
        s"the CMT_COURSES_HOME directory at '${cmtCoursesHome.value.getAbsolutePath}' does not exist, creating it")
      sbt.io.IO.createDirectory(cmtCoursesHome.value)
    }

  private def createCmtHomeIfNotExists(cmtHome: CmtHome): Unit =
    if (!cmtHome.value.exists()) {
      printMessage(s"the CMT_HOME directory at '${cmtHome.value.getAbsolutePath}' does not exist, creating it")
      sbt.io.IO.createDirectory(cmtHome.value)
    }

  private def createDefaultConfigIfNotExists(cmtHome: CmtHome, cmtCoursesHome: CmtCoursesHome): Unit =
    val configFile = globalConfigFile(cmtHome)
    if (!configFile.value.exists()) {
      printMessage(
        s"global configuration file is missing from '${configFile.value.getAbsolutePath}' creating it with default values")
      val currentCourse = CurrentCourse(StudentifiedRepo(cmtCoursesHome.value))
      val githubApiToken = GithubApiToken(DefaultGithubApiToken)
      val configuration = Configuration(cmtHome, CoursesDirectory(cmtCoursesHome.value), currentCourse, githubApiToken)
      configuration.flush()
    }

  private def writeGlobalConfig(cmtGlobalConfigFile: CmtGlobalConfigFile, replaceTokens: String => String): Unit = {
    val configStrToWrite = replaceTokens(configStringTemplate)
    write(cmtGlobalConfigFile.value, configStrToWrite)
  }
