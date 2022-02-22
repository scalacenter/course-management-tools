package cmt

import scopt.OParser

object CmdLineParse:

  def parse(args: Array[String]): Option[CmtcOptions] =
    OParser.parse(parser, args, CmtcOptions())
