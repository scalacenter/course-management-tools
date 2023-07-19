package com.lunatech.cmt.admin

import com.lunatech.cmt.CmtError
import java.io.File
import com.lunatech.cmt.toExecuteCommandErrorMessage

def validateTargetFolder(mainRepository: File, target: File): Either[CmtError, Unit] =
  val canonicalStudentifyBaseDirectory = target.getCanonicalPath
  val canonicalMainRepository = mainRepository.getCanonicalPath
  val mainRepositoryEqualsStudentifyBaseDirectory = canonicalStudentifyBaseDirectory == canonicalMainRepository
  val StudentifyBaseDirectoryIsSubfolderOfmainRepository =
    canonicalStudentifyBaseDirectory.startsWith(canonicalMainRepository)
  (mainRepositoryEqualsStudentifyBaseDirectory, StudentifyBaseDirectoryIsSubfolderOfmainRepository) match {
    case (true, _) =>
      Left("destination folder cannot be the same as the main repository root folder".toExecuteCommandErrorMessage)
    case (_, true) =>
      Left("destination folder cannot be a subfolder of the main repository".toExecuteCommandErrorMessage)
    case _ =>
      Right(())
  }
