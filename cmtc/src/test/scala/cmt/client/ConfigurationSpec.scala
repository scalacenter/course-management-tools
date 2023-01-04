package cmt.client

import java.lang.reflect.Field
import java.lang.reflect.Method
import cmt.client.Configuration.CmtHome
import cmt.client.Domain.StudentifiedRepo
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import cmt.support.EitherSupport
import sbt.io.IO as sbtio
import sbt.io.syntax.*
import scala.jdk.CollectionConverters.*
import scala.compiletime.uninitialized

final class ConfigurationSpec extends AnyWordSpecLike with Matchers with BeforeAndAfterEach with EitherSupport {

  var tempDirectory: File = uninitialized

  override def beforeEach(): Unit = {
    super.beforeEach()
    tempDirectory = sbtio.createTemporaryDirectory
  }

  override def afterEach(): Unit = {
    sbtio.delete(tempDirectory)
    super.afterEach()
  }

  "load" should {
    "create the default configuration in the appropriate home directory" in {
      println(tempDirectory)
      val userHome = file(System.getProperty("user.home"))
      val expectedConfiguration = Configuration(
        CmtHome(tempDirectory / ".cmt"),
        CoursesDirectory(tempDirectory / "Courses"),
        CurrentCourse(StudentifiedRepo(file(System.getProperty("user.dir")))))
      val receivedConfiguration = assertRight(Configuration.load(Some(tempDirectory)))

      receivedConfiguration shouldBe expectedConfiguration
    }
  }
}
