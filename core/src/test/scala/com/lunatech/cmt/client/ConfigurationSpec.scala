package com.lunatech.cmt.client

import com.lunatech.cmt.Domain.StudentifiedRepo
import com.lunatech.cmt.client.Configuration.CmtHome
import com.lunatech.cmt.support.EitherSupport
import dev.dirs.ProjectDirectories
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import sbt.io.IO as sbtio
import sbt.io.syntax.*

final class ConfigurationSpec extends AnyWordSpecLike with Matchers with BeforeAndAfterEach with EitherSupport {

  "load" should {
    "create the default configuration in the appropriate home directory" in {
      val projectDirectories = ProjectDirectories.from("com", "lunatech", "cmt")
      val configDir = file(projectDirectories.configDir)

      sbtio.delete(configDir / "com.lunatech.cmt.conf")

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
