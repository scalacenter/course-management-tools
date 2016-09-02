package com.lightbend.coursegentools

import java.io.File

/**
  * Copyright © 2014, 2015, 2016 Lightbend, Inc. All rights reserved. [http://www.lightbend.com]
  */

object LinearizeCmdLineOptParse {
  def parse(args: Array[String]): Option[LinearizeCmdOptions] = {

    val parser = new scopt.OptionParser[LinearizeCmdOptions]("linearize") {
      head("linearize", "1.0")

      arg[File]("masterRepo")
        .text("base folder holding master course repository")
        .action { case (masterRepo, c) =>
          if (! folderExists(masterRepo)) {
            println(s"base master repo folder (${masterRepo.getPath}) doesn't exist")
            System.exit(-1)
          }
          c.copy(masterRepo = masterRepo)
        }

      arg[File]("linearRepo")
        .text("base folder for linearized version repo")
        .action { case (linearRepo, config) =>
          if (! folderExists(linearRepo)) {
            println(s"base folder for linearized version repo (${linearRepo.getPath}) doesn't exist")
            System.exit(-1)
          }
          config.copy(linearRepo = linearRepo)}

    }

    parser.parse(args, LinearizeCmdOptions())
  }
}
