package cmt

import sbt.io.syntax.*
import sbt.io.IO as sbtio

object TestHelpers:
  final case class ExercisePrefixesAndExerciseNames(prefixes: Set[String], exercises: Vector[String])

  def getExercisePrefixAndExercises(mainRepo: File)(config: CMTaConfig): ExercisePrefixesAndExerciseNames =
    val PrefixSpec = raw"(.*)_\d{3}_\w+$$".r
    val matchedNames =
      sbtio.listFiles(isExerciseFolder())(mainRepo / config.mainRepoExerciseFolder).map(_.getName).to(List)
    val prefixes = matchedNames.map { case PrefixSpec(n) => n }.to(Set)
    val exercises = sbtio
      .listFiles(isExerciseFolder())(mainRepo / config.mainRepoExerciseFolder)
      .map(_.getName)
      .to(Vector)
      .sorted match
      case Vector() =>
        printErrorAndExit("No exercises found. Check your configuration"); ???
      case exercises => exercises
    ExercisePrefixesAndExerciseNames(prefixes, exercises)
  end getExercisePrefixAndExercises
