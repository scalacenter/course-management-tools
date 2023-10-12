package coursemgmt.admin

import coursemgmt.CmtError
import java.io.File
import coursemgmt.toExecuteCommandErrorMessage

def validateDestinationFolder(mainRepository: File, destination: File): Either[CmtError, Unit] =
  val canonicalTarget = destination.getCanonicalPath
  val canonicalMainRepository = mainRepository.getCanonicalPath
  val destinationEqualsMainRepository = canonicalTarget == canonicalMainRepository
  val destinationIsSubfolderOfmainRepository =
    canonicalTarget.startsWith(canonicalMainRepository)
  (destinationEqualsMainRepository, destinationIsSubfolderOfmainRepository) match {
    case (true, _) =>
      Left("destination folder cannot be the same as the main repository root folder".toExecuteCommandErrorMessage)
    case (_, true) =>
      Left("destination folder cannot be a subfolder of the main repository".toExecuteCommandErrorMessage)
    case _ =>
      Right(())
  }
