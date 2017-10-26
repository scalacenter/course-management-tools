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

object DeLinearizeCmdLineOptParse {
  def parse(args: Array[String]): Option[DeLinearizeCmdOptions] = {

    val parser = new scopt.OptionParser[DeLinearizeCmdOptions]("delinearize") {
      head("delinearize", "1.0")

      arg[File]("linearRepo")
        .text("base folder for linearized version repo")
        .action { case (linearRepo, config) =>
          if (!folderExists(linearRepo)) {
            println(toConsoleRed(s"Base folder for linearized version repo (${linearRepo.getPath}) doesn't exist"))
            System.exit(-1)
          }
          config.copy(linearRepo = linearRepo)
        }

      arg[File]("masterRepo")
        .text("base folder holding master course repository")
        .action { case (masterRepo, c) =>
          if (!folderExists(masterRepo)) {
            println(toConsoleRed(s"Base master repo folder (${masterRepo.getPath}) doesn't exist"))
            System.exit(-1)
          }
          c.copy(masterRepo = masterRepo)
        }
    }

    parser.parse(args, DeLinearizeCmdOptions())
  }
}
