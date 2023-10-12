package coursemgmt.client.command

import coursemgmt.CmtError
import coursemgmt.client.Configuration

trait Executable[T]:
  extension (t: T) def execute(configuration: Configuration): Either[CmtError, String]
