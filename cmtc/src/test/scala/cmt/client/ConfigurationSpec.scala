package cmt.client

import cmt.client.Configuration.CmtHome
import cmt.client.Domain.StudentifiedRepo
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import cmt.support.EitherSupport
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
      val l = sbtio.readLines(configFile, StandardCharsets.UTF_8).mkString("\n")
      println(s"""
           |
           |Config:
           |$l
           |""".stripMargin)
      Some(sbtio.readLines(configFile, StandardCharsets.UTF_8).mkString("\n"))
    else None

  override def beforeEach(): Unit = {
    super.beforeEach()
    tempDirectory = sbtio.createTemporaryDirectory
    sbt.io.IO.delete(configFile)
    println(s"tempDirectory = $tempDirectory")
  }

  override def afterEach(): Unit = {
    savedCmtConfig.foreach { config =>
      cmt.Helpers.dumpStringToFile(config, configFile)
    }
    sbtio.delete(tempDirectory)
    super.afterEach()
  }

  "load" should {
    "create the default configuration in the appropriate home directory" in {
      val projectDirectories = ProjectDirectories.from("com", "lunatech", "cmt")
      val configDir = file(projectDirectories.configDir)
      val cacheDir = file(projectDirectories.cacheDir)
      println(s"cacheDir = $cacheDir")
      val expectedConfiguration = Configuration(
        CmtHome(configDir),
        CoursesDirectory(cacheDir / "Courses"),
        CurrentCourse(StudentifiedRepo(cacheDir / "Courses")))

      val receivedConfiguration = assertRight(Configuration.load())

      receivedConfiguration shouldBe expectedConfiguration
    }
  }
}
