package cmt.admin.command.execution

/** Copyright 2022 - Eric Loots - eric.loots@gmail.com / Trevor Burton-McCreadie - trevor@thinkmorestupidless.com
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *
  * See the License for the specific language governing permissions and limitations under the License.
  */

import cmt.Helpers.*
import cmt.printErrorAndExit
import cmt.admin.command.AdminCommand.Linearize
import cmt.core.execution.Executable
import cmt.{ProcessDSL, toConsoleGreen}
import sbt.io.IO as sbtio
import sbt.io.syntax.*
import cmt.CmtError

given Executable[Linearize] with
  extension (cmd: Linearize)
    def execute(): Either[CmtError, String] = {
      import LinearizeHelpers.*

      for {
        _ <- exitIfGitIndexOrWorkspaceIsntClean(cmd.mainRepository.value)

        mainRepoName = cmd.mainRepository.value.getName
        tmpFolder = sbtio.createTemporaryDirectory
        cleanedMainRepo = tmpFolder / cmd.mainRepository.value.getName
        ExercisesMetadata(prefix, exercises, exerciseNumbers) <- getExerciseMetadata(cmd.mainRepository.value)(
          cmd.config)
        linearizedRootFolder = cmd.linearizeBaseDirectory.value / mainRepoName

        _ = println(s"Linearizing ${toConsoleGreen(cmd.mainRepository.value.getPath)} to ${toConsoleGreen(
            cmd.linearizeBaseDirectory.value.getPath)}")

        _ <- copyCleanViaGit(cmd.mainRepository.value, tmpFolder, mainRepoName)

        _ = checkpreExistingAndCreateArtifactRepo(
          cmd.linearizeBaseDirectory.value,
          linearizedRootFolder,
          cmd.forceDeleteDestinationDirectory.value)

        _ <- initializeGitRepo(linearizedRootFolder)

        _ <- commitExercises(cleanedMainRepo, exercises, linearizedRootFolder, cmd)

        _ = sbtio.delete(tmpFolder)
        successMessage <- Right(s"Successfully linearized ${cmd.mainRepository.value.getPath}")

      } yield successMessage
    }
end given

private object LinearizeHelpers:

  def commitExercises(
      cleanedMainRepo: File,
      exercises: Seq[String],
      linearizedRootFolder: File,
      cmd: Linearize): Either[CmtError, Unit] =

    val dotIgnoreFile = cleanedMainRepo / ".gitignore"
    if dotIgnoreFile.exists then sbtio.copyFile(dotIgnoreFile, linearizedRootFolder / ".gitignore")

    exercises match
      case exercise +: remainingExercises =>
        val from = cleanedMainRepo / cmd.config.mainRepoExerciseFolder / exercise
        val linearizedCodeFolder = linearizedRootFolder / cmd.config.linearizedRepoActiveExerciseFolder
        println(s"Copying from $from to $linearizedCodeFolder")
        sbtio.delete(linearizedCodeFolder)
        sbtio.createDirectory(linearizedCodeFolder)
        sbtio.copyDirectory(from, linearizedCodeFolder, preserveLastModified = true)
        val commitResult: Either[CmtError, Unit] = commitToGit(exercise, linearizedRootFolder)
        commitResult match
          case Right(_)    => commitExercises(cleanedMainRepo, remainingExercises, linearizedRootFolder, cmd)
          case l @ Left(_) => l
      case Nil => Right(())

end LinearizeHelpers
