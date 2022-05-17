package cmt.client.command.execution

import cmt.client.command.ClientCommand.Configure
import cmt.core.execution.Executable

given Executable[Configure.type] with
  extension (cmd: Configure.type)
    def execute(): Either[String, String] = {
      println("configuring")
      Right("")
    }

