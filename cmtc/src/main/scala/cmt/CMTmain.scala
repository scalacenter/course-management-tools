package cmt

object Main:
  def main(args: Array[String]): Unit =
    val cmdLineArgs: CmtcOptions =
      CmdLineParse
        .parse(args)
        .getOrElse {
          System.exit(1)
          ???
        }

    val config: CMTcConfig =
      new CMTcConfig(
        cmdLineArgs.studentifiedRepo.get
      ) // Safe: at this point we know that studentifiedRepo exists

    cmdLineArgs match {
      case CmtcOptions(
            PullSolution(Some(exerciseID)),
            Some(studentifiedRepo)
          ) =>
        CMTStudent.pullSolution(studentifiedRepo, exerciseID)(config)
      case CmtcOptions(ListExercises, Some(studentifiedRepo)) =>
        CMTStudent.listExercises(studentifiedRepo)(config)
      case CmtcOptions(NextExercise, Some(studentifiedRepo)) =>
        CMTStudent.moveToNextExercise(studentifiedRepo)(config)
      case CmtcOptions(PreviousExercise, Some(studentifiedRepo)) =>
        CMTStudent.moveToPreviousExercise(studentifiedRepo)(config)
      case CmtcOptions(SaveState, Some(studentifiedRepo)) =>
        CMTStudent.saveState(studentifiedRepo)(config)
      case CmtcOptions(ListSavedStates, Some(studentifiedRepo)) =>
        CMTStudent.listSavedStates(studentifiedRepo)(config)
      case CmtcOptions(
            RestoreState(Some(exerciseID)),
            Some(studentifiedRepo)
          ) =>
        CMTStudent.restoreState(studentifiedRepo, exerciseID)(config)
      case _ =>
    }
