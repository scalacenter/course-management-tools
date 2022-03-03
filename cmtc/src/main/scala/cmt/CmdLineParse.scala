package cmt

import cmt.core.cli.CliParser
import scopt.{OEffect, OParser}

object CmdLineParse extends CliParser:

  def parse(args: Array[String]): Either[cmt.core.cli.CmdLineParseError, CmtcOptions] =
    _parse(parser, CmtcOptions())(args)
