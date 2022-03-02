package cmt

import scopt.{OParser, OEffect}

object CmdLineParse extends CliParser:

  def parse(args: Array[String]): Either[CmdLineParseError, CmtcOptions] =
    _parse(parser, CmtcOptions())(args)
