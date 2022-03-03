package cmt

import cmt.admin.cli.{AdminCliParser, CliOptions}
import cmt.support.CommandLineParseTestBase

class CommandLineParseTest
    extends CommandLineParseTestBase[CliOptions](
      AdminCliParser.parse,
      StudentifyArguments,
      DuplicateInsertBeforeArguments,
      LinearizeArguments,
      DelinearizeArguments,
      RenumberArguments,
      VersionArguments)
