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

object DeLinearize {
  def main(args: Array[String]): Unit = {

    import Helpers._

    val cmdOptions = DeLinearizeCmdLineOptParse.parse(args)
    if (cmdOptions.isEmpty) System.exit(-1)
    val DeLinearizeCmdOptions(mainRepoPath, linearizedRepo, optConfigurationFile) = cmdOptions.get
    val mainRepo = resolveMainRepoPath(mainRepoPath)

    implicit val config: MainSettings = new MainSettings(mainRepo, optConfigurationFile)
    implicit val eofe: ExitOnFirstError = ExitOnFirstError(true)

    val exercisesInMain = getExerciseNames(mainRepo)
    val exercisesAndSHAs = getExercisesAndSHAs(linearizedRepo)
    checkReposMatch(exercisesInMain, exercisesAndSHAs)
    putBackToMain(mainRepo, linearizedRepo, exercisesAndSHAs)
  }
}
