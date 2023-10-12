package coursemgmt.core.cli

import caseapp.{Command, RemainingArgs}
import caseapp.core.Error
import caseapp.core.help.Help
import caseapp.core.parser.Parser
import coursemgmt.{
  CmtError,
  MissingTrailingArguments,
  NoTrailingArguments,
  UnexpectedTrailingArguments,
  UnexpectedUnparsedArguments,
  printErrorAndExit,
  toCmtError
}

extension (self: RemainingArgs)
  def enforceNoTrailingArguments(): Either[CmtError, RemainingArgs] =
    enforceTrailingArgumentCount(expectedCount = 0)

  def enforceTrailingArgumentCount(expectedCount: Int): Either[CmtError, RemainingArgs] = {
    (self.unparsed.toList, self.remaining.toList) match {
      case (Nil, Nil) if expectedCount != 0 => Left(NoTrailingArguments(expectedCount))
      case (Nil, remaining) if remaining.size < expectedCount =>
        Left(MissingTrailingArguments(expectedCount, remaining.size))
      case (Nil, remaining) if remaining.size > expectedCount => Left(UnexpectedTrailingArguments(remaining))
      case (Nil, _)                                           => Right(self)
      case (unparsed, _)                                      => Left(UnexpectedUnparsedArguments(unparsed))
    }
  }

abstract class CmtCommand[T](using parser: Parser[T], help: Help[T]) extends Command[T] {

  override def error(message: Error): Nothing = {
    val error = message.toCmtError
    printErrorAndExit(error.map(_.prettyPrint).mkString("\n"))
  }
}
