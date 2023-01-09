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

import cmt.Helpers.dumpStringToFile

import java.util.UUID
import java.nio.charset.StandardCharsets
import cmt.admin.Domain.MainRepository
import cmt.client.Domain.StudentifiedRepo
import cmt.support.{ExerciseMetadata, SourcesStruct}
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen}
import org.scalatest.featurespec.AnyFeatureSpecLike
import org.scalatest.matchers.should.Matchers
import support.*
import sbt.io.syntax.*
import sbt.io.IO as sbtio

trait StudentificationMovingBetweenExercisesFunctionalFixture {

  val testConfig: String =
    """cmt {
      |  main-repo-exercise-folder = code
      |  studentified-repo-solutions-folder = .cue
      |  studentified-saved-states-folder = .savedStates
      |  studentified-repo-active-exercise-folder = code
      |  linearized-repo-active-exercise-folder = code
      |  config-file-default-name = course-management.conf
      |  test-code-folders = [ "config", "readme/README.md" ]
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
                "config/example-1.yaml",
                "readme/README.md"
              ),
            readme = List("README.md"),
            main =
              List(
                "build.sbt",
                "src/main/cmt/Main.scala"
              )
          )
      )
      .addExercise(
        "exercise_002_desc" ->
          SourcesStruct(
            test =
              List(
                "config/example-1.yaml",
                "config/example-2.yaml"
              ),
            readme = List("README.md"),
            main =
              List(
                "build.sbt",
                "src/main/cmt/Main.scala"
              )
          )
      )
      .addExercise(
        "exercise_003_desc" ->
          SourcesStruct(
            test =
              List(
                "config/example-2.yaml",
                "config/example-3.yaml"
              ),
            readme = List("README.md"),
            main =
              List(
                "build.sbt",
                "src/main/cmt/Main.scala"
              )
          )
      )
    // @formatter:on
}

final class StudentificationMovingBetweenExercisesFunctionalSpec
    extends AnyFeatureSpecLike
    with Matchers
    with GivenWhenThen
    with StudentificationMovingBetweenExercisesFunctionalFixture
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

      When("we move back to the previous exercise")

      gotoPreviousExercise(cMTcConfig, studentifiedRepoFolder)

      Then(
        "readme and test code for that exercise should have been pulled in and deleted test code should not be present")

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

        val deletedTests: SourceFiles =
          exercises.getTestCode("exercise_003_desc") -- exercises.getTestCode("exercise_002_desc")
        actualCode.doesNotContain(deletedTests)
      }

      When("we move back to the first exercise")

      gotoPreviousExercise(cMTcConfig, studentifiedRepoFolder)

      Then(
        "readme and test code for that exercise should have been pulled in and deleted test code should not be present")

      {
        val actualCode: SourceFiles = extractCodeFromRepo(studentifiedRepoCodeFolder)
        // @formatter:off
        val expectedCode: SourceFiles =
          exercises.getTestCode("exercise_001_desc") ++
            exercises.getReadmeCode("exercise_001_desc") ++
            exercises.getMainCode("exercise_002_desc") ++
            dontTouchMeFile
        // @formatter:on
        actualCode shouldBe expectedCode

        val deletedTests: SourceFiles =
          exercises.getTestCode("exercise_002_desc") -- exercises.getTestCode("exercise_001_desc")
        actualCode.doesNotContain(deletedTests)
      }
    }
  }
}
