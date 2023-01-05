package cmt.client.cli

import caseapp.core.help.Help
import caseapp.core.parser.Parser
import cmt.printErrorAndExit
import cmt.client.Configuration
import cmt.core.cli.CmtCommand
import cats.syntax.either.*

abstract class CmtcCommand[T](implicit parser: Parser[T], help: Help[T]) extends CmtCommand[T] {

  protected val configuration = Configuration
    .load()
    .leftMap(error => printErrorAndExit(error.prettyPrint))
    .getOrElse(printErrorAndExit("failed to load configuration"))
}
