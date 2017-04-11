package com.lightbend.coursegentools

import java.io.File

object MasterAdmCmdLineOptParse {
  def parse(args: Array[String]): Option[MasterAdmCmdOption] = {

    val parser = new scopt.OptionParser[MasterAdmCmdOption]("masteradm") {
      head("masteradm", "1.0")

      arg[File]("masterRepo")
        .text("base folder holding master course repository")
        .action { case (masterRepo, c) =>
          if (! folderExists(masterRepo)) {
            println(s"base master repo folder (${masterRepo.getPath}) doesn't exist")
            System.exit(-1)
          }
          c.copy(masterRepo = masterRepo)
        }

      opt[Unit]("multi-jvm")
        .text("generate multi-jvm build file")
        .abbr("mjvm")
        .action { case (_, c) =>
          c.copy(multiJVM = true)
        }

      opt[Unit]("renum")
        .text("renumber exercises")
        .abbr("r")
        .action { case (_, c) =>
          c.copy(renumber = true)
        }

      opt[Int]("renum-base")
        .text("index of first exercise")
        .abbr("rbase")
        .action { case (rbase, c) =>
          if (rbase < 0) {
            println(s"The base index for exercises should be >= 0")
            System.exit(-1)
          }
          c.copy(renumberOffset = rbase)
        }

      opt[Int]("renum-step")
        .text("step between exercises")
        .abbr("rstep")
        .action { case (rstep, c) =>
          if (rstep <= 0) {
            println(s"The step between exercises should be > 0")
            System.exit(-1)
          }
          c.copy(renumberDelta = rstep)
        }
    }

    parser.parse(args, MasterAdmCmdOption())
  }
}
