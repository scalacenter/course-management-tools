package cmt

import scopt.OParser

object CmdLineParse:

  def parse(args: Array[String]): Option[CmdOptions] =
    OParser.parse(parser, args, CmdOptions())



        