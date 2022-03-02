package cmt

import scopt.OEffect.ReportError

object Main:

  def main(args: Array[String]): Unit =
    CmdLineParse.parse(args) match {
      case Right(options) =>
        selectAndExecuteCommand(options)

      case Left(CmdLineParseError(x)) =>
        printError(x.collect { case ReportError(msg) => msg }.mkString("\n"))
    }

  private def selectAndExecuteCommand(options: CmtaOptions): Unit = {
    given CMTaConfig = CMTaConfig(options.mainRepo, options.configFile)

    val config: CMTaConfig = CMTaConfig(options.mainRepo, options.configFile)

    options match {
      case CmtaOptions(
            mainRepo,
            Studentify(Some(stuBase), forceDeleteExistingDestinationFolder: Boolean, initializeAsGitRepo: Boolean),
            _) =>
        CMTStudentify.studentify(mainRepo, stuBase, forceDeleteExistingDestinationFolder, initializeAsGitRepo)(config)

      case CmtaOptions(mainRepo, RenumberExercises(renumFromOpt, renumTo, renumBy), configFile) =>
        val message = renumFromOpt match {
          case Some(renumFrom) =>
            s"Renumbered exercises in ${mainRepo.getPath} from ${renumFrom} to ${renumTo} by ${renumBy}"
          case None => s"Renumbered exercises in ${mainRepo.getPath} to ${renumTo} by ${renumBy}"
        }
        CMTAdmin.renumberExercises(mainRepo, renumFromOpt, renumTo, renumBy)(config).printResultOrError(message)

      case CmtaOptions(mainRepo, DuplicateInsertBefore(exerciseNumber), configFile) =>
        CMTAdmin
          .duplicateInsertBefore(mainRepo, exerciseNumber)(config)
          .printResultOrError(s"Duplicated and inserted exercise $exerciseNumber")

      case CmtaOptions(mainRepo, Linearize(Some(linBase), forceDeleteExistingDestinationFolder: Boolean), _) =>
        CMTLinearize.linearize(mainRepo, linBase, forceDeleteExistingDestinationFolder)(config)

      case CmtaOptions(mainRepo, DeLinearize(Some(linBase)), _) =>
        CMTDeLinearize.delinearize(mainRepo, linBase)(config)

      case _ =>
    }
  }

  extension (result: Either[String, Unit])
    def printResultOrError(message: String): Unit =
      result match
        case Left(errorMessage) =>
          System.err.println(toConsoleRed(s"Error: $errorMessage"))
          System.exit(1)
        case Right(_) =>
          printMessage(message)
