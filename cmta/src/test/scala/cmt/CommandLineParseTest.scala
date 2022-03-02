package cmt

import cmt.support.CommandLineParseTestBase

class CommandLineParseTest
    extends CommandLineParseTestBase[CmtaOptions](
      CmdLineParse.parse,
      StudentifyArguments,
      DuplicateInsertBeforeArguments,
      LinearizeArguments,
      DelinearizeArguments,
      RenumberArguments,
      VersionArguments)
