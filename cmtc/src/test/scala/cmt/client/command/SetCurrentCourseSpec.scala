package cmt.client.command

import cmt.client.Configuration.CmtHome
import cmt.client.Domain.StudentifiedRepo
import cmt.client.{Configuration, CoursesDirectory, CurrentCourse}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import cmt.support.EitherSupport
import sbt.io.IO as sbtio
import sbt.io.syntax.*

import scala.compiletime.uninitialized

final class SetCurrentCourseSpec extends AnyWordSpecLike with Matchers with BeforeAndAfterEach with EitherSupport {

  var tempDirectory: File = uninitialized

  override def beforeEach(): Unit = {
    super.beforeEach()
    tempDirectory = sbtio.createTemporaryDirectory
  }

  override def afterEach(): Unit = {
    sbtio.delete(tempDirectory)
    super.afterEach()
  }

  "set-current-course" when {

    "given a studentified directory" should {

      "write the global configuration with the updated `current-course` value" in {
        val expectedConfiguration = Configuration(
          CmtHome(tempDirectory / ".cmt"),
          CoursesDirectory(tempDirectory / "Courses"),
          CurrentCourse(StudentifiedRepo(file(System.getProperty("user.dir")))))

        val receivedConfiguration = assertRight(Configuration.load(Some(tempDirectory)))

        val expectedDirectory = tempDirectory / "i-am-the-current-course-directory"
        expectedDirectory.mkdir()

        val expectedCurrentCourse = CurrentCourse(StudentifiedRepo(expectedDirectory))

        SetCurrentCourse.Options(directory = expectedCurrentCourse.value).execute(receivedConfiguration)

        val reloadedConfiguration: Configuration = assertRight(Configuration.load(Some(tempDirectory)))

        reloadedConfiguration.currentCourse shouldBe expectedCurrentCourse
      }
    }
  }
}
