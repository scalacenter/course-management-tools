package cmt.admin.cli

import cmt.admin.cli.{AdminCliParser, CliOptions}
import cmt.support.CommandLineParseTestBase
import cmt.*

class CommandLineParseSpec
    extends CommandLineParseTestBase[CliOptions](
      AdminCliParser.parse,
      StudentifyArguments,
      DuplicateInsertBeforeArguments,
      LinearizeArguments,
      DelinearizeArguments,
      RenumberArguments,
      VersionArguments)
