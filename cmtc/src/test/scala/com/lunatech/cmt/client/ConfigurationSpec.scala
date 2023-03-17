package com.lunatech.cmt.client

import com.lunatech.cmt.client.Configuration.CmtHome
import com.lunatech.cmt.client.Domain.StudentifiedRepo
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import com.lunatech.cmt.support.EitherSupport
import sbt.io.IO as sbtio
import sbt.io.syntax.*

import scala.compiletime.uninitialized
import dev.dirs.ProjectDirectories

import java.nio.charset.StandardCharsets

final class ConfigurationSpec extends AnyWordSpecLike with Matchers with BeforeAndAfterEach with EitherSupport {

  var tempDirectory: File = uninitialized
  val configFile = file(Configuration.UserConfigDir) / Configuration.CmtGlobalConfigName
  var savedCmtConfig: Option[String] =
    if (configFile.isFile)
      Some(sbtio.readLines(configFile, StandardCharsets.UTF_8).mkString("\n"))
    else None

  override def beforeEach(): Unit = {
    super.beforeEach()
    tempDirectory = sbtio.createTemporaryDirectory
    sbt.io.IO.delete(configFile)
  }

  override def afterEach(): Unit = {
    savedCmtConfig.foreach { config =>
      com.lunatech.cmt.Helpers.dumpStringToFile(config, configFile)
    }
    sbtio.delete(tempDirectory)
    super.afterEach()
  }

  "load" should {
    "create the default configuration in the appropriate home directory" in {
      val projectDirectories = ProjectDirectories.from("com", "lunatech", "cmt")
      val configDir = file(projectDirectories.configDir)
      val cacheDir = file(projectDirectories.cacheDir)
      val expectedConfiguration = Configuration(
        CmtHome(configDir),
        CoursesDirectory(cacheDir / "Courses"),
        CurrentCourse(StudentifiedRepo(cacheDir / "Courses")))

      val receivedConfiguration = assertRight(Configuration.load())

      receivedConfiguration shouldBe expectedConfiguration
    }
  }
}
