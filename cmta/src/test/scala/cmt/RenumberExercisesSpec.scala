package cmt

import cmt.Helpers.ExercisePrefixesAndExerciseNames_TBR
import cmt.admin.Domain.{MainRepository, RenumberOffset, RenumberStart, RenumberStep}
import cmt.admin.command.AdminCommand.RenumberExercises
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.*
import sbt.io.IO as sbtio
import sbt.io.syntax.*
import com.typesafe.config.ConfigFactory
import cmt.admin.command.execution.given

import java.util.UUID

trait RenumberExercisesFixture:
  val testConfig: String =
    """cmt {
      |  main-repo-exercise-folder = code
      |  studentified-repo-solutions-folder = .cue
      |  studentified-saved-states-folder = .savedStates
      |  studentified-repo-active-exercise-folder = code
      |  linearized-repo-active-exercise-folder = code
      |  config-file-default-name = course-management.conf
      |  test-code-folders = [ "src/test" ]
      |  read-me-files = [ "README.md" ]
      |  cmt-studentified-config-file = .cmt-config
      |  cmt-studentified-dont-touch = [ ".idea", ".bsp", ".bloop" ]
      |}""".stripMargin

  val tempDirectory: File = sbtio.createTemporaryDirectory

  def getMainRepoAndConfig(): (File, File, CMTaConfig) =
    val mainRepo: File = tempDirectory / UUID.randomUUID().toString

    sbtio.createDirectory(mainRepo)
    Helpers.dumpStringToFile(testConfig, mainRepo / "course-management.conf")

    val config: CMTaConfig = CMTaConfig(mainRepo, None)

    val codeFolder = mainRepo / "code"
    sbtio.createDirectory(codeFolder)

    (mainRepo, codeFolder, config)

  def createExercises(codeFolder: File, exercises: Vector[String]): Vector[String] =
    sbtio.createDirectories(exercises.map(exercise => codeFolder / exercise))
    sbtio.touch(exercises.map(exercise => codeFolder / exercise / "README.md"))
    exercises

end RenumberExercisesFixture

class RenumberExercisesSpec
    extends AnyWordSpec
    with should.Matchers
    with BeforeAndAfterAll
    with RenumberExercisesFixture:

  override def afterAll(): Unit =
    tempDirectory.delete()

  "RenumberExercises" when {
    "given a renumbering" should {
      val (mainRepo, codeFolder, config) = getMainRepoAndConfig()

      val exerciseNames =
        Vector("exercise_001_desc", "exercise_002_desc", "exercise_003_desc", "exercise_004_desc", "exercise_005_desc")
      val exercises = createExercises(codeFolder, exerciseNames)

      "succeed if exercises are moved to a new offset and renumber step values" in {
        val command = RenumberExercises(
          mainRepository = MainRepository(mainRepo),
          config = config,
          maybeStart = None,
          offset = RenumberOffset(20),
          step = RenumberStep(2))
        val result = command.execute()
        result shouldBe Right(command.successMessage)

        val renumberedExercises = Helpers.getExercisePrefixAndExercises_TBR(mainRepo)(config).exercises
        val expectedExercises = Vector(
          "exercise_020_desc",
          "exercise_022_desc",
          "exercise_024_desc",
          "exercise_026_desc",
          "exercise_028_desc")
        renumberedExercises shouldBe expectedExercises
      }
      "succeed and return the original exercise set when using the default offset and renumber step alues" in {
        val command = RenumberExercises(
          mainRepository = MainRepository(mainRepo),
          config = config,
          maybeStart = None,
          offset = RenumberOffset(1),
          step = RenumberStep(1))
        val result = command.execute()
        result shouldBe Right(command.successMessage)

        val renumberedExercises = Helpers.getExercisePrefixAndExercises_TBR(mainRepo)(config).exercises
        val expectedExercises = Vector(
          "exercise_001_desc",
          "exercise_002_desc",
          "exercise_003_desc",
          "exercise_004_desc",
          "exercise_005_desc")
        renumberedExercises shouldBe expectedExercises
      }
      "succeed if exercises are moved to offset 0 and the first exercise to renumber is the first one in the exercise series" in {
        val command = RenumberExercises(
          mainRepository = MainRepository(mainRepo),
          config = config,
          maybeStart = Some(RenumberStart(1)),
          offset = RenumberOffset(0),
          step = RenumberStep(1))
        val result = command.execute()
        result shouldBe Right(command.successMessage)

        val renumberedExercises = Helpers.getExercisePrefixAndExercises_TBR(mainRepo)(config).exercises
        val expectedExercises = Vector(
          "exercise_000_desc",
          "exercise_001_desc",
          "exercise_002_desc",
          "exercise_003_desc",
          "exercise_004_desc")
        renumberedExercises shouldBe expectedExercises
      }
      "succeed and leave the first exercise number unchanged and create a gap between the first and second exercise" in {
        val command = RenumberExercises(
          mainRepository = MainRepository(mainRepo),
          config = config,
          maybeStart = Some(RenumberStart(1)),
          offset = RenumberOffset(10),
          step = RenumberStep(1))
        val result = command.execute()
        result shouldBe Right(command.successMessage)

        val renumberedExercises = Helpers.getExercisePrefixAndExercises_TBR(mainRepo)(config).exercises
        val expectedExercises = Vector(
          "exercise_000_desc",
          "exercise_010_desc",
          "exercise_011_desc",
          "exercise_012_desc",
          "exercise_013_desc")
        renumberedExercises shouldBe expectedExercises
      }
      "succeed when renumbering moves exercises to the end of the available exercise number space" in {
        val command = RenumberExercises(
          mainRepository = MainRepository(mainRepo),
          config = config,
          maybeStart = None,
          offset = RenumberOffset(995),
          step = RenumberStep(1))
        val result = command.execute()
        result shouldBe Right(command.successMessage)

        val renumberedExercises = Helpers.getExercisePrefixAndExercises_TBR(mainRepo)(config).exercises
        val expectedExercises = Vector(
          "exercise_995_desc",
          "exercise_996_desc",
          "exercise_997_desc",
          "exercise_998_desc",
          "exercise_999_desc")
        renumberedExercises shouldBe expectedExercises
      }
      "fail when renumbering would move outside the available exercise number space and leave the exercise name unchanged" in {
        val command = RenumberExercises(
          mainRepository = MainRepository(mainRepo),
          config = config,
          maybeStart = None,
          offset = RenumberOffset(996),
          step = RenumberStep(1))
        val result = command.execute()

        result shouldBe Left("Cannot renumber exercises as it would exceed the available exercise number space")
        val renumberedExercises = Helpers.getExercisePrefixAndExercises_TBR(mainRepo)(config).exercises
        val expectedExercises = Vector(
          "exercise_995_desc",
          "exercise_996_desc",
          "exercise_997_desc",
          "exercise_998_desc",
          "exercise_999_desc")
        renumberedExercises shouldBe expectedExercises
      }
      "succeed when moving exercises up to a range that overlaps with the current one" in {
        val command = RenumberExercises(
          mainRepository = MainRepository(mainRepo),
          config = config,
          maybeStart = None,
          offset = RenumberOffset(992),
          step = RenumberStep(1))
        val result = command.execute()

        result shouldBe Right(command.successMessage)
        val renumberedExercises = Helpers.getExercisePrefixAndExercises_TBR(mainRepo)(config).exercises
        val expectedExercises = Vector(
          "exercise_992_desc",
          "exercise_993_desc",
          "exercise_994_desc",
          "exercise_995_desc",
          "exercise_996_desc")
        renumberedExercises shouldBe expectedExercises
      }
      "succeed when moving exercises down to a range that overlaps with the current one" in {
        val command = RenumberExercises(
          mainRepository = MainRepository(mainRepo),
          config = config,
          maybeStart = None,
          offset = RenumberOffset(995),
          step = RenumberStep(1))
        val result = command.execute()

        result shouldBe Right(command.successMessage)
        val renumberedExercises = Helpers.getExercisePrefixAndExercises_TBR(mainRepo)(config).exercises
        val expectedExercises = Vector(
          "exercise_995_desc",
          "exercise_996_desc",
          "exercise_997_desc",
          "exercise_998_desc",
          "exercise_999_desc")
        renumberedExercises shouldBe expectedExercises
      }
      "succeed and return a series of exercises numbered starting at 1 when renumbering with default args" in {
        val command = RenumberExercises(
          mainRepository = MainRepository(mainRepo),
          config = config,
          maybeStart = None,
          offset = RenumberOffset(1),
          step = RenumberStep(1))
        val result = command.execute()

        result shouldBe Right(command.successMessage)
        val renumberedExercises = Helpers.getExercisePrefixAndExercises_TBR(mainRepo)(config).exercises
        val expectedExercises = Vector(
          "exercise_001_desc",
          "exercise_002_desc",
          "exercise_003_desc",
          "exercise_004_desc",
          "exercise_005_desc")
        renumberedExercises shouldBe expectedExercises
      }
      "fail and leave the exercise numbers unchanged when default renumbering is applied again" in {
        val command = RenumberExercises(
          mainRepository = MainRepository(mainRepo),
          config = config,
          maybeStart = None,
          offset = RenumberOffset(1),
          step = RenumberStep(1))
        val result = command.execute()

        result shouldBe Left("Renumber: nothing to renumber")
        val renumberedExercises = Helpers.getExercisePrefixAndExercises_TBR(mainRepo)(config).exercises
        val expectedExercises = Vector(
          "exercise_001_desc",
          "exercise_002_desc",
          "exercise_003_desc",
          "exercise_004_desc",
          "exercise_005_desc")
        renumberedExercises shouldBe expectedExercises
      }
      "fail when trying to renumber a range of exercises that would clash with other exercises" in {
        val command = RenumberExercises(
          mainRepository = MainRepository(mainRepo),
          config = config,
          maybeStart = Some(RenumberStart(3)),
          offset = RenumberOffset(2),
          step = RenumberStep(1))
        val result = command.execute()

        result shouldBe Left("Moved exercise range overlaps with other exercises")
        val renumberedExercises = Helpers.getExercisePrefixAndExercises_TBR(mainRepo)(config).exercises
        val expectedExercises = Vector(
          "exercise_001_desc",
          "exercise_002_desc",
          "exercise_003_desc",
          "exercise_004_desc",
          "exercise_005_desc")
        renumberedExercises shouldBe expectedExercises
      }
      "fail when trying to renumber an exercise if it would move into a gap in the numbering" in {
        RenumberExercises(
          mainRepository = MainRepository(mainRepo),
          config = config,
          maybeStart = Some(RenumberStart(3)),
          offset = RenumberOffset(10),
          step = RenumberStep(1)).execute()

        val renumberedExercises = Helpers.getExercisePrefixAndExercises_TBR(mainRepo)(config).exercises
        val expectedExercises = Vector(
          "exercise_001_desc",
          "exercise_002_desc",
          "exercise_010_desc",
          "exercise_011_desc",
          "exercise_012_desc")
        val result = RenumberExercises(
          mainRepository = MainRepository(mainRepo),
          config = config,
          maybeStart = Some(RenumberStart(12)),
          offset = RenumberOffset(5),
          step = RenumberStep(1)).execute()
        result shouldBe Left("Moved exercise range overlaps with other exercises")
        renumberedExercises shouldBe expectedExercises
      }
    }
    "given any renumbering in a fully packed exercise numbering space" should {
      val (mainRepo, codeFolder, config) = getMainRepoAndConfig()

      val exerciseNames = Vector.from(0 to 999).map(i => f"exercise_$i%03d_desc")
      val exercises = createExercises(codeFolder, exerciseNames)

      "fail when trying to shift all exercises one position downwards and leave the exercise name unchanged" in {
        val result = RenumberExercises(
          mainRepository = MainRepository(mainRepo),
          config = config,
          maybeStart = None,
          offset = RenumberOffset(1),
          step = RenumberStep(1)).execute()
        result shouldBe Left("Cannot renumber exercises as it would exceed the available exercise number space")
        val renumberedExercises = Helpers.getExercisePrefixAndExercises_TBR(mainRepo)(config).exercises
        renumberedExercises shouldBe exercises
      }
      "fail when trying to move the second to last exercise on position downwards and leave the exercise name unchanged" in {
        val result = RenumberExercises(
          mainRepository = MainRepository(mainRepo),
          config = config,
          maybeStart = Some(RenumberStart(998)),
          offset = RenumberOffset(999),
          step = RenumberStep(1)).execute()
        result shouldBe Left("Cannot renumber exercises as it would exceed the available exercise number space")
        val renumberedExercises = Helpers.getExercisePrefixAndExercises_TBR(mainRepo)(config).exercises
        renumberedExercises shouldBe exercises
      }
      "fail when trying to insert holes in the numbering and leave the exercise name unchanged" in {
        val result = RenumberExercises(
          mainRepository = MainRepository(mainRepo),
          config = config,
          maybeStart = None,
          offset = RenumberOffset(0),
          step = RenumberStep(2)).execute()
        result shouldBe Left("Cannot renumber exercises as it would exceed the available exercise number space")
        val renumberedExercises = Helpers.getExercisePrefixAndExercises_TBR(mainRepo)(config).exercises
        renumberedExercises shouldBe exercises
      }
    }
  }

end RenumberExercisesSpec
