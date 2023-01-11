package cmt.client.command

import cmt.CmtError
import cmt.client.Configuration

trait Executable[T]:
  extension (t: T) def execute(configuration: Configuration): Either[CmtError, String]
