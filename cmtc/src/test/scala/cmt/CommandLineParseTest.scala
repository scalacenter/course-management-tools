package cmt

import cmt.support.CommandLineParseTestBase

class CommandLineParseTest extends CommandLineParseTestBase[CmtcOptions](CmdLineParse.parse)
