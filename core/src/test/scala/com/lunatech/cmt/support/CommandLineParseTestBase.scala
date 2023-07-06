package com.lunatech.cmt.support

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

import caseapp.Parser
import caseapp.core.Error
import caseapp.core.Error.SeveralErrors
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.Tables.Table
import org.scalatest.prop.TableFor2
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import sbt.io.IO
import sbt.io.syntax.*
import com.lunatech.cmt.{CmtError, toCmtError}

import scala.language.postfixOps

trait CommandLineArguments[T] {
  val identifier: String
  val parser: Parser[T]
  def invalidArguments(tempDirectory: File): TableFor2[Seq[String], Set[CmtError]]
  def validArguments(tempDirectory: File): TableFor2[Seq[String], T]
}

object CommandLineArguments {
  def invalidArgumentsTable(args: (Seq[String], Set[CmtError])*): TableFor2[Seq[String], Set[CmtError]] =
    Table(("args", "errors"), args*)

  def validArgumentsTable[T](args: (Seq[String], T)*): TableFor2[Seq[String], T] =
    Table(("args", "expectedResult"), args*)
}

abstract class CommandLineParseTestBase[T](commandArguments: CommandLineArguments[T]*)
    extends AnyWordSpecLike
    with Matchers
    with BeforeAndAfterAll
    with ScalaCheckPropertyChecks
    with EitherSupport {

  private val tempDirectory = IO.createTemporaryDirectory

  override def afterAll(): Unit =
    val _ = tempDirectory.delete()

  "CLI Parser" when {

    commandArguments.foreach { command =>
      s"given invalid ${command.identifier} arguments" should {

        "report appropriate errors" in {
          forAll(command.invalidArguments(tempDirectory)) { (args: Seq[String], errors: Set[CmtError]) =>
            assertFailureWithErrors(command.parser, args.toArray, errors)
          }
        }
      }
    }

    commandArguments.foreach { command =>
      s"given valid ${command.identifier} arguments" should {

        "return expected results" in {
          forAll(command.validArguments(tempDirectory)) { (args: Seq[String], expectedResult: T) =>
            assertSuccessWithResponse(command.parser, args.toArray, expectedResult)
          }
        }
      }
    }
  }

  private def assertFailureWithErrors(parser: Parser[T], args: Array[String], errors: Set[CmtError]) = {
    val resultOr = parser.parse(args.to(Seq))
    val error = assertLeft(resultOr)

    error match {
      case e: SeveralErrors => e.toCmtError should contain theSameElementsAs errors
      case _                => throw new IllegalArgumentException(s"expected SeveralErrors but found $error")
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
