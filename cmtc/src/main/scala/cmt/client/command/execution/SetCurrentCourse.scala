package cmt.client.command.execution

import cmt.client.{Configuration, CurrentCourse}
import cmt.client.Domain.StudentifiedRepo
import cmt.client.command.ClientCommand.SetCurrentCourse
import cmt.core.execution.Executable

given Executable[SetCurrentCourse] with
  extension (cmd: SetCurrentCourse)
    def execute(): Either[String, String] = {
      val updatedConfig = cmd.configuration.copy(currentCourse = CurrentCourse(cmd.studentifiedRepo))
      Configuration.save(updatedConfig)
      Right("")
    }
