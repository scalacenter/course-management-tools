package cmt.support

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
import cmt.admin.Domain.MainRepository
import cmt.admin.cli.SharedOptions
import cmt.admin.command
import cmt.client.{Configuration, CoursesDirectory, CurrentCourse}
import cmt.client.Configuration.CmtHome
import cmt.client.Domain.{ExerciseId, ForceMoveToExercise, StudentifiedRepo, TemplatePath}
import cmt.client.command.{
  GotoExercise,
  GotoFirstExercise,
  NextExercise,
  PreviousExercise,
  PullSolution,
  PullTemplate,
  RestoreState,
  SaveState
}
import cmt.{CMTcConfig, CmtError, Helpers}
import sbt.io.IO as sbtio
import sbt.io.syntax.*

import java.nio.charset.StandardCharsets
import java.util.UUID
import scala.annotation.targetName

type FilePath = String
type ExerciseName = String
type SourceFileStruct = List[FilePath]
final case class SourcesStruct(test: SourceFileStruct, readme: SourceFileStruct, main: SourceFileStruct)
opaque type ExerciseMetadata = Map[ExerciseName, SourcesStruct]

extension (sfs: SourceFileStruct)
  def toSourceFiles: SourceFiles =
    sfs.map(n => (n, UUID.randomUUID())).to(Map).map { case (k, v) => (adaptToOSSeparatorChar(k), v) }

object ExerciseMetadata:
  def apply(): ExerciseMetadata = Map.empty

  extension (es: ExerciseMetadata)
    def addExercise(exercise: (ExerciseName, SourcesStruct)): ExerciseMetadata = es + exercise

    def toExercises: Exercises = es.view
      .mapValues { case SourcesStruct(test, readme, main) =>
        ExercisesStruct(test = test.toSourceFiles, readme = readme.toSourceFiles, main = main.toSourceFiles)
      }
      .to(Map)

end ExerciseMetadata

type CheckSum = UUID
opaque type SourceFiles = Map[FilePath, CheckSum]
final case class ExercisesStruct(test: SourceFiles, readme: SourceFiles, main: SourceFiles)
opaque type Exercises = Map[ExerciseName, ExercisesStruct]

object Exercises:
  extension (exercises: Exercises)
    def createRepo(
        baseFolder: File,
        codeFolder: String,
        additionalFiles: Option[SourceFiles] = None): Either[String, Unit] =
      for {
        exercise <- exercises.keys
        ExercisesStruct(test, readme, main) = exercises(exercise)

      } {
        test.createFiles(baseFolder, exercise, codeFolder)
        readme.createFiles(baseFolder, exercise, codeFolder)
        main.createFiles(baseFolder, exercise, codeFolder)
      }
      initializeGitRepo(baseFolder)
      setGitConfig(baseFolder)
      commitToGit("Initial commit", baseFolder)
      Right(())

    def getMainCode(exerciseName: ExerciseName): SourceFiles =
      exercises(exerciseName).main

    def getMainFile(exerciseName: ExerciseName, filePath: String): Tuple2[FilePath, CheckSum] =
      adaptToOSSeparatorChar(filePath) -> getMainCode(exerciseName)(adaptToOSSeparatorChar(filePath))

    def getTestCode(exerciseName: ExerciseName): SourceFiles =
      exercises(exerciseName).test

    def getReadmeCode(exerciseName: ExerciseName): SourceFiles =
      exercises(exerciseName).readme

    def getAllCode(exerciseName: ExerciseName): SourceFiles =
      getMainCode(exerciseName) ++ getTestCode(exerciseName) ++ getReadmeCode(exerciseName)

end Exercises

object SourceFiles:
  def apply(sourceFiles: Map[FilePath, UUID]): SourceFiles = sourceFiles

  extension (sf: SourceFiles)
    def createFiles(baseFolder: File, exercise: String, codeFolder: String): Unit =
      sf.foreach { case (filePath, checksum) =>
        sbtio.touch(baseFolder / codeFolder / exercise / filePath)
        dumpStringToFile(checksum.toString, baseFolder / codeFolder / exercise / filePath)
      }

    def moveFile(baseFolder: File, fromPath: String, toPath: String): SourceFiles =
      for {
        (path, checksum) <- sf
        tp =
          if (adaptToOSSeparatorChar(fromPath) == path)
            val localToPath = adaptToOSSeparatorChar(toPath)
            sbtio.move(baseFolder / path, baseFolder / localToPath)
            localToPath
          else path
      } yield (tp, checksum)

    @targetName("mergeWith")
    def ++(other: SourceFiles): SourceFiles =
      sf ++ other

    @targetName("mergeFile")
    def +(other: Tuple2[FilePath, CheckSum]): SourceFiles =
      sf + other

    @targetName("minus")
    def --(other: SourceFiles): SourceFiles =
      sf -- other.keys

    def doesNotContain(otherSourceFiles: SourceFiles): Unit =
      for {
        (filePath, _) <- otherSourceFiles
      } assert(!sf.contains(filePath), s"Actual sourceFiles shouldn't contain ${filePath}")
end SourceFiles

def createMainRepo(tmpDir: File, repoName: String, exercises: Exercises, testConfig: String): File =
  import Exercises.*
  val mainRepo = tmpDir / repoName
  sbtio.touch(mainRepo / "course-management.conf")
  Helpers.dumpStringToFile(testConfig, mainRepo / "course-management.conf")
  exercises.createRepo(mainRepo, "code")
  mainRepo

def studentifyMainRepo(tmpDir: File, repoName: String, mainRepo: File): File =
  import cmt.admin.Domain.{ForceDeleteDestinationDirectory, InitializeGitRepo, StudentifyBaseDirectory}
  import cmt.admin.command.Studentify

  val studentifyBase = tmpDir / "stu"
  sbtio.createDirectory(studentifyBase)
  val cmd = Studentify.Options(
    studentifyBaseDirectory = StudentifyBaseDirectory(studentifyBase),
    forceDelete = ForceDeleteDestinationDirectory(false),
    initGit = InitializeGitRepo(false),
    shared = SharedOptions(mainRepository = MainRepository(mainRepo)))
  cmd.execute()
  studentifyBase / repoName

def extractCodeFromRepo(codeFolder: File): SourceFiles =
  val files = Helpers.fileList(codeFolder)
  val filesAndChecksums = for {
    file <- files
    Some(fileName) = file.relativeTo(codeFolder): @unchecked
    checksum = java.util.UUID.fromString(sbtio.readLines(file, StandardCharsets.UTF_8).head)
  } yield (fileName.getPath, checksum)
  SourceFiles(filesAndChecksums.to(Map))

def gotoNextExercise(config: CMTcConfig, studentifiedRepo: File): Either[CmtError, String] =
  NextExercise.Options(force = ForceMoveToExercise(false)).execute(createConfiguration(studentifiedRepo))

def gotoNextExerciseForced(config: CMTcConfig, studentifiedRepo: File): Either[CmtError, String] =
  NextExercise.Options(force = ForceMoveToExercise(true)).execute(createConfiguration(studentifiedRepo))

def gotoPreviousExercise(config: CMTcConfig, studentifiedRepo: File): Either[CmtError, String] =
  PreviousExercise.Options(force = ForceMoveToExercise(false)).execute(createConfiguration(studentifiedRepo))

def gotoPreviousExerciseForced(config: CMTcConfig, studentifiedRepo: File): Either[CmtError, String] =
  PreviousExercise.Options(force = ForceMoveToExercise(true)).execute(createConfiguration(studentifiedRepo))

def pullSolution(config: CMTcConfig, studentifiedRepo: File): Either[CmtError, String] =
  PullSolution.Options().execute(createConfiguration(studentifiedRepo))

def gotoExercise(config: CMTcConfig, studentifiedRepo: File, exercise: String): Either[CmtError, String] =
  GotoExercise
    .Options(exercise = Some(ExerciseId(exercise)), force = ForceMoveToExercise(false))
    .execute(createConfiguration(studentifiedRepo))

def gotoFirstExercise(config: CMTcConfig, studentifiedRepo: File): Either[CmtError, String] =
  GotoFirstExercise.Options(force = ForceMoveToExercise(false)).execute(createConfiguration(studentifiedRepo))

def saveState(config: CMTcConfig, studentifiedRepo: File): Either[CmtError, String] =
  SaveState.Options().execute(createConfiguration(studentifiedRepo))

def restoreState(config: CMTcConfig, studentifiedRepo: File, exercise: String): Either[CmtError, String] =
  RestoreState.Options(exercise = Some(ExerciseId(exercise))).execute(createConfiguration(studentifiedRepo))

def pullTemplate(config: CMTcConfig, studentifiedRepo: File, templatePath: String): Either[CmtError, String] =
  PullTemplate
    .Options(template = Some(TemplatePath(Helpers.adaptToOSSeparatorChar(templatePath))))
    .execute(createConfiguration(studentifiedRepo))

def addFileToStudentifiedRepo(studentifiedRepo: File, filePath: String): SourceFiles =
  val fileContent = UUID.randomUUID()
  sbtio.touch(studentifiedRepo / filePath)
  dumpStringToFile(fileContent.toString, studentifiedRepo / filePath)
  SourceFiles(Map(filePath -> fileContent))

private def createConfiguration(studentifiedRepoDirectory: File): Configuration =
  Configuration(
    CmtHome(file(".")),
    CoursesDirectory(file(".")),
    CurrentCourse(StudentifiedRepo(studentifiedRepoDirectory)))
