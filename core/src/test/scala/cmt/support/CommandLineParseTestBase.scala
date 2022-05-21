package cmt.support

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

import cmt.core.cli.CmdLineParseError
import cmt.support.EitherSupport
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.Tables.Table
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

object CommandLineArguments {
  def invalidArgumentsTable(args: (Seq[String], Seq[ReportError])*): TableFor2[Seq[String], Seq[ReportError]] =
    Table(("args", "errors"), args*)

  def validArgumentsTable[T](args: (Seq[String], T)*): TableFor2[Seq[String], T] =
    Table(("args", "expectedResult"), args*)
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

        "report expected errors" in {
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
