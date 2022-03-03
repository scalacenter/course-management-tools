package cmt.core.cli

import scopt.{OEffect, OParser}

trait CliParser {

  protected def _parse[T](parser: OParser[_, T], options: T)(
      args: Array[String]): Either[cmt.core.cli.CmdLineParseError, T] =
    OParser.runParser(parser, args, options) match {
      case (result, effects) => handleParsingResult(result, effects)
    }

  private def handleParsingResult[T](
      maybeResult: Option[T],
      effects: List[OEffect]): Either[cmt.core.cli.CmdLineParseError, T] =
    maybeResult match {
      case Some(validOptions) => Right(validOptions)
      case _                  => Left(cmt.core.cli.CmdLineParseError(effects))
    }
}
