package cmt

import scopt.{OEffect, OParser}

object CmdLineParse:

  final case class CmdLineParseError(errors: List[OEffect])

  def parse(args: Array[String]): Either[CmdLineParseError, CmtaOptions] =
    OParser.runParser(cmtaParser, args, CmtaOptions()) match {
      case (result, effects) => handleParsingResult(result, effects)
    }

  private def handleParsingResult(
      maybeResult: Option[CmtaOptions],
      effects: List[OEffect]): Either[CmdLineParseError, CmtaOptions] =
    maybeResult match {
      case Some(validOptions) => Right(validOptions)
      case _                  => Left(CmdLineParseError(effects))
    }
