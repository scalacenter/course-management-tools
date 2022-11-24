package cmt

/** Copyright 2022 - Eric Loots - eric.loots@gmail.com / Trevor Burton-McCreadie - trevor@thinkmorestupidless.com
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *
  * See the License for the specific language governing permissions and limitations under the License.
  */

import cmt.Helpers.{dumpStringToFile, fileList}
import cmt.admin.Domain.MainRepository
import cmt.client.Domain.StudentifiedRepo
import cmt.client.cli.CliCommand.PullTemplate
import cmt.support.*
import org.scalatest.featurespec.AnyFeatureSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen}
import sbt.io.IO as sbtio
import sbt.io.syntax.*

import java.nio.charset.StandardCharsets
import java.util.UUID

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
}

final class StudentificationFunctionalSpec
    extends AnyFeatureSpecLike
    with Matchers
    with GivenWhenThen
    with StudentifiedRepoFixture
    with BeforeAndAfterAll {

  val tmpDir: File = sbtio.createTemporaryDirectory

  override def afterAll(): Unit =
    sbtio.delete(tmpDir)

  info("As a user")
  info("I want to be able to studentify a main repository")
  info("So I can share a course")

  Feature("Studentification") {

    Scenario("A user creates a main repository") {

      Given("a main repository")

      val exercises: Exercises = exerciseMetadata.toExercises
      val mainRepo = createMainRepo(tmpDir, "Functional-Test-Repo", exercises, testConfig)

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
        addFileToStudentifiedRepo(
          studentifiedRepoCodeFolder,
          Helpers.adaptToOSSeparatorChar(".mvn/someFolder/mustNotBeTouchedByCmt"))

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
        addFileToStudentifiedRepo(
          studentifiedRepoCodeFolder,
          Helpers.adaptToOSSeparatorChar("src/main/cmt/Main.scala")) ++
          addFileToStudentifiedRepo(
            studentifiedRepoCodeFolder,
            Helpers.adaptToOSSeparatorChar("src/main/cmt/pack/Toto.scala"))

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
        addFileToStudentifiedRepo(
          studentifiedRepoCodeFolder,
          Helpers.adaptToOSSeparatorChar(".mvn/someFolder/mustNotBeTouchedByCmt"))
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

      When(
        "Adding a file in the src code that isn't part of the current exercise test code and moving to the previous exercise")

      val uniqueTestCodeFile =
        addFileToStudentifiedRepo(
          studentifiedRepoCodeFolder,
          Helpers.adaptToOSSeparatorChar("src/test/cmt/pkg/T2-unique.scala"))
      gotoPreviousExercise(cMTcConfig, studentifiedRepoFolder)

      Then(
        "should be the solution for the first exercise + the pulled template file + the files in the template folder")

      {
        val actualCode = extractCodeFromRepo(studentifiedRepoCodeFolder)

        // @formatter:off
        val expectedCode =
          exercises.getMainCode("exercise_001_desc") ++
          exercises.getTestCode("exercise_003_desc") ++
          dontTouchMeFile_1 ++
          uniqueTestCodeFile ++
          exercises.getReadmeCode("exercise_003_desc") +
          exercises.getMainFile("exercise_004_desc", "src/main/cmt/sample/Sample1.scala") +
          exercises.getMainFile("exercise_004_desc", "src/main/cmt/template/Template1.scala") +
          exercises.getMainFile("exercise_004_desc", "src/main/cmt/template/Template2.scala")
        // @formatter:on

        actualCode shouldBe expectedCode
      }

      When("a test code file that is part of the solution is modified")

      val changedTestFile =
        addFileToStudentifiedRepo(
          studentifiedRepoCodeFolder,
          Helpers.adaptToOSSeparatorChar("src/test/cmt/pkg/T3.scala"))

      Then("any move (next, previous, first, random) to another exercise should generate an error")

      {
        val gotoPreviousExerciseResult = gotoPreviousExercise(cMTcConfig, studentifiedRepoFolder)
        val gotoPreviousExerciseExpectedResult = Left(s"""previous-exercise cancelled.
             |
             |${toConsoleYellow("You have modified the following file(s):")}
             |${toConsoleGreen(List("src/test/cmt/pkg/T3.scala").mkString("\n   ", "\n   ", "\n"))}
             |""".stripMargin)
        gotoPreviousExerciseResult shouldBe gotoPreviousExerciseExpectedResult

        val gotoNextExerciseResult = gotoNextExercise(cMTcConfig, studentifiedRepoFolder)
        val gotoNextExerciseExpectedResult = Left(s"""next-exercise cancelled.
             |
             |${toConsoleYellow("You have modified the following file(s):")}
             |${toConsoleGreen(List("src/test/cmt/pkg/T3.scala").mkString("\n   ", "\n   ", "\n"))}
             |""".stripMargin)
        gotoNextExerciseResult shouldBe gotoNextExerciseExpectedResult

        val gotoFirstExerciseResult = gotoFirstExercise(cMTcConfig, studentifiedRepoFolder)
        val gotoFirstExerciseExpectedResult = Left(s"""goto-exercise cancelled.
             |
             |${toConsoleYellow("You have modified the following file(s):")}
             |${toConsoleGreen(List("src/test/cmt/pkg/T3.scala").mkString("\n   ", "\n   ", "\n"))}
             |""".stripMargin)
        gotoFirstExerciseResult shouldBe gotoFirstExerciseExpectedResult

        val gotoSecondExerciseResult = gotoExercise(cMTcConfig, studentifiedRepoFolder, "exercise_002_desc")
        val gotoSecondExerciseExpectedResult = Left(s"""goto-exercise cancelled.
             |
             |${toConsoleYellow("You have modified the following file(s):")}
             |${toConsoleGreen(List("src/test/cmt/pkg/T3.scala").mkString("\n   ", "\n   ", "\n"))}
             |""".stripMargin)
        gotoSecondExerciseResult shouldBe gotoSecondExerciseExpectedResult

        val actualCode = extractCodeFromRepo(studentifiedRepoCodeFolder)

        // @formatter:off
        val expectedCode =
          exercises.getMainCode("exercise_001_desc") ++
          exercises.getTestCode("exercise_003_desc") ++
          dontTouchMeFile_1 ++
          uniqueTestCodeFile ++
          changedTestFile ++
          exercises.getReadmeCode("exercise_003_desc") +
          exercises.getMainFile("exercise_004_desc", "src/main/cmt/sample/Sample1.scala") +
          exercises.getMainFile("exercise_004_desc", "src/main/cmt/template/Template1.scala") +
          exercises.getMainFile("exercise_004_desc", "src/main/cmt/template/Template2.scala")
        // @formatter:on

        actualCode shouldBe expectedCode
      }

      When("a test code file that is part of the solution and that was modified is restored to its original content")

      // Let's first save the current state of the exercise so that we can restore it for other test scenarios
      saveState(cMTcConfig, studentifiedRepoFolder)
      // Restore the test code file to its original content
      pullTemplate(cMTcConfig, studentifiedRepoFolder, "src/test/cmt/pkg/T3.scala")

      Then("it should be possible to move to the next exercise without forcing it")

      {
        val gotoNextExerciseResult = gotoNextExercise(cMTcConfig, studentifiedRepoFolder)
        val gotoNextExerciseExpectedResult = Symbol("right")
        gotoNextExerciseResult shouldBe gotoNextExerciseExpectedResult

        val actualCode = extractCodeFromRepo(studentifiedRepoCodeFolder)

        // @formatter:off
        val expectedCode =
          exercises.getMainCode("exercise_001_desc") ++
          exercises.getTestCode("exercise_004_desc") ++
          dontTouchMeFile_1 ++
          uniqueTestCodeFile ++
          exercises.getReadmeCode("exercise_004_desc") +
          exercises.getMainFile("exercise_004_desc", "src/main/cmt/sample/Sample1.scala") +
          exercises.getMainFile("exercise_004_desc", "src/main/cmt/template/Template1.scala") +
          exercises.getMainFile("exercise_004_desc", "src/main/cmt/template/Template2.scala")
        // @formatter:on

        actualCode shouldBe expectedCode
      }

      When("an exercise state is restored that contains a modified test code file")

      // Start from a known (saved) state in previous scenario
      restoreState(cMTcConfig, studentifiedRepoFolder, "exercise_003_desc")

      Then("moving to the next exercise should generate an error unless the move is forced")

      {
        val gotoNextExerciseResult = gotoNextExercise(cMTcConfig, studentifiedRepoFolder)
        val gotoNextExerciseExpectedResult = Left(s"""next-exercise cancelled.
             |
             |${toConsoleYellow("You have modified the following file(s):")}
             |${toConsoleGreen(List("src/test/cmt/pkg/T3.scala").mkString("\n   ", "\n   ", "\n"))}
             |""".stripMargin)
        gotoNextExerciseResult shouldBe gotoNextExerciseExpectedResult

        val actualCode = extractCodeFromRepo(studentifiedRepoCodeFolder)

        // @formatter:off
        val expectedCode =
          exercises.getMainCode("exercise_001_desc") ++
          exercises.getTestCode("exercise_003_desc") ++
          dontTouchMeFile_1 ++
          uniqueTestCodeFile ++
          changedTestFile ++
          exercises.getReadmeCode("exercise_003_desc") +
          exercises.getMainFile("exercise_004_desc", "src/main/cmt/sample/Sample1.scala") +
          exercises.getMainFile("exercise_004_desc", "src/main/cmt/template/Template1.scala") +
          exercises.getMainFile("exercise_004_desc", "src/main/cmt/template/Template2.scala")
        // @formatter:on

        actualCode shouldBe expectedCode

        // Repeat the move but force it
        val gotoNextExerciseForcedResult = gotoNextExerciseForced(cMTcConfig, studentifiedRepoFolder)
        val gotoNextExerciseForcedExpectedResult = Symbol("right")

        gotoNextExerciseForcedResult shouldBe gotoNextExerciseForcedExpectedResult

        val actualCodeForced = extractCodeFromRepo(studentifiedRepoCodeFolder)

        // @formatter:off
        val expectedCodeForced =
          exercises.getMainCode("exercise_001_desc") ++
          exercises.getTestCode("exercise_004_desc") ++
          dontTouchMeFile_1 ++
          uniqueTestCodeFile ++
          exercises.getReadmeCode("exercise_004_desc") +
          exercises.getMainFile("exercise_004_desc", "src/main/cmt/sample/Sample1.scala") +
          exercises.getMainFile("exercise_004_desc", "src/main/cmt/template/Template1.scala") +
          exercises.getMainFile("exercise_004_desc", "src/main/cmt/template/Template2.scala")
        // @formatter:on

        actualCodeForced shouldBe expectedCodeForced
      }

      When("an exercise state is restored that contains a modified test code file")

      // Start from a known (saved) state
      restoreState(cMTcConfig, studentifiedRepoFolder, "exercise_003_desc")

      Then(
        "moving the modified file to a different path (not part of the existing test code paths) should make a move to another exercise possible")

      {
        val movedFile = changedTestFile.moveFile(
          studentifiedRepoCodeFolder,
          "src/test/cmt/pkg/T3.scala",
          "src/test/cmt/pkg/T3NotInTestCode.scala")
        val gotoNextExerciseResult = gotoNextExercise(cMTcConfig, studentifiedRepoFolder)

        val gotoNextExerciseExpectedResult = Symbol("right")
        gotoNextExerciseResult shouldBe gotoNextExerciseExpectedResult

        val actualCode = extractCodeFromRepo(studentifiedRepoCodeFolder)

        // @formatter:off
        val expectedCode =
          exercises.getMainCode("exercise_001_desc") ++
          exercises.getTestCode("exercise_004_desc") ++
          dontTouchMeFile_1 ++
          uniqueTestCodeFile ++
          movedFile ++
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
