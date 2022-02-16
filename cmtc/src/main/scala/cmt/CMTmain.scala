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
    given CMTcConfig = new CMTcConfig(cmdLineArgs.studentifiedRepo.get) // Safe: at this point we know that studentifiedRepo exists

    cmdLineArgs match {
      
      case CmtcOptions(PullSolution, CmdPullSolutionOptions(Some(exerciseID)), Some(studentifiedRepo)) =>
        CMTStudent.pullSolution(studentifiedRepo, exerciseID)

      case CmtcOptions(ListExercises, _, Some(studentifiedRepo)) =>
        CMTStudent.listExercises(studentifiedRepo)
      
      case CmtcOptions(NextExercise, _, Some(studentifiedRepo)) =>
        CMTStudent.moveToNextExercise(studentifiedRepo)
      
      case CmtcOptions(PreviousExercise, _, Some(studentifiedRepo)) =>
        CMTStudent.moveToPreviousExercise(studentifiedRepo)

      case _ =>
    }
