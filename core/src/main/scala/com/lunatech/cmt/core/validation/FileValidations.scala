package com.lunatech.cmt.core.validation

import caseapp.core.Error
import cats.data.{NonEmptyList, ValidatedNel}
import cats.syntax.either.*
import com.lunatech.cmt.Helpers
import sbt.io.syntax.File

object FileValidations:

  type ValidationResult[T] = ValidatedNel[Error, T]

  extension (file: File)
    def validateExists: ValidatedNel[Error, File] =
      Either.cond(file.exists(), file, Error.Other(s"${file.getAbsolutePath} does not exist")).toValidatedNel

    def validateIsDirectory: ValidatedNel[Error, File] =
      Either.cond(file.isDirectory, file, Error.Other(s"${file.getAbsolutePath} is not a directory")).toValidatedNel

    def validateIsFile: ValidatedNel[Error, File] =
      Either.cond(file.isFile, file, Error.Other(s"${file.getAbsolutePath} is not a file")).toValidatedNel

    def validateIsInAGitRepository: ValidatedNel[Error, File] =
      Helpers
        .resolveMainRepoPath(file)
        .leftMap(_ => Error.Other(s"${file.getAbsolutePath} is not in a git repository"))
        .toValidatedNel

  extension (nel: NonEmptyList[Error])
    def flatten: Error =
      nel.tail.foldLeft(nel.head)((acc, next) => acc.append(next))
