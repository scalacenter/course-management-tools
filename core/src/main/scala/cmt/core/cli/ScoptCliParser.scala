package cmt.core.cli

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

  def parse[T](parser: OParser[_, T], options: T)(args: Array[String]): Either[CmdLineParseError, T] =
    OParser.runParser(parser, args, options) match
      case (result, effects) => handleParsingResult(result, effects)

  private def handleParsingResult[T](maybeResult: Option[T], effects: List[OEffect]): Either[CmdLineParseError, T] =
    maybeResult match
      case Some(validOptions) => Right(validOptions)
      case _                  => Left(CmdLineParseError(effects))
}
