package cmt.client.cli

import cmt.client.cli.CliOptions
import cmt.client.cli.CliCommand.*
import cmt.core.cli.{CmdLineParseError, ScoptCliParser}
import sbt.io.syntax.File
import scopt.{OParser, OParserBuilder}
import cmt.ValidationExtensions.*
import cmt.client.Domain.{ExerciseId, StudentifiedRepo, TemplatePath}

object ClientCliParser {

  def parse(args: Array[String]): Either[CmdLineParseError, CliOptions] =
    ScoptCliParser.parse(parser, CliOptions.default())(args)

  val parser = {
    given builder: OParserBuilder[CliOptions] = OParser.builder[CliOptions]
    import builder.*

    OParser.sequence(
      programName("cmtc"),
      pullSolutionParser,
      listExercisesParser,
      gotoExerciseParser,
      gotoFirstExerciseParser,
      previousExerciseParser,
      nextExerciseParser,
      pullTemplateParser,
      saveStateParser,
      restoreStateParser,
      savedStateParser,
      versionParser,
      validateConfig)
  }

  private def previousExerciseParser(using builder: OParserBuilder[CliOptions]): OParser[Unit, CliOptions] =
    import builder.*
    cmd("previous-exercise")
      .text("Move to previous exercise and pull in tests for that exercise")
      .action { (_, c) => c.copy(command = PreviousExercise) }
      .children(
        arg[File]("<studentified directory>")
          .validate(_.existsAndIsADirectory)
          .action((studentifiedRepo, options) => options.copy(studentifiedRepo = StudentifiedRepo(studentifiedRepo))))

  private def nextExerciseParser(using builder: OParserBuilder[CliOptions]): OParser[Unit, CliOptions] =
    import builder.*
    cmd("next-exercise")
      .text("Move to next exercise and pull in tests for that exercise")
      .action { (_, c) => c.copy(command = NextExercise) }
      .children(
        arg[File]("<studentified directory>")
          .validate(_.existsAndIsADirectory)
          .action((studentifiedRepo, options) => options.copy(studentifiedRepo = StudentifiedRepo(studentifiedRepo))))

  private def listExercisesParser(using builder: OParserBuilder[CliOptions]): OParser[Unit, CliOptions] =
    import builder.*
    cmd("list-exercises")
      .text("List all exercises")
      .action { (_, c) => c.copy(command = ListExercises) }
      .children(
        arg[File]("<studentified directory>")
          .validate(_.existsAndIsADirectory)
          .action((studentifiedRepo, options) => options.copy(studentifiedRepo = StudentifiedRepo(studentifiedRepo))))

  private def pullTemplateParser(using builder: OParserBuilder[CliOptions]): OParser[Unit, CliOptions] =
    import builder.*
    cmd("pull-template")
      .text("Pull a template into the current exercise state")
      .action((_, options) => options.copy(command = PullTemplate))
      .children(
        arg[String]("<template path>").action((templatePath, options) =>
          options.copy(templatePath = TemplatePath(templatePath))),
        arg[File]("<studentified directory>")
          .validate(_.existsAndIsADirectory)
          .action((studentifiedRepo, options) => options.copy(studentifiedRepo = StudentifiedRepo(studentifiedRepo))))

  private def gotoExerciseParser(using builder: OParserBuilder[CliOptions]): OParser[Unit, CliOptions] =
    import builder.*
    cmd("goto-exercise")
      .text("Go to a given exercise and fetch corresponding tests")
      .action((_, options) => options.copy(command = GotoExercise))
      .children(
        arg[String]("<exercise ID>").action((exerciseId, options) => options.copy(exerciseId = ExerciseId(exerciseId))),
        arg[File]("<studentified directory>")
          .validate(_.existsAndIsADirectory)
          .action((studentifiedRepo, options) => options.copy(studentifiedRepo = StudentifiedRepo(studentifiedRepo))))

  private def gotoFirstExerciseParser(using builder: OParserBuilder[CliOptions]): OParser[Unit, CliOptions] =
    import builder.*
    cmd("goto-first-exercise")
      .text("Go to the first exercise and fetch corresponding tests")
      .action { (_, c) => c.copy(command = GotoFirstExercise) }
      .children(
        arg[File]("<studentified directory>")
          .validate(_.existsAndIsADirectory)
          .action((studentifiedRepo, options) => options.copy(studentifiedRepo = StudentifiedRepo(studentifiedRepo))))

  private def pullSolutionParser(using builder: OParserBuilder[CliOptions]): OParser[Unit, CliOptions] =
    import builder.*
    cmd("pull-solution")
      .text("Pull solution for a given exercise")
      .action { (_, c) => c.copy(command = PullSolution) }
      .children(
        arg[File]("<studentified directory>")
          .validate(_.existsAndIsADirectory)
          .action((studentifiedRepo, options) => options.copy(studentifiedRepo = StudentifiedRepo(studentifiedRepo))))

  private def saveStateParser(using builder: OParserBuilder[CliOptions]): OParser[Unit, CliOptions] =
    import builder.*
    cmd("save-state")
      .text("Save state of current exercise")
      .action { (_, c) => c.copy(command = SaveState) }
      .children(
        arg[File]("<studentified directory>")
          .validate(_.existsAndIsADirectory)
          .action((studentifiedRepo, options) => options.copy(studentifiedRepo = StudentifiedRepo(studentifiedRepo))))

  private def savedStateParser(using builder: OParserBuilder[CliOptions]): OParser[Unit, CliOptions] =
    import builder.*
    cmd("list-saved-states")
      .text("List all saved exercise states")
      .action { (_, c) => c.copy(command = ListSavedStates) }
      .children(
        arg[File]("<studentified directory>")
          .validate(_.existsAndIsADirectory)
          .action((studentifiedRepo, options) => options.copy(studentifiedRepo = StudentifiedRepo(studentifiedRepo))))

  private def restoreStateParser(using builder: OParserBuilder[CliOptions]): OParser[Unit, CliOptions] =
    import builder.*
    cmd("restore-state")
      .text("Restore state of a previously save exercise state")
      .action((_, options) => options.copy(command = RestoreState))
      .children(
        arg[String]("<exercise ID>").action((exerciseId, options) => options.copy(exerciseId = ExerciseId(exerciseId))),
        arg[File]("<studentified directory>")
          .validate(_.existsAndIsADirectory)
          .action((studentifiedRepo, options) => options.copy(studentifiedRepo = StudentifiedRepo(studentifiedRepo))))

  private def versionParser(using builder: OParserBuilder[CliOptions]): OParser[Unit, CliOptions] =
    import builder.*
    cmd("version").text("Print version information").action { (_, c) =>
      c.copy(command = Version)
    }

  private def validateConfig(using builder: OParserBuilder[CliOptions]): OParser[Unit, CliOptions] =
    import builder.*
    checkConfig(config =>
      config.command match
        case NoCommand         => failure("missing command")
        case PullSolution      => success
        case ListExercises     => success
        case NextExercise      => success
        case PreviousExercise  => success
        case SaveState         => success
        case ListSavedStates   => success
        case RestoreState      => success
        case GotoExercise      => success
        case GotoFirstExercise => success
        case PullTemplate      => success
        case Version           => success
    )
}
