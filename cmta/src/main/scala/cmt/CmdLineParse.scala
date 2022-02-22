package cmt

import scopt.OParser

object CmdLineParse:

  def cmtaParse(args: Array[String]): Option[CmtaOptions] =
    OParser.parse(cmtaParser, args, CmtaOptions())
