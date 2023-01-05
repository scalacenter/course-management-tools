package cmt.core

import caseapp.{Command, RemainingArgs}
import caseapp.core.Error
import caseapp.core.help.Help
import caseapp.core.parser.Parser
import cmt.{
  CmtError,
  FailedToExecuteCommand,
  FailedToValidateArgument,
  OptionName,
  RequiredOptionIsMissing,
  printErrorAndExit,
  toCmtError
}

abstract class CmtCommand[T](implicit parser: Parser[T], help: Help[T]) extends Command[T] {

  protected def enforceNoTrailingArguments(args: RemainingArgs): Unit =
    if (args.remaining.nonEmpty || args.unparsed.nonEmpty)
      printErrorAndExit(
        s"""unrecognised trailing arguments '${args.remaining.mkString(",")} -- ${args.unparsed.mkString(",")}'""")

  override def error(message: Error): Nothing = {
    val error = message.toCmtError
    printErrorAndExit(error.map(_.prettyPrint).mkString("\n"))
  }
}
