package coursemgmttools.client

import coursemgmttools.Domain.StudentifiedRepo
import coursemgmttools.client.Configuration.CmtHome
import coursemgmttools.support.EitherSupport
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

      val cacheDir = file(projectDirectories.cacheDir)
      val expectedConfiguration = Configuration(
        CmtHome(configDir),
        CoursesDirectory(cacheDir / "Courses"),
        CurrentCourse(StudentifiedRepo(cacheDir / "Courses")))

      // this spec is mimic'ing the first time the tool is run, so it expects no config file to exist.
      // other specs _may_ have run before this one and already created the config file
      // so that's why we ensure that the cmt config file is removed before we execute
      // otherwise, if the file exists, it will likely not contain default values and the assertion below will fail
      sbtio.delete(configDir / "coursemgmttools.conf")

      val receivedConfiguration = assertRight(Configuration.load())

      receivedConfiguration shouldBe expectedConfiguration
    }
  }
}
