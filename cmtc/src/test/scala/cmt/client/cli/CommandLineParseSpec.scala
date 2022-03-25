package cmt.client.cli

import cmt.client.cli.VersionArguments
import cmt.client.cli.{CliOptions, ClientCliParser}
import cmt.support.CommandLineParseTestBase

class CommandLineParseSpec extends CommandLineParseTestBase[CliOptions](ClientCliParser.parse, VersionArguments)
