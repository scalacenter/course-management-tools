package cmt.core.cli

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

import scopt.OEffect.{DisplayToErr, ReportError}
import scopt.{OEffect, OParser}

final case class CmdLineParseError(errors: List[OEffect]) {

  def toErrorString(): String = {
    val errorMessage = errors.collect { case ReportError(msg) => msg }.mkString("", "\n          ", "")
    val displayMessage = errors.collect { case DisplayToErr(msg) => msg }.mkString("\n")
    s"""$errorMessage
       |$displayMessage
       |""".stripMargin
  }
}

object ScoptCliParser {

  def parse[T](parser: OParser[?, T], options: T)(args: Array[String]): Either[CmdLineParseError, T] =
    OParser.runParser(parser, args, options) match
      case (result, effects) => handleParsingResult(result, effects)

  private def handleParsingResult[T](maybeResult: Option[T], effects: List[OEffect]): Either[CmdLineParseError, T] =
    maybeResult match
      case Some(validOptions) => Right(validOptions)
      case _                  => Left(CmdLineParseError(effects))
}
