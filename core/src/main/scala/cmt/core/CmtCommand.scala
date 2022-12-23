package cmt.core

import caseapp.Command
import caseapp.core.Error
import caseapp.core.help.Help
import caseapp.core.parser.Parser
import cmt.{
  CmtError,
  toCmtError,
  RequiredOptionIsMissing,
  OptionName,
  FailedToValidateArgument,
  FailedToExecuteCommand,
  FailedToValidateCommandOptions,
  prettyPrint,
  printErrorAndExit
}

abstract class CmtCommand[T](implicit parser: Parser[T], help: Help[T]) extends Command[T] {

  override def error(message: Error): Nothing = {
    val error = message.toCmtError
    printErrorAndExit(error.map(prettyPrint).mkString("\n"))
  }
}
