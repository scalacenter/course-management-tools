package com.lightbend.coursegentools

import java.io.File

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

object LinearizeCmdLineOptParse {
  def parse(args: Array[String]): Option[LinearizeCmdOptions] = {

    implicit val eofe: ExitOnFirstError = ExitOnFirstError(true)

    val parser = new scopt.OptionParser[LinearizeCmdOptions]("linearize") {
      head("linearize", "3.0")

      arg[File]("masterRepo")
        .text("base folder holding master course repository")
        .action { case (masterRepo, c) =>
          if (! folderExists(masterRepo))
            printError(s"Base master repo folder (${masterRepo.getPath}) doesn't exist")
          c.copy(masterRepo = masterRepo)
        }

      arg[File]("linearRepo")
        .text("base folder for linearized version repo")
        .action { case (linearRepo, config) =>
          if (! folderExists(linearRepo))
            printError(s"Base folder for linearized version repo (${linearRepo.getPath}) doesn't exist")
          config.copy(linearRepo = linearRepo)}

      opt[Unit]("multi-jvm")
        .text("generate multi-jvm build file")
        .abbr("mjvm")
        .action { case (_, c) =>
          c.copy(multiJVM = true)
        }

      opt[Unit]("force-delete")
        .text("Force-delete a pre-existing destination folder")
        .abbr("f")
        .action { case (_, c) =>
          c.copy(forceDeleteExistingDestinationFolder = true)
        }

      opt[String]("config-file")
        .text("configuration file")
        .abbr("cfg")
        .action {
          case (cfgFile, c) =>
            c.copy(configurationFile = Some(cfgFile))
        }

      opt[Unit]("dotty")
        .text("studentified repository is a Dotty project")
        .abbr("dot")
        .action { case (_, c) =>
          c.copy(isADottyProject = true)
        }
      opt[Unit]("no-auto-reload-sbt")
        .text("no automatic reload on build definition change")
        .abbr("nar")
        .action { case (_, c) =>
          c.copy(autoReloadOnBuildDefChange = false)
        }
    }

    parser.parse(args, LinearizeCmdOptions())
  }
}
