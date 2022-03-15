package cmt

import cmt.Helpers.dumpStringToFile

import java.util.UUID
import java.nio.charset.StandardCharsets
import cmt.admin.Domain.MainRepository
import cmt.client.Domain.StudentifiedRepo
import cmt.client.cli.CliCommand.PullTemplate
import cmt.support.{ExerciseMetadata, SourcesStruct}
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen}
import org.scalatest.featurespec.AnyFeatureSpecLike
import org.scalatest.matchers.should.Matchers
import support.*
import sbt.io.syntax.*
import sbt.io.IO as sbtio

trait StudentifiedRepoFixture {

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
      |  cmt-studentified-dont-touch = [ ".mvn" ]
      |}""".stripMargin

  val exerciseMetadata: ExerciseMetadata =
    // @formatter:off
    ExerciseMetadata()
      .addExercise(
        "exercise_001_desc" ->
          SourcesStruct(
            test =
              List(
                "src/test/cmt/T1.scala",
                "src/test/cmt/pkg/T2.scala"
              ),
            readme = List("README.md"),
            main =
              List(
                "build.sbt",
                "src/main/cmt/Main.scala")
          )
      )
      .addExercise(
        "exercise_002_desc" ->
          SourcesStruct(
            test =
              List(
                "src/test/cmt/T1.scala",
                "src/test/cmt/pkg/T2.scala"
              ),
            readme = List("README.md"),
            main =
              List(
                "build.sbt",
                "src/main/cmt/Main.scala",
                "src/main/cmt/Main1.scala")
          )
      )
      .addExercise(
        "exercise_003_desc" ->
          SourcesStruct(
            test =
              List(
                "src/test/cmt/T1.scala",
                "src/test/cmt/pkg/T3.scala"
              ),
            readme = List("README.md"),
            main =
              List(
                "build.sbt",
                "src/main/cmt/Main1.scala"
              )
          )
      )
      .addExercise("exercise_004_desc" ->
        SourcesStruct(
          test =
            List(
              "src/test/cmt/T1.scala",
              "src/test/cmt/pkg/T3.scala"
            ),
          readme = List("README.md"),
          main = List(
            "build.sbt",
            "src/main/cmt/Main1.scala",
            "src/main/cmt/sample/Sample1.scala",
            "src/main/cmt/sample/Sample2.scala",
            "src/main/cmt/template/Template1.scala",
            "src/main/cmt/template/Template2.scala"
          )
        )
      )
    // @formatter:on

  def createMainRepo(tmpDir: File, repoName: String, exercises: Exercises): File =
    val mainRepo = tmpDir / repoName
    sbtio.touch(mainRepo / "course-management.conf")
    dumpStringToFile(testConfig, mainRepo / "course-management.conf")
    println(s"Mainrepo = $mainRepo")
    exercises.createRepo(mainRepo, "code")
    mainRepo

  def studentifyMainRepo(tmpDir: File, repoName: String, mainRepo: File): File =
    import cmt.admin.Domain.{ForceDeleteDestinationDirectory, InitializeGitRepo, StudentifyBaseDirectory}
    import cmt.admin.command.AdminCommand.Studentify
    import cmt.admin.command.execution.given
    val studentifyBase = tmpDir / "stu"
    sbtio.createDirectory(studentifyBase)
    val cmd = Studentify(
      MainRepository(mainRepo),
      new CMTaConfig(mainRepo, Some(mainRepo / "course-management.conf")),
      StudentifyBaseDirectory(studentifyBase),
      forceDeleteDestinationDirectory = ForceDeleteDestinationDirectory(false),
      initializeAsGitRepo = InitializeGitRepo(false))
    cmd.execute()
    studentifyBase / repoName

  def extractCodeFromRepo(codeFolder: File): SourceFiles =
    val files = Helpers.fileList(codeFolder)
    val filesAndChecksums = for {
      file <- files
      Some(fileName) = file.relativeTo(codeFolder): @unchecked
      checksum = java.util.UUID.fromString(sbtio.readLines(file, StandardCharsets.UTF_8).head)
    } yield (fileName.getPath, checksum)
    SourceFiles(filesAndChecksums.to(Map))

  def gotoNextExercise(config: CMTcConfig, studentifiedRepo: File): Unit =
    import cmt.client.command.ClientCommand.NextExercise
    import cmt.client.command.execution.given
    NextExercise(config, StudentifiedRepo(studentifiedRepo)).execute()

  def pullSolution(config: CMTcConfig, studentifiedRepo: File): Unit =
    import cmt.client.command.ClientCommand.PullSolution
    import cmt.client.command.execution.given
    PullSolution(config, StudentifiedRepo(studentifiedRepo)).execute()

  def gotoExercise(config: CMTcConfig, studentifiedRepo: File, exercise: String): Unit =
    import cmt.client.command.ClientCommand.GotoExercise
    import cmt.client.Domain.ExerciseId
    import cmt.client.command.execution.given
    GotoExercise(config, StudentifiedRepo(studentifiedRepo), ExerciseId(exercise)).execute()

  def gotoFirstExercise(config: CMTcConfig, studentifiedRepo: File): Unit =
    import cmt.client.command.ClientCommand.GotoFirstExercise
    import cmt.client.command.execution.given
    GotoFirstExercise(config, StudentifiedRepo(studentifiedRepo)).execute()

  def saveState(config: CMTcConfig, studentifiedRepo: File): Unit =
    import cmt.client.command.ClientCommand.SaveState
    import cmt.client.command.execution.given
    SaveState(config, StudentifiedRepo(studentifiedRepo)).execute()

  def restoreState(config: CMTcConfig, studentifiedRepo: File, exercise: String): Unit =
    import cmt.client.command.ClientCommand.RestoreState
    import cmt.client.Domain.ExerciseId
    import cmt.client.command.execution.given
    RestoreState(config, StudentifiedRepo(studentifiedRepo), ExerciseId(exercise)).execute()

  def pullTemplate(config: CMTcConfig, studentifiedRepo: File, templatePath: String): Unit =
    import cmt.client.command.ClientCommand.PullTemplate
    import cmt.client.Domain.TemplatePath
    import cmt.client.command.execution.given
    PullTemplate(config, StudentifiedRepo(studentifiedRepo), TemplatePath(templatePath)).execute()

  def addFileToStudentifiedRepo(studentifiedRepo: File, filePath: String): SourceFiles =
    val fileContent = UUID.randomUUID()
    sbtio.touch(studentifiedRepo / filePath)
    dumpStringToFile(fileContent.toString, studentifiedRepo / filePath)
    SourceFiles(Map(filePath -> fileContent))

}

final class StudentificationFunctionalSpec
    extends AnyFeatureSpecLike
    with Matchers
    with GivenWhenThen
    with StudentifiedRepoFixture
    with BeforeAndAfterAll {

  val tmpDir: File = sbtio.createTemporaryDirectory

  override def afterAll(): Unit =
    println("deleting temp directory")
    sbtio.delete(tmpDir)

  info("As a user")
  info("I want to be able to studentify a main repository")
  info("So I can share a course")

  Feature("Studentification") {

    Scenario("A user creates a main repository") {
      Given("a main repository")

      val exercises: Exercises = exerciseMetadata.toExercises
      val mainRepo = createMainRepo(tmpDir, "Functional-Test-Repo", exercises)

      When("the main repository is studentified")

      val studentifiedRepoFolder = studentifyMainRepo(tmpDir, "Functional-Test-Repo", mainRepo)
      val cMTcConfig: CMTcConfig = CMTcConfig(studentifiedRepoFolder)

      val studentifiedRepoCodeFolder = studentifiedRepoFolder / "code"

      Then("the result is a studentified repository positioned at the first exercise")

      {
        val actualCode = extractCodeFromRepo(studentifiedRepoCodeFolder)
        val expectedCode = exercises.getAllCode("exercise_001_desc")
        actualCode shouldBe expectedCode
      }

      When("a file is added to a folder that is marked as 'don't touch' in the main configuration")

      val dontTouchMeFile =
        addFileToStudentifiedRepo(studentifiedRepoCodeFolder, ".mvn/someFolder/mustNotBeTouchedByCmt")

      Then("We should see that file as part of the overall file set")

      {
        val actualCode = extractCodeFromRepo(studentifiedRepoCodeFolder)
        val expectedCode = exercises.getAllCode("exercise_001_desc") ++ dontTouchMeFile
        actualCode shouldBe expectedCode
      }

      When("the studentified repository is moved to the second exercise")

      gotoNextExercise(cMTcConfig, studentifiedRepoFolder)

      Then(
        "readme and test code for that exercise should have been pulled in and deleted test code should not be present")

      {
        val actualCode: SourceFiles = extractCodeFromRepo(studentifiedRepoCodeFolder)
        // @formatter:off
        val expectedCode: SourceFiles =
          exercises.getTestCode("exercise_002_desc") ++
          exercises.getReadmeCode("exercise_002_desc") ++
          exercises.getMainCode("exercise_001_desc") ++
          dontTouchMeFile
        // @formatter:on
        actualCode shouldBe expectedCode

        val deletedTests: SourceFiles =
          exercises.getTestCode("exercise_001_desc") -- exercises.getTestCode("exercise_002_desc")
        actualCode.doesNotContain(deletedTests)

      }

      When("pulling the solution for an exercise")

      pullSolution(cMTcConfig, studentifiedRepoFolder)

      Then("main code should be pulled in")

      {
        val actualCode = extractCodeFromRepo(studentifiedRepoCodeFolder)
        // @formatter:off
        val expectedCode =
          exercises.getTestCode("exercise_002_desc") ++
          exercises.getReadmeCode("exercise_002_desc") ++
          exercises.getMainCode("exercise_002_desc") ++
          dontTouchMeFile
        // @formatter:on
        actualCode shouldBe expectedCode
      }

      When("the studentified is moved to the third exercise")

      gotoNextExercise(cMTcConfig, studentifiedRepoFolder)

      Then(
        "readme and test code for that exercise should have been pulled in and deleted test code should not be present")

      {
        val actualCode: SourceFiles = extractCodeFromRepo(studentifiedRepoCodeFolder)
        // @formatter:off
        val expectedCode: SourceFiles =
          exercises.getTestCode("exercise_003_desc") ++
          exercises.getReadmeCode("exercise_003_desc") ++
          exercises.getMainCode("exercise_002_desc") ++
          dontTouchMeFile
        // @formatter:on
        actualCode shouldBe expectedCode

        val deletedTests: SourceFiles =
          exercises.getTestCode("exercise_002_desc") -- exercises.getTestCode("exercise_003_desc")
        actualCode.doesNotContain(deletedTests)
      }

      When(
        "the solution for the third exercise is pulled and the studentified is moved to the second exercise using the 'goto-exercise' functionality")

      pullSolution(cMTcConfig, studentifiedRepoFolder)
      gotoExercise(cMTcConfig, studentifiedRepoFolder, "exercise_002_desc")

      Then(
        "readme and test code for that exercise should have been pulled in and deleted test code should not be present and the main code for the third exercise should have been pulled")

      {
        val actualCode: SourceFiles = extractCodeFromRepo(studentifiedRepoCodeFolder)
        // @formatter:off
        val expectedCode: SourceFiles =
          exercises.getTestCode("exercise_002_desc") ++
          exercises.getReadmeCode("exercise_002_desc") ++
          exercises.getMainCode("exercise_003_desc") ++
          dontTouchMeFile
        // @formatter:on
        actualCode shouldBe expectedCode

        val deletedTests: SourceFiles =
          exercises.getTestCode("exercise_003_desc") -- exercises.getTestCode("exercise_002_desc")
        actualCode.doesNotContain(deletedTests)
      }

      When("moving to the first exercise using the goto-first-exercise command")

      gotoFirstExercise(cMTcConfig, studentifiedRepoFolder)

      Then(
        "readme and test code for that exercise should have been pulled and the main code for the third exercise should be there")

      {
        val actualCode: SourceFiles = extractCodeFromRepo(studentifiedRepoCodeFolder)
        // @formatter:off
        val expectedCode: SourceFiles =
          exercises.getTestCode("exercise_001_desc") ++
          exercises.getReadmeCode("exercise_001_desc") ++
          exercises.getMainCode("exercise_003_desc") ++
          dontTouchMeFile
        // @formatter:on
        actualCode shouldBe expectedCode

        val deletedTests: SourceFiles =
          exercises.getTestCode("exercise_003_desc") -- exercises.getTestCode("exercise_002_desc")
        actualCode.doesNotContain(deletedTests)
      }

      When("the current state of an exercise is mutated and subsequently saved with the 'save-state' command")

      val modifiedMainCode =
        addFileToStudentifiedRepo(studentifiedRepoCodeFolder, "src/main/cmt/Main.scala") ++
          addFileToStudentifiedRepo(studentifiedRepoCodeFolder, "src/main/cmt/pack/Toto.scala")

      saveState(cMTcConfig, studentifiedRepoFolder)

      Then("this should be reflected in the studentified repository")

      {
        val actualCode: SourceFiles = extractCodeFromRepo(studentifiedRepoCodeFolder)
        // @formatter:off
        val expectedCode: SourceFiles =
          exercises.getTestCode("exercise_001_desc") ++
          exercises.getReadmeCode("exercise_001_desc") ++
          exercises.getMainCode("exercise_003_desc") ++
          dontTouchMeFile ++
          modifiedMainCode
        // @formatter:on
        actualCode shouldBe expectedCode
      }

      When("moving to the second exercise and pulling the solution")

      gotoExercise(cMTcConfig, studentifiedRepoCodeFolder, "exercise_002_desc")
      pullSolution(cMTcConfig, studentifiedRepoFolder)

      Then("the current studentified repo should reflect that")

      {
        val actualCode: SourceFiles = extractCodeFromRepo(studentifiedRepoCodeFolder)
        // @formatter:off
        val expectedCode: SourceFiles =
          exercises.getTestCode("exercise_002_desc") ++
          exercises.getReadmeCode("exercise_002_desc") ++
          exercises.getMainCode("exercise_002_desc") ++
          dontTouchMeFile
        // @formatter:on
        actualCode shouldBe expectedCode
      }

      When("restoring the previously saved state using the 'restore-state' command")

      val dontTouchMeFile_1 =
        addFileToStudentifiedRepo(studentifiedRepoCodeFolder, ".mvn/someFolder/mustNotBeTouchedByCmt")
      restoreState(cMTcConfig, studentifiedRepoFolder, "exercise_001_desc")

      Then("should fully reflect what was saved")

      {
        val actualCode: SourceFiles = extractCodeFromRepo(studentifiedRepoCodeFolder)
        // @formatter:off
        val expectedCode: SourceFiles =
          exercises.getTestCode("exercise_001_desc") ++
          exercises.getReadmeCode("exercise_001_desc") ++
          exercises.getMainCode("exercise_003_desc") ++
          dontTouchMeFile_1 ++
          modifiedMainCode
        // @formatter:on
        actualCode shouldBe expectedCode
      }

      When(
        "moving to and pulling the solution for the first exercise, moving to the last exercise and pulling a template file")

      gotoFirstExercise(cMTcConfig, studentifiedRepoFolder)
      pullSolution(cMTcConfig, studentifiedRepoFolder)
      gotoExercise(cMTcConfig, studentifiedRepoFolder, "exercise_004_desc")
      pullTemplate(cMTcConfig, studentifiedRepoFolder, "src/main/cmt/sample/Sample1.scala")

      Then("should be the solution for the first exercise + the pulled in template file")

      {
        val actualCode = extractCodeFromRepo(studentifiedRepoCodeFolder)

        // @formatter:off
        val expectedCode =
          exercises.getMainCode("exercise_001_desc") ++
          exercises.getTestCode("exercise_004_desc") ++
          dontTouchMeFile_1 ++
          exercises.getReadmeCode("exercise_004_desc") +
          exercises.getMainFile("exercise_004_desc", "src/main/cmt/sample/Sample1.scala")
        // @formatter:on
        actualCode shouldBe expectedCode
      }

      When("having already pulled one template file for the last exercise, pulling in a template folder")

      pullTemplate(cMTcConfig, studentifiedRepoFolder, "src/main/cmt/template")

      Then(
        "should be the solution for the first exercise + the pulled template file + the files in the template folder")

      {
        val actualCode = extractCodeFromRepo(studentifiedRepoCodeFolder)

        // @formatter:off
        val expectedCode =
          exercises.getMainCode("exercise_001_desc") ++
          exercises.getTestCode("exercise_004_desc") ++
          dontTouchMeFile_1 ++
          exercises.getReadmeCode("exercise_004_desc") +
          exercises.getMainFile("exercise_004_desc", "src/main/cmt/sample/Sample1.scala") +
          exercises.getMainFile("exercise_004_desc", "src/main/cmt/template/Template1.scala") +
          exercises.getMainFile("exercise_004_desc", "src/main/cmt/template/Template2.scala")
        // @formatter:on

        actualCode shouldBe expectedCode
      }
    }
  }
}
