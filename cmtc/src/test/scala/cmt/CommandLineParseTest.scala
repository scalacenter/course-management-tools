package cmt

import cmt.client.cli.{CliOptions, ClientCliParser}
import cmt.support.CommandLineParseTestBase

class CommandLineParseTest extends CommandLineParseTestBase[CliOptions](ClientCliParser.parse, VersionArguments)
