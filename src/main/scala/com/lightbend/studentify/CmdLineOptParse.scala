package com.lightbend.studentify

import java.io.File

/**
  * Copyright © 2014, 2015, 2016 Lightbend, Inc. All rights reserved. [http://www.lightbend.com]
  */

object CmdLineOptParse {

  def parse(args: Array[String]): Option[CmdOptions] = {

    val parser = new scopt.OptionParser[CmdOptions]("studentify") {
      head("studentify", "1.0")

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

    }

    parser.parse(args, CmdOptions())
  }
}
