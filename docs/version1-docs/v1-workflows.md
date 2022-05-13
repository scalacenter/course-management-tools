---
id: v1-workflows
title: Workflows (Version 1)
sidebar_label: Workflows
---


## Studentifying a CMT main repository

> NOTE: You're browsing an older version of the tooling. You can find the docs of the current version [here](../workflows.md).

A _studentified_ artifact can be generated from a CMT main repository
by running the `cmt-studentify` command. The process looks as follows:

![studentify process](https://i.imgur.com/8gH7Y7a.png)

The _studentified_ artifact is self-contained (and can optionally be generated
as a **_git_** repository) sbt build and is typically used in a training
context.

When sbt is started in the root folder of the _studentified_ artifact,
the following commands can be run from the sbt prompt:

![Studentified repo - commands](https://i.imgur.com/TgJ6rCD.png)

## Evolving the content of a CMT main repository

During the lifetime of a CMT main repository, the need will arise to change
its content. For example, one may need to:

- add an exercise at the end of the existing series of exercises
- insert a new exercise between two consecutive exercises
- change the title of an exercise
- change the course name
- change the code in an exercise and make the required changes
  to subsequent exercises

In general, for each of the above changes, there's an optimal way to
implement them. There are two approaches to applying changes:

- direct changes on the CMT main repository. For example, when the exercise
  instructions for a particular exercise need to be changed, this
  approach is optimal
- indirect changes via the so-called _linearize_/_delinearize_ process.
  This approach is recommended when code changes are applied in an
  exercise that is followed by one or more exercises: the "effect" of
  the changes needs to be applied to subsequent exercises. The generic
  approach when using **_git_** is to apply interactive rebasing.
  Obviously, there's no way we can do this on the CMT main repository
  and that's where `cmt-linearize` comes in.

A _linearized_ repo is a git repository in which each exercise in the CMT main
repo repository is "mapped" to a commit. The following diagrams depicts
the process:

![Linearize process](https://i.imgur.com/hsJy9ZT.png)  

To illustrate the process, assume we run the following command to linearize
a CMT main repository:
```
cmt-linearize -dot /Users/ericloots/Trainingen/LBT/lunatech-scala-2-to-scala3-course \
                   /Users/ericloots/tmp/lin
```

After a successful completion of this command, the _linearized_ repo will be
in a subfolder of `/Users/ericloots/tmp/lin` named `lunatech-scala-2-to-scala3-course`.

We can verify a couple of things on the _linearized_ repo.

```
$ cd /Users/ericloots/tmp/lin/lunatech-scala-2-to-scala3-course

$ git log --oneline
d92f752 (HEAD -> main) exercise_011_multiversal_equality
8e2cd20 exercise_010_opaque_type_aliases
ae5447e exercise_009_union_types
a3989c7 exercise_008_enum_and_export
bd9ad74 exercise_007_givens
e1052c7 exercise_006_using_and_summon
badeca3 exercise_005_extension_methods
aa02237 exercise_004_parameter_untupling
9b8bb2c exercise_003_top_level_definitions
6d55244 exercise_002_dotty_new_syntax_and_indentation_based_syntax
96e94ea exercise_001_dotty_deprecated_syntax_rewriting
098aa38 exercise_000_sudoku_solver_initial_state
```

We can observe that the last commit (HEAD) corresponds to the last exercise
on the main repository.

We can also inspect the differences between, say, exercises 8 and 9 as illustrated
here. Let's first see which files were changed between these exercises.

```
$ git diff --name-only HEAD~3 HEAD~2
exercises/README.md
exercises/src/main/scala/org/lunatechlabs/dotty/sudoku/SudokuProblemSender.scala
exercises/src/main/scala/org/lunatechlabs/dotty/sudoku/SudokuSolver.scala
```

Let's see what changed in `SudokuSolver.scala`:

```
$ git diff HEAD~3 HEAD~2  \
      exercises/src/main/scala/org/lunatechlabs/dotty/sudoku/SudokuProblemSender.scala
diff --git a/exercises/src/main/scala/org/lunatechlabs/dotty/sudoku/SudokuProblemSender.scala b/exercises/src/main/scala/org/lunatechlabs/dotty/sudoku/SudokuProblemSender.scala
index 8d5ef31..1ab0200 100644
--- a/exercises/src/main/scala/org/lunatechlabs/dotty/sudoku/SudokuProblemSender.scala
+++ b/exercises/src/main/scala/org/lunatechlabs/dotty/sudoku/SudokuProblemSender.scala
@@ -9,36 +9,31 @@ object SudokuProblemSender {

   enum Command {
     case SendNewSudoku
-    // Wrapped responses
-    case SolutionWrapper(result: SudokuSolver.Response)
   }
   export Command._

+  type CommandAndResponses = Command | SudokuSolver.Response
+
   private val rowUpdates: Vector[SudokuDetailProcessor.RowUpdate] =
     SudokuIO
       .readSudokuFromFile(new File("sudokus/001.sudoku"))
       .map ((rowIndex, update) => SudokuDetailProcessor.RowUpdate(rowIndex, update))

   def apply(sudokuSolver: ActorRef[SudokuSolver.Command],
-            sudokuSolverSettings: SudokuSolverSettings
-  ): Behavior[Command] =
-    Behaviors.setup { context =>
+            sudokuSolverSettings: SudokuSolverSettings): Behavior[Command] =
+    Behaviors.setup[CommandAndResponses] { context =>
       Behaviors.withTimers { timers =>
         new SudokuProblemSender(sudokuSolver, context, timers, sudokuSolverSettings).sending()
       }
-    }
+    }.narrow // Restrict the actor's [external] protocol to its set of commands
 }

 class SudokuProblemSender private (sudokuSolver: ActorRef[SudokuSolver.Command],
-                                   context: ActorContext[SudokuProblemSender.Command],
-                                   timers: TimerScheduler[SudokuProblemSender.Command],
-                                   sudokuSolverSettings: SudokuSolverSettings
-) {
+                                   context: ActorContext[SudokuProblemSender.CommandAndResponses],
+                                   timers: TimerScheduler[SudokuProblemSender.CommandAndResponses],
+                                   sudokuSolverSettings: SudokuSolverSettings) {
   import SudokuProblemSender._

-  private val solutionWrapper: ActorRef[SudokuSolver.Response] =
-    context.messageAdapter(response => SolutionWrapper(response))
-
   private val initialSudokuField = rowUpdates.toSudokuField

   private val rowUpdatesSeq = LazyList
@@ -77,14 +72,14 @@ class SudokuProblemSender private (sudokuSolver: ActorRef[SudokuSolver.Command],
                                problemSendInterval
   ) // on a 5 node RPi 4 based cluster in steady state, this can be lowered to about 6ms

-  def sending(): Behavior[Command] =
+  def sending(): Behavior[CommandAndResponses] =
     Behaviors.receiveMessage {
       case SendNewSudoku =>
         context.log.debug("sending new sudoku problem")
         val nextRowUpdates = rowUpdatesSeq.next
-        sudokuSolver ! SudokuSolver.InitialRowUpdates(nextRowUpdates, solutionWrapper)
+        sudokuSolver ! SudokuSolver.InitialRowUpdates(nextRowUpdates, context.self)
         Behaviors.same
-      case SolutionWrapper(solution: SudokuSolver.SudokuSolution) =>
+      case solution: SudokuSolver.SudokuSolution =>
         context.log.info(s"${SudokuIO.sudokuPrinter(solution)}")
         Behaviors.same
     }
```

This illustrates another use case of a _linearized_ repository: figuring out
what changes between exercises.

Let's return to the topic of this section: editing the content of a CMT
main repository. Typically, when making a change to the code in a particular
exercise, one wants to let the effect of such a change ripple through all
subsequent exercises. In some cases, it may be relatively straightforward to
apply such changes directly on the CMT main repository. However, in general,
doing so is prone to errors: necessary changes may be overlooked and annoying
minor differences in formatting may slip in. Therefore, it is recommended to
use **_git_** interactive rebasing instead as depicted in the following
diagram.

![Interactive rebasing process](https://i.imgur.com/z7N2Z4J.png)

When making large changes, it is recommended to split these in a series
of smaller steps. This may simplify the process of merge conflict resolution
if these arise during the completion of the interactive rebasing process.

The _linearized_ repo is generated in such a way that it can be loaded in
sbt (or an IDE). This means that tests can be run during the editing process
to verify that the refactored code still works correctly.

Once the refactoring of the code in the _linearized_ repository is complete,
the applied changes need to be reflected in the CMT main repository. This is
done via the _delinearization_ process as depicted in the following diagram:

![Delinearize process](https://i.imgur.com/BYlAaPh.png)

In our sample scenario, we run the following `cmt-delinearize` command to perform
the _delinearization_:

```
cmt-linearize /Users/ericloots/Trainingen/LBT/lunatech-scala-2-to-scala3-course \
              /Users/ericloots/tmp/lin/lunatech-scala-2-to-scala3-course
```

Running the `git status` command on the CMT master repository will show _all_
the files that were changed in the editing process.

With a refactoring cycle completed, we can repeat the process. As long as we
don't make any direct edits on any of the exercises in the CMT master repo,
we can repeat the **_git_** interactive rebasing process/_delinearization_
as many times as needed. Between iterations, we can also do the following:

- "checkpoint" what we already have on the CMT master repository by committing
  it. This doesn't hurt and if needed this can be undone easily.
- run the tests on all exercises. This can be done in two ways:
  - in sbt, select the root project and run the `test` command. This will
    run all the tests in all exercises
  - generate a test script via `cmt-mainadm` (see section 
    [_"generation of a test script"_](v1-reference-mainadm.md#generation-of-a-repository-test-script)
    for details). Doing the tests in this way not only runs the exercises
    tests, but also verify the proper functioning of both the _studentified_ and
    _linearized_ artifacts.

## Inserting, deleting and renumbering exercises

`cmt-mainadm` is your friend for these kind of tasks. See the following
sections in the reference chapter:

- [inserting an exercise](v1-reference-mainadm.md#duplicate-a-given-exercise-and-insert-it-before-that-exercise)
- [deleting an exercise](v1-reference-mainadm.md#deleting-an-exercise)
- [renumbering exercises](v1-reference-mainadm.md#renumber-exercises-with-a-given-offset-and-step-size)

## Changing the title of a course

The title of a course, which is displayed in the sbt prompt in both the CMT main
repository and the _studentified_ repo, is retrieved from a file name `.courseName`
in the CMT main repository's root folder. Simply edit the first line of this file
to reflect the desired course name.

## Verify a CMT main repository

Checking the full functionality of a CMT main repository manually is a
tedious and time-consuming process. For this reason, `cmt-mainadm` has an option
to create a test script that will run the following tests:

- execute all tests on every exercise in the CMT main repository
- _studentify_ the repo and run a series of commands (just like a student
  would do) on the _studentified_ repository
- _linearize_ the CMT main repository and run the tests on the "HEAD" exercise

Have a look at section [_"generation of a test script"_](v1-reference-mainadm.md#generation-of-a-repository-test-script)
for more details.
