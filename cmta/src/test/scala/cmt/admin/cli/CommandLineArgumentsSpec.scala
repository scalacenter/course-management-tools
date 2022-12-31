package cmt.admin.cli

import caseapp.Parser
import caseapp.core.Error.{RequiredOptionNotSpecified, SeveralErrors}
import cmt.CmtError
import cmt.support.EitherSupport
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableFor2
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.prop.Tables.Table
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import sbt.io.IO
import sbt.io.syntax.File
import cmt.toCmtError

abstract class CommandLineArgumentsSpec[T]
    extends AnyWordSpecLike
    with Matchers
    with BeforeAndAfterAll
    with ScalaCheckPropertyChecks
    with EitherSupport {

  val identifier: String
  val parser: Parser[T]
  def invalidArguments(tempDirectory: File): TableFor2[Seq[String], Set[CmtError]]
  def validArguments(tempDirectory: File): TableFor2[Seq[String], T]

  protected def invalidArgumentsTable(args: (Seq[String], Set[CmtError])*): TableFor2[Seq[String], Set[CmtError]] =
    Table(("args", "errors"), args*)

  protected def validArgumentsTable[T](args: (Seq[String], T)*): TableFor2[Seq[String], T] =
    Table(("args", "expectedResult"), args*)

  private val tempDirectory = IO.createTemporaryDirectory

  override def afterAll(): Unit =
    tempDirectory.delete()

  "CLI Parser" when {

    s"given invalid $identifier arguments" should {

      "report appropriate errors" in {
        forAll(invalidArguments(tempDirectory)) { (args: Seq[String], errors: Set[CmtError]) =>
          assertFailureWithErrors(parser, args.toArray, errors)
        }
      }
    }

    s"given valid $identifier arguments" should {

      "return expected results" in {
        forAll(validArguments(tempDirectory)) { (args: Seq[String], expectedResult: T) =>
          assertSuccessWithResponse(parser, args.toArray, expectedResult)
        }
      }
    }
  }

  private def assertFailureWithErrors(parser: Parser[T], args: Array[String], errors: Set[CmtError]) = {
    val resultOr = parser.parse(args.to(Seq))
    val error = assertLeft(resultOr)

    error match {
      case e: SeveralErrors              => e.toCmtError should contain theSameElementsAs errors
      case e: RequiredOptionNotSpecified => e.toCmtError shouldBe errors
      case _                             => throw new IllegalArgumentException(s"expected $errors but found $error")
    }
  }

  private def assertSuccessWithResponse(parser: Parser[T], args: Array[String], expectedResult: T) = {
    val resultOr = parser.parse(args.to(Seq)).map { case (result, _) =>
      result
    }
    val result = assertRight(resultOr)
    result shouldBe expectedResult
  }
}
