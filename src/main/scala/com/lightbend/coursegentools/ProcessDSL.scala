package com.lightbend.coursegentools

/**
  * Copyright © 2014, 2015, 2016 Lightbend, Inc. All rights reserved. [http://www.lightbend.com]
  */

import java.io.File

import scala.sys.process.Process
import scala.util.Try

object ProcessDSL {
  case class ProcessCmd(cmd: Seq[String], workingDir: File)

  implicit class ProcessExtension(val cmd: ProcessCmd) extends AnyVal {
    def runAndExitIfFailed(errorMsg: String): Unit = {
      val status = Try(Process(cmd.cmd, cmd.workingDir).!)
      if (status.isFailure) {
        println(
          s"""
             |$errorMsg
             |  Executed command: ${cmd.cmd.mkString(" ")}
             |  Working directory: ${cmd.workingDir}
           """.stripMargin)
        System.exit(-1)
      }
    }
  }

  implicit class StringToProcessExt(val command: String) extends AnyVal {
    def toProcessCmd(workingDir: File): ProcessCmd = {
      val cmdSeq = command.split("""\s+""").toVector
      ProcessCmd(cmdSeq, workingDir)
    }
  }
}
