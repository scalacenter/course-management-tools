package com.lunatech.cmt.client.cli

import caseapp.core.help.Help
import caseapp.core.parser.Parser
import cats.syntax.either.*
import com.lunatech.cmt.client.Configuration
import com.lunatech.cmt.core.cli.CmtCommand
import com.lunatech.cmt.printErrorAndExit

abstract class CmtcCommand[T](using parser: Parser[T], help: Help[T]) extends CmtCommand[T] {

  protected val configuration = Configuration
    .load()
    .leftMap(error => printErrorAndExit(error.prettyPrint))
    .getOrElse(printErrorAndExit("failed to load configuration"))
}
