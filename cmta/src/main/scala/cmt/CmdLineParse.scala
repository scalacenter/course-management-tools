package cmt

object CmdLineParse extends CliParser:

  def parse(args: Array[String]): Either[CmdLineParseError, CmtaOptions] =
    _parse(cmtaParser, CmtaOptions())(args)
