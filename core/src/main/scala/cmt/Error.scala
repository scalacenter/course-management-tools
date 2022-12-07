package cmt

sealed trait CmtError {
  def toDisplayString: String
}

final case class RequiredOptionIsMissing(option: OptionName) extends CmtError {
  override def toDisplayString: String =
    s"missing required option ${option.value}"
}
final case class FailedToValidateArgument(option: OptionName, reasons: Seq[ErrorMessage]) extends CmtError {
  override def toDisplayString: String =
    s"""Failed to validate arguments:${reasons.foreach(str => s"\n    $str")}"""
}
object FailedToValidateArgument:
  def because(option: String, messages: String*): FailedToValidateArgument =
    FailedToValidateArgument(OptionName(option), messages.map(ErrorMessage(_)))

final case class FailedToValidateCommandOptions(reasons: List[ErrorMessage]) extends CmtError {
  override def toDisplayString: String =
    s"""Failed to validate command options:${reasons.foreach(str => s"\n    $str")}"""
}

final case class FailedToExecuteCommand(reason: ErrorMessage) extends CmtError {
  override def toDisplayString: String =
    s"""Failed to execute command: ${reason.message}"""
}

case class OptionName(value: String)
case class ErrorMessage(message: String)

extension (errorMessage: String)
  def toExecuteCommandErrorMessage: FailedToExecuteCommand =
    FailedToExecuteCommand(ErrorMessage(errorMessage))
