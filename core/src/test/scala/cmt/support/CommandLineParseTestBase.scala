package cmt.support

import cmt.core.cli.CmdLineParseError
import cmt.support.EitherSupport
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableFor2
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import sbt.io.IO
import sbt.io.syntax.*
import scopt.OEffect.ReportError

import scala.language.postfixOps

trait CommandLineArguments[T] {
  val identifier: String
  def invalidArguments(tempDirectory: File): TableFor2[Seq[String], Seq[ReportError]]
  def validArguments(tempDirectory: File): TableFor2[Seq[String], T]
}

abstract class CommandLineParseTestBase[T](
    parseFn: Array[String] => Either[CmdLineParseError, T],
    commandArguments: CommandLineArguments[T]*)
    extends AnyWordSpecLike
    with Matchers
    with BeforeAndAfterAll
    with ScalaCheckPropertyChecks
    with EitherSupport {

  private val tempDirectory = IO.createTemporaryDirectory

  override def afterAll(): Unit =
    tempDirectory.delete()

  "CLI Parser" when {

    commandArguments.foreach { command =>
      s"given invalid ${command.identifier} arguments" should {

        "report appropriate errors" in {
          forAll(command.invalidArguments(tempDirectory)) { (args: Seq[String], errors: Seq[ReportError]) =>
            assertFailureWithErrors(args.toArray, errors*)
          }
        }
      }
    }

    commandArguments.foreach { command =>
      s"given valid ${command.identifier} arguments" should {

        "return expected results" in {
          forAll(command.validArguments(tempDirectory)) { (args: Seq[String], expectedResult: T) =>
            assertSuccessWithResponse(args.toArray, expectedResult)
          }
        }
      }
    }
  }

  private def assertFailureWithErrors(args: Array[String], errors: ReportError*) = {
    val resultOr = parseFn(args)
    val error = assertLeft(resultOr)
    (error.errors should contain).allElementsOf(errors)
  }

  private def assertSuccessWithResponse(args: Array[String], expectedResult: T) = {
    val resultOr = parseFn(args)
    val result = assertRight(resultOr)
    result shouldBe expectedResult
  }
}
