package cmt

/** Copyright 2022 - Eric Loots - eric.loots@gmail.com / Trevor Burton-McCreadie - trevor@thinkmorestupidless.com
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *
  * See the License for the specific language governing permissions and limitations under the License.
  */

import scala.sys.process.Process
import scala.util.{Failure, Success, Try}

import sbt.io.syntax.*

object ProcessDSL:
  final case class CmdWithsWorkingDir(cmd: String, workingDir: String)

  final case class ProcessCmd(cmd: Seq[String], workingDir: File)

  extension (cmd: ProcessCmd)

    def runWithStatus(msg: String): Either[CmtError, Unit] = {
      val status = Try(Process(cmd.cmd, cmd.workingDir).!)
      status match
        case Success(_)  => Right(())
        case Failure(ex) => Left(FailedToExecuteCommand(ErrorMessage(msg)))
    }

    def runAndReadOutput(): Either[CmtError, String] =
      val consoleRes = Try(Process(cmd.cmd, cmd.workingDir).!!)
      consoleRes match
        case Success(result) => Right(result.trim)
        case Failure(_) =>
          val msg = s"""
                       |  Executed command: ${cmd.cmd.mkString(" ")}
                       |  Working directory: ${cmd.workingDir}
          """.stripMargin
          Left(FailedToExecuteCommand(ErrorMessage(msg)))
  end extension

  extension (command: String)
    def toProcessCmd(workingDir: File): ProcessCmd =
      val SplitRegex = "([^\"]\\S*|\".+?\")\\s*".r
      val cmdArgs =
        SplitRegex.findAllMatchIn(command).map(_.toString.replaceAll(" $", "").replaceAll(""""""", "")).toVector
      ProcessCmd(cmdArgs, workingDir)

end ProcessDSL
