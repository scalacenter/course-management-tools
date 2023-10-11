package coursemgmttools.client.command

import coursemgmttools.Domain.StudentifiedRepo
import coursemgmttools.Helpers.dumpStringToFile
import coursemgmttools.client.{Configuration, CurrentCourse}
import coursemgmttools.support.EitherSupport
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import sbt.io.IO as sbtio
import sbt.io.syntax.*

import java.nio.charset.StandardCharsets
import scala.compiletime.uninitialized

trait SetCurrentCourseSpecTestData {
  val bookmark: String = "exercise_000_initial_state"

  val `cmt-config`: String =
    s"""{
       |    "active-exercise-folder" : "code",
       |    "cmt-studentified-dont-touch" : [ ],
       |    "code-size-and-checksums" : ".cmt/.cmt-code-size-checksums",
       |    "exercises" : [
       |        "exercise_000_initial_state",
       |    ],
       |    "read-me-files" : [
       |        "README.md",
       |    ],
       |    "studentified-repo-bookmark-file" : ".cmt/.bookmark",
       |    "studentified-repo-solutions-folder" : ".cmt/.cue",
       |    "studentified-saved-states-folder" : ".cmt/.cue/.savedStates",
       |    "test-code-folders" : [
       |    ],
       |    "test-code-size-and-checksums" : ".cmt/.cmt-test-size-checksums"
       |}
       |""".stripMargin

  val `cmt-code-size-checksums`: String =
    s"""code-metadata {
       |    "exercise_000_initial_state"=[
       |        {
       |            "README.md" {
       |                sha256="2f16cd4a5dfb22df0d40c596427a3d33a16c8b71198ce0a412989f9be013705d"
       |                size=11564
       |            }
       |        }
       |    ]
       |}
       |""".stripMargin

  val `cmt-test-size-checksums`: String =
    s"""testcode-metadata {
       |    "exercise_000_initial_state"=[
       |        {
       |            "src/test/resources/.gitignore" {
       |                sha256="631b0196fe4474b2b7cb8367f7535ad3d2c541d11e56f74cbf06bd8aff77d1d6"
       |                size=69
       |            }
       |        }
       |     ]
       |}
       |
       |readmefiles-metadata {
       |    "exercise_000_initial_state"=[
       |        {
       |            "README.md" {
       |                sha256="2f16cd4a5dfb22df0d40c596427a3d33a16c8b71198ce0a412989f9be013705d"
       |                size=11564
       |            }
       |        }
       |     ]
       |}
       |""".stripMargin
}

final class SetCurrentCourseSpec
    extends AnyWordSpecLike
    with Matchers
    with BeforeAndAfterEach
    with EitherSupport
    with SetCurrentCourseSpecTestData {

  var tempDirectory: File = uninitialized
  val configFile: File = file(Configuration.UserConfigDir) / Configuration.CmtGlobalConfigName
  var savedCmtConfig: Option[String] =
    if (configFile.isFile)
      Some(sbtio.readLines(configFile, StandardCharsets.UTF_8).mkString("\n"))
    else None

  override def beforeEach(): Unit = {
    super.beforeEach()
    sbt.io.IO.delete(configFile)
    tempDirectory = sbtio.createTemporaryDirectory
  }

  override def afterEach(): Unit = {
    savedCmtConfig.foreach { config =>
      coursemgmttools.Helpers.dumpStringToFile(config, configFile)
    }
    sbtio.delete(tempDirectory)
    super.afterEach()
  }

  "set-current-course" when {

    "given a studentified directory" should {

      "write the global configuration with the updated `current-course` value" in {
        val receivedConfiguration = assertRight(Configuration.load())

        val expectedDirectory = tempDirectory / "i-am-the-current-course-directory"
        expectedDirectory.mkdir()
        val cmtConfigFolder = expectedDirectory / ".cmt"
        cmtConfigFolder.mkdir()

        dumpStringToFile(`cmt-config`, cmtConfigFolder / ".cmt-config")
        dumpStringToFile(`cmt-code-size-checksums`, cmtConfigFolder / ".cmt-code-size-checksums")
        dumpStringToFile(`cmt-test-size-checksums`, cmtConfigFolder / ".cmt-test-size-checksums")
        dumpStringToFile(bookmark, cmtConfigFolder / ".bookmark")

        val expectedCurrentCourse = CurrentCourse(StudentifiedRepo(expectedDirectory))

        SetCurrentCourse.Options(directory = expectedCurrentCourse.value).execute(receivedConfiguration)

        val reloadedConfiguration: Configuration = assertRight(Configuration.load())

        reloadedConfiguration.currentCourse shouldBe expectedCurrentCourse
      }
    }
  }
}
