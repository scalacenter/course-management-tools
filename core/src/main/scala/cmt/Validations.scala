package cmt

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

//import cmt.{FileValidations, IntValidations, StringValidations}
import sbt.io.syntax.File

object ValidationExtensions:

  extension (file: File)
    def existsAndIsADirectory: Either[String, Unit] =
      FileValidations.existsAndIsADirectory(file)
    def existsAndIsADirectoryInAGitRepository: Either[String, Unit] =
      FileValidations.existsAndIsADirectoryInAGitRepository(file)

  extension (int: Int)
    def isGreaterThanZero: Either[String, Unit] =
      IntValidations.isGreaterThanZero(int)
    def isNotNegative: Either[String, Unit] =
      IntValidations.isNotNegative(int)

object StringValidations:

  def isAnInteger(str: String): Either[String, Int] =
    try {
      Right(str.toInt)
    } catch {
      case _ => Left(s"'$str' is not a number")
    }

object IntValidations:

  def isGreaterThanZero(int: Int): Either[String, Unit] =
    Either.cond(int > 0, (), s"expected a number greater than 0 but received '$int'")

  def isNotNegative(int: Int): Either[String, Unit] =
    Either.cond(int >= 0, (), s"expected a number greater than 0 but received '$int'")

object FileValidations:

  def exists(file: File): Either[String, Unit] =
    Either.cond(file.exists(), (), s"${file.getPath} does not exist")

  def isADirectory(file: File): Either[String, Unit] =
    Either.cond(file.isDirectory, (), s"${file.getPath} is not a directory")

  def isAFile(file: File): Either[String, Unit] =
    Either.cond(file.isFile, (), s"${file.getPath} is not a file")

  def isInAGitRepository(file: File): Either[String, Unit] =
    Helpers.resolveMainRepoPath(file).map(_ => ()).left.map(_ => s"${file.getPath} is not in a git repository")

  def existsAndIsADirectory(file: File): Either[String, Unit] =
    for {
      _ <- exists(file)
      _ <- isADirectory(file)
    } yield ()

  def existsAndIsAFile(file: File): Either[String, Unit] =
    for {
      _ <- exists(file)
      _ <- isAFile(file)
    } yield ()

  def existsAndIsADirectoryInAGitRepository(file: File): Either[String, Unit] =
    for {
      _ <- existsAndIsADirectory(file)
      _ <- isInAGitRepository(file)
    } yield ()
