package cmt.admin.command.execution

import cmt.Helpers.*
import cmt.admin.command.AdminCommand.Linearize
import cmt.core.execution.Executable
import cmt.{ProcessDSL, toConsoleGreen}
import sbt.io.IO as sbtio
import sbt.io.syntax.*

given Executable[Linearize] with
  extension (cmd: Linearize)
    def execute(): Either[String, String] = {
      import LinearizeHelpers.*

      for {
        _ <- exitIfGitIndexOrWorkspaceIsntClean(cmd.mainRepository.value)
        _ = println(s"Linearizing ${toConsoleGreen(cmd.mainRepository.value.getPath)} to ${toConsoleGreen(
            cmd.linearizeBaseDirectory.value.getPath)}")

        mainRepoName = cmd.mainRepository.value.getName
        tmpFolder = sbtio.createTemporaryDirectory
        cleanedMainRepo = tmpFolder / cmd.mainRepository.value.getName

        _ <- copyCleanViaGit(cmd.mainRepository.value, tmpFolder, mainRepoName)

        ExercisesMetadata(prefix, exercises, exerciseNumbers) <- getExerciseMetadata(cmd.mainRepository.value)(
          cmd.config)

        linearizedRootFolder = cmd.linearizeBaseDirectory.value / mainRepoName

        _ = if linearizedRootFolder.exists && cmd.forceDeleteDestinationDirectory.value then
          sbtio.delete(linearizedRootFolder)
        _ = sbtio.createDirectory(linearizedRootFolder)

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
      cmd: Linearize): Either[String, Unit] =
    exercises match
      case exercise +: remainingExercises =>
        val from = cleanedMainRepo / cmd.config.mainRepoExerciseFolder / exercise
        val linearizedCodeFolder = linearizedRootFolder / cmd.config.linearizedRepoActiveExerciseFolder
        println(s"Copying from $from to $linearizedCodeFolder")
        sbtio.delete(linearizedCodeFolder)
        sbtio.createDirectory(linearizedCodeFolder)
        sbtio.copyDirectory(from, linearizedCodeFolder, preserveLastModified = true)
        val commitResult: Either[String, Unit] = commitToGit(exercise, linearizedRootFolder)
        commitResult match
          case Right(_) => commitExercises(cleanedMainRepo, remainingExercises, linearizedRootFolder, cmd)
          case left     => left
      case Nil => Right(())
end LinearizeHelpers
