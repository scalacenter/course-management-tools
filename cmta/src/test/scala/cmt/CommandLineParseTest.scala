package cmt

import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableFor2
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import sbt.io.IO
import sbt.io.syntax.*
import scopt.OEffect.ReportError

import scala.language.postfixOps

trait CommandLineArguments {
  val identifier: String
  def invalidArguments(tempDirectory: File): TableFor2[Seq[String], Seq[ReportError]]
  def validArguments(tempDirectory: File): TableFor2[Seq[String], CmtaOptions]
}

class CommandLineParseTest extends AnyWordSpecLike with Matchers with BeforeAndAfterAll with ScalaCheckPropertyChecks {

  private val tempDirectory = IO.createTemporaryDirectory

  override def afterAll(): Unit =
    tempDirectory.delete()

  private val commandArguments = List(StudentifyArguments, DuplicateInsertBeforeArguments, LinearizeArguments, DelinearizeArguments, RenumberArguments)

  "CLI Parser" when {

    commandArguments.foreach { command =>

      s"given invalid ${command.identifier} arguments" should {

        "report appropriate errors" in {
          forAll(command.invalidArguments(tempDirectory)) { (args: Seq[String], errors: Seq[ReportError]) =>
            assertFailureWithErrors(args.toArray, errors *)
          }
        }
      }
    }

    commandArguments.foreach { command =>

      s"given valid ${command.identifier} arguments" should {

        "return expected results" in {
          forAll(command.validArguments(tempDirectory)) { (args: Seq[String], expectedResult: CmtaOptions) =>
            assertSuccessWithResponse(args.toArray, expectedResult)
          }
        }
      }
    }
  }

  private def assertFailureWithErrors(args: Array[String], errors: ReportError*) = {
    val resultOr = CmdLineParse.parse(args)
    val error = assertLeft(resultOr)
    (error.errors should contain).allElementsOf(errors)
  }

  private def assertSuccessWithResponse(args: Array[String], expectedResult: CmtaOptions) = {
    val resultOr = CmdLineParse.parse(args)
    val result = assertRight(resultOr)
    result shouldBe expectedResult
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
