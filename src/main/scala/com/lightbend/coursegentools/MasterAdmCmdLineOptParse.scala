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

object MasterAdmCmdLineOptParse {
  def parse(args: Array[String]): Option[MasterAdmCmdOptions] = {

    implicit val eofe: ExitOnFirstError = ExitOnFirstError(true)

    val parser = new scopt.OptionParser[MasterAdmCmdOptions]("masteradm") {
      head("masteradm", "3.0")

      arg[File]("masterRepo")
        .text("base folder holding master course repository")
        .action { case (masterRepo, c) =>
          if (! folderExists(masterRepo))
            printError(s"Base master repo folder (${masterRepo.getPath}) doesn't exist")
          c.copy(masterRepo = masterRepo)
        }

      opt[Unit]("multi-jvm")
        .text("generate multi-jvm build file")
        .abbr("mjvm")
        .action { case (_, c) =>
          c.copy(multiJVM = true)
        }

      opt[Unit]("build-file-regen")
        .text("regenerate project root build file")
        .abbr("b")
        .action { case (_, c) =>
            c.copy(regenBuildFile = true)
        }

      opt[Int]("delete")
        .text("")
        .abbr("d")
        .action { case (exNr, c) =>
          c.copy(deleteExerciseNr = Some(exNr))
        }

      opt[Unit]("renumber")
        .text("renumber exercises")
        .abbr("r")
        .action { case (_, c) =>
            c.copy(renumberExercises = true)
        }

      opt[Int]("renumber-offset")
        .text("renumber exercises - offset")
        .abbr("ro")
        .action { case (offset, c) =>
            c.copy(renumberExercisesBase = offset)
        }

      opt[Int]("renumber-step")
        .text("renumber exercises - step")
        .abbr("rs")
        .action { case (step, c) =>
          c.copy(renumberExercisesStep = step)
        }

      opt[Int]("duplicate-insert-before")
        .text("")
        .abbr("dib")
        .action { case (exNr, c) =>
          c.copy(duplicateExerciseInsertBeforeNr = Some(exNr))
        }

      opt[String]("config-file")
        .text("configuration file")
        .abbr("cfg")
        .action {
          case (cfgFile, c) =>
            c.copy(configurationFile = Some(cfgFile))
        }

      opt[Unit]("check-master-repo")
        .text("verify soundness of master repository")
        .abbr("c")
        .action {
          case (_, c) =>
            c.copy(checkMaster = true)
        }

      opt[Unit]("add-master-commands")
        .text("add command files to master repository")
        .abbr("amc")
          .action {
            case (_, c) =>
              c.copy(addMasterCommands = true)
          }

      opt[File]("generate-tests-script")
        .text("generate a script that tests master repo, studentified repo functionality and linearize/delinearize")
        .abbr("t")
        .action {
          case (testFile, c) =>
            c.copy(testFile = Some(testFile))
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

    parser.parse(args, MasterAdmCmdOptions())
  }
}
