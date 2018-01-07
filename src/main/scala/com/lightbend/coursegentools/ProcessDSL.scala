package com.lightbend.coursegentools

/**
  * Copyright © 2016 Lightbend, Inc
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *
  * NO COMMERCIAL SUPPORT OR ANY OTHER FORM OF SUPPORT IS OFFERED ON
  * THIS SOFTWARE BY LIGHTBEND, Inc.
  *
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

import java.io.File

import scala.sys.process.Process
import scala.util.Try

object ProcessDSL {
  case class ProcessCmd(cmd: Seq[String], workingDir: File)

  implicit class ProcessExtension(val cmd: ProcessCmd) extends AnyVal {
    def runAndExitIfFailed(errorMsg: String): Unit = {
      val status = Try(Process(cmd.cmd, cmd.workingDir).!)
      if (status.getOrElse(-1) != 0) {
        println(
          s"""
             |$errorMsg
             |  Executed command: ${cmd.cmd.mkString(" ")}
             |  Working directory: ${cmd.workingDir}
           """.stripMargin)
        System.exit(status.getOrElse(-1))
      }
    }

    def run: Int = {
      val status = Try(Process(cmd.cmd, cmd.workingDir).!)
      status.getOrElse(-1)
    }
  }

  implicit class StringToProcessExt(val command: String) extends AnyVal {
    def toProcessCmd(workingDir: File): ProcessCmd = {
      val cmdSeq = command.split("""\s+""").toVector
      ProcessCmd(cmdSeq, workingDir)
    }
  }
}
