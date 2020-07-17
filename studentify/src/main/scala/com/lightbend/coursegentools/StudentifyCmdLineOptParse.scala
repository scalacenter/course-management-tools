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

object StudentifyCmdLineOptParse {

  def parse(args: Array[String]): Option[StudentifyCmdOptions] = {

    implicit val eofe: ExitOnFirstError = ExitOnFirstError(true)

    val parser = new scopt.OptionParser[StudentifyCmdOptions]("studentify") {
      head("Course Management Tools:", "studentify", com.github.eloots.cmt.BuildInfo.version)

      arg[File]("mainRepo")
        .text("base folder holding main course repository")
        .action { case (mainRepo, c) =>
          if (! folderExists(mainRepo))
            printError(s"Base main repo folder (${mainRepo.getPath}) doesn't exist")
          c.copy(mainRepo = mainRepo)
        }

      arg[File]("out")
        .text("base folder for student repo")
        .action { case (out, config) =>
          if (! folderExists(out))
            printError(s"Base folder (${out.getPath}) doesn't exist")
          config.copy(out = out)}

      opt[Unit]("multi-jvm")
        .text("generate multi-jvm build file")
        .abbr("mjvm")
        .action { case (_, c) =>
                c.copy(multiJVM = true)
            }

      opt[String]("first-exercise")
          .text("name of first exercise to output")
            .abbr("fe")
              .action { case (firstExercise, c) =>
                  c.copy(first = Some(firstExercise))
              }

      opt[String]("last-exercise")
        .text("name of last exercise to output")
        .abbr("le")
        .action { case (lastExercise, c) =>
          c.copy(last = Some(lastExercise))
        }

      opt[String]("selected-first-exercise")
        .text("name of initial exercise on start")
        .abbr("sfe")
        .action {
          case (selectedFirstEx, c) =>
            c.copy(selectedFirst = Some(selectedFirstEx))
        }

      opt[String]("config-file")
        .text("configuration file")
        .abbr("cfg")
        .action {
          case (cfgFile, c) =>
            c.copy(configurationFile = Some(cfgFile))
        }

      opt[Unit]("git")
        .text("initialise studentified repository as a git repository")
        .abbr("g")
        .action { case (_, c) =>
          c.copy(initAsGitRepo = true)
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
      
      help("help").text("Prints the usage text")

      version("version").abbr("v").text("Prints the version info")
    }

    parser.parse(args, StudentifyCmdOptions())
  }
}
