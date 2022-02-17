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

    given ExitOnFirstError = ExitOnFirstError(true)
    val config: CMTcConfig = new CMTcConfig(cmdLineArgs.studentifiedRepo.get) // Safe: at this point we know that studentifiedRepo exists

    cmdLineArgs match {
      
      case CmtcOptions(PullSolution, CmdPullSolutionOptions(Some(exerciseID)), Some(studentifiedRepo)) =>
        CMTStudent.pullSolution(studentifiedRepo, exerciseID)(config)

      case CmtcOptions(ListExercises, _, Some(studentifiedRepo)) =>
        CMTStudent.listExercises(studentifiedRepo)(config)
      
      case CmtcOptions(NextExercise, _, Some(studentifiedRepo)) =>
        CMTStudent.moveToNextExercise(studentifiedRepo)(config)
      
      case CmtcOptions(PreviousExercise, _, Some(studentifiedRepo)) =>
        CMTStudent.moveToPreviousExercise(studentifiedRepo)(config)

      case _ =>
    }
