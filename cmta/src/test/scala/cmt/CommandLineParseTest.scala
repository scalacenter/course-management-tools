package cmt

import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import sbt.io.IO
import sbt.io.syntax.*
import scopt.OEffect.ReportError

class CommandLineParseTest extends AnyWordSpecLike with Matchers with BeforeAndAfterAll {

  private val tempDirectory = IO.createTemporaryDirectory

  override def afterAll(): Unit =
    tempDirectory.delete()

  "commmand line parser" when {

    "given the 'studentify' command" should {

      "fail if main repository argument and studentified directories are missing" in {
        val args = Array("studentify")
        val resultOr = CmdLineParse.parse(args)

        val error = assertLeft(resultOr)
        (error.errors should contain).allOf(
          ReportError("Missing argument <Main repo>"),
          ReportError("Missing argument <studentified repo parent folder>"))
      }

      "fail if main repository and studentified directories don't exist" in {
        val mainRepositoryPath = s"${tempDirectory.getAbsolutePath}/i/do/not/exist"
        val studentifiedDirectoryPath = s"${tempDirectory.getAbsolutePath}/neither/do/i"
        val args = Array("studentify", mainRepositoryPath, studentifiedDirectoryPath)
        val resultOr = CmdLineParse.parse(args)

        val error = assertLeft(resultOr)
        (error.errors should contain).allOf(
          ReportError(s"$mainRepositoryPath does not exist"),
          ReportError(s"$studentifiedDirectoryPath does not exist"))
      }

      "fail if main repository and studentified directories are files" in {
        val mainRepositoryPath = "./cmta/src/test/resources/i-am-a-file.txt"
        val studentifiedDirectoryPath = "./cmta/src/test/resources/i-am-a-directory/i-am-a-file-in-a-directory.txt"
        val args = Array("studentify", mainRepositoryPath, studentifiedDirectoryPath)
        val resultOr = CmdLineParse.parse(args)

        val error = assertLeft(resultOr)
        (error.errors should contain).allOf(
          ReportError(s"$mainRepositoryPath is not a directory"),
          ReportError(s"$studentifiedDirectoryPath is not a directory"))
      }

      "fail if main repository is not a git repository" in {
        val mainRepositoryPath = tempDirectory.getAbsolutePath
        val studentifiedDirectoryPath = "./cmta/src/test/resources/i-am-another-directory"
        val args = Array("studentify", mainRepositoryPath, studentifiedDirectoryPath)
        val resultOr = CmdLineParse.parse(args)

        val error = assertLeft(resultOr)
        error.errors should contain(ReportError(s"$mainRepositoryPath is not a git repository"))
      }

      "succeed if main repository and studentified directories exist and are directories" in {
        val mainRepositoryPath = "./cmta/src/test/resources/i-am-a-directory"
        val studentifiedDirectoryPath = "./cmta/src/test/resources/i-am-another-directory"
        val args = Array("studentify", mainRepositoryPath, studentifiedDirectoryPath)
        val resultOr = CmdLineParse.parse(args)

        val result = assertRight(resultOr)
        val expectedResult = CmtaOptions(
          Helpers.resolveMainRepoPath(file(mainRepositoryPath)).toOption.get,
          Studentify(
            Some(file(studentifiedDirectoryPath)),
            forceDeleteExistingDestinationFolder = false,
            initializeAsGitRepo = false))

        result shouldBe expectedResult
      }
    }
  }

  private def assertRight[E, T](either: Either[E, T]): T =
    either match {
      case Left(error)   => throw new IllegalStateException(s"Expected Either.right, got Either.left [$error]")
      case Right(result) => result
    }

  private def assertLeft[E, T](either: Either[E, T]): E =
    either match {
      case Left(error)   => error
      case Right(result) => throw new IllegalStateException(s"Expected Either.left, got Either.right [$result]")
    }
}
