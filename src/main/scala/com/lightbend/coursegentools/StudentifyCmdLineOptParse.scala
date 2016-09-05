package com.lightbend.coursegentools

import java.io.File

/**
  * Copyright © 2014, 2015, 2016 Lightbend, Inc. All rights reserved. [http://www.lightbend.com]
  */

object StudentifyCmdLineOptParse {

  def parse(args: Array[String]): Option[StudentifyCmdOptions] = {

    val parser = new scopt.OptionParser[StudentifyCmdOptions]("studentify") {
      head("studentify", "1.1")

      arg[File]("masterRepo")
        .text("base folder holding master course repository")
        .action { case (masterRepo, c) =>
          if (! folderExists(masterRepo)) {
            println(s"base master repo folder (${masterRepo.getPath}) doesn't exist")
            System.exit(-1)
          }
          c.copy(masterRepo = masterRepo)
        }

      arg[File]("out")
        .text("base folder for student repo")
        .action { case (out, config) =>
          if (! folderExists(out)) {
            println(s"base folder (${out.getPath}) doesn't exist")
            System.exit(-1)
          }
          config.copy(out = out)}

      opt[Unit]("multi-jvm")
        .text("generate multi-jvm build file")
        .abbr("mjvm")
        .action { case (_, c) =>
                c.copy(multiJVM = true)
            }

    }

    parser.parse(args, StudentifyCmdOptions())
  }
}
