package cmt

import scopt.{OEffect, OParser}

final case class CmdLineParseError(errors: List[OEffect])

trait CliParser {

  protected def _parse[T](parser: OParser[_, T], options: T)(args: Array[String]): Either[CmdLineParseError, T] =
    OParser.runParser(parser, args, options) match {
      case (result, effects) => handleParsingResult(result, effects)
    }

  private def handleParsingResult[T](maybeResult: Option[T], effects: List[OEffect]): Either[CmdLineParseError, T] =
    maybeResult match {
      case Some(validOptions) => Right(validOptions)
      case _                  => Left(CmdLineParseError(effects))
    }
}
