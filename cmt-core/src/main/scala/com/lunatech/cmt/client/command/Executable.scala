package com.lunatech.cmt.client.command

import com.lunatech.cmt.CmtError
import com.lunatech.cmt.client.Configuration

trait Executable[T]:
  extension (t: T) def execute(configuration: Configuration): Either[CmtError, String]
