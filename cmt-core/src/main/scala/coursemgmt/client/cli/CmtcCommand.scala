package coursemgmt.client.cli

import caseapp.core.help.Help
import caseapp.core.parser.Parser
import cats.syntax.either.*
import coursemgmt.client.Configuration
import coursemgmt.core.cli.CmtCommand
import coursemgmt.printErrorAndExit

abstract class CmtcCommand[T](using parser: Parser[T], help: Help[T]) extends CmtCommand[T] {

  protected val configuration = Configuration
    .load()
    .leftMap(error => printErrorAndExit(error.prettyPrint))
    .getOrElse(printErrorAndExit("failed to load configuration"))
}
