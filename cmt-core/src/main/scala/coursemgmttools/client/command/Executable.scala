package coursemgmttools.client.command

import coursemgmttools.CmtError
import coursemgmttools.client.Configuration

trait Executable[T]:
  extension (t: T) def execute(configuration: Configuration): Either[CmtError, String]
