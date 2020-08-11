---
id: your_first_project
title: Your first CMT project
sidebar_label: Your first CMT project
---

## Starting from a CMT master template

### Introduction

The Course Management Tools come with a number of templates. The available
templates can be listed via `cmt-mainadm init -l` as shown here:

```
$ cmt-mainadm init -l
[0] dotty-cmt-template-common
[1] dotty-cmt-template-no-common
[2] play-cmt-template-no-common
[3] scala-cmt-template-common
[4] scala-cmt-template-no-common
``` 

So, at the time we ran the above command, 5 CMT templates were available: 2 for 
[Dotty](https://dotty.epfl.ch) projects, 2 for [Scala](https://scala-lang.org) and
one for a [Play](https://www.playframework.com) project.

> NOTE
> As you can see, suffix of the template names is either _"common"_ or _"no_common"_.
> If you have no idea what this means, start with the version ending in _"no_common"_.

These templates are tested and valid CMT master repositories. Each of them contain
just 2 exercises. They can be cloned as explained later in this section and
subsequently adapted to your needs.

During the cloning process, we can adapt the name of the course and decide on where
the clone will be created by specifying a target directory.

Cloning can be done in interactive- or non-interactive mode. Of course, in both cases,
you'll need the name of a template. So, first run `cmt-mainadm init -l` to get an
up-to-date list of CMT templates.

Let's have a look at interactive and [non-interactive mode](#non-interactive-mode).

### Interactive mode

When running `cmt-mainadm init`, we automatically enter the interactive mode. This will
prompt us for the parameters. The CMT template name, the name we want to assign to the
cloned course, and the [base] target directory. After successful completion of the 
command, a folder with the name of the course. The name is converted to all lowercase
characters and any spaces in the name swapped to dashes (`-`)).

Let's see how this works in a trial run:

```
$ cmt-mainadm init -l
[0] dotty-cmt-template-common
[1] dotty-cmt-template-no-common
[2] play-cmt-template-no-common
[3] scala-cmt-template-common
[4] scala-cmt-template-no-common

$ cmt-mainadm init
 Template name: scala-cmt-template-no-common
 Course name: Starting with Scala
 Target directory [/Users/ericloots/Trainingen/LBT/course-management-tools]:/Users/ericloots/tmp
 Checkout template into /var/folders/rq/vhwkgm9x2rs33jl4t2x6t6jr0000gn/T/sbt_78941d08
 Initialized empty Git repository in /private/var/folders/rq/vhwkgm9x2rs33jl4t2x6t6jr0000gn/T/sbt_78941d08/.git/
 Updating origin
 From https://github.com/eloots/course-management-tools
  * [new branch]      add-documentation      -> origin/add-documentation
  * [new branch]      add-social-media-preview-image-and-fix-footer-links -> origin/add-social-media-preview-image-and-fix-footer-links
  * [new branch]      gh-pages               -> origin/gh-pages
  * [new branch]      main                   -> origin/main
  * [new branch]      mainadm-validation-fix -> origin/mainadm-validation-fix
  * [new tag]         1.0.0                  -> 1.0.0
 From https://github.com/eloots/course-management-tools
  * branch            main       -> FETCH_HEAD
 Template created in /var/folders/rq/vhwkgm9x2rs33jl4t2x6t6jr0000gn/T/sbt_78941d08/course-templates/scala-cmt-template-no-common/
 Copying template to /Users/ericloots/tmp/starting-with-scala
 Initialized empty Git repository in /Users/ericloots/tmp/starting-with-scala/.git/
 [main (root-commit) dedb4fc] Initial commit
  28 files changed, 827 insertions(+)
  create mode 100644 .courseName
  create mode 100644 .gitignore
  create mode 100644 .sbtopts
  create mode 100644 README.md
  create mode 100644 build.sbt
  create mode 100644 course-management.conf
  create mode 100644 project/AdditionalSettings.scala
  create mode 100644 project/CommonSettings.scala
  create mode 100644 project/CompileOptions.scala
  create mode 100644 project/Dependencies.scala
  create mode 100644 project/MPSelection.scala
  create mode 100644 project/Man.scala
  create mode 100644 project/Navigation.scala
  create mode 100644 project/StudentCommandsPlugin.scala
  create mode 100644 project/StudentKeys.scala
  create mode 100644 project/build.properties
  create mode 100644 step_000_initial_state/README.md
  create mode 100644 step_000_initial_state/src/main/resources/logback.xml
  create mode 100644 step_000_initial_state/src/main/scala/org/cmt/Main.scala
  create mode 100644 step_000_initial_state/src/main/scala/org/cmt/Math.scala
  create mode 100644 step_000_initial_state/src/test/resources/logback-test.xml
  create mode 100644 step_000_initial_state/src/test/scala/org/cmt/MathSuite.scala
  create mode 100644 step_001_add_multiplication/README.md
  create mode 100644 step_001_add_multiplication/src/main/resources/logback.xml
  create mode 100644 step_001_add_multiplication/src/main/scala/org/cmt/Main.scala
  create mode 100644 step_001_add_multiplication/src/main/scala/org/cmt/Math.scala
  create mode 100644 step_001_add_multiplication/src/test/resources/logback-test.xml
  create mode 100644 step_001_add_multiplication/src/test/scala/org/cmt/MathSuite.scala

```
<br>
> NOTE
> The path of the target folder should be an absolute path to an existing folder

Let's have a look at the cloned repository.

```
$ cd /Users/ericloots/tmp

$ ls -l
drwxr-xr-x  12 ericloots  staff  384 Aug 11 11:41 starting-with-scala

$ cd starting-with-scala

$ sbt

<elided>

man [e] > Starting with Scala > initial state > projects
[info] In file:/Users/ericloots/tmp/starting-with-scala/
[info] 	   scala-master
[info] 	 * step_000_initial_state
[info] 	   step_001_add_multiplication

man [e] > Starting with Scala > initial state > test
[info] Compiling 2 Scala sources to /Users/ericloots/tmp/starting-with-scala/step_000_initial_state/target/scala-2.13/classes ...
[info] Compiling 1 Scala source to /Users/ericloots/tmp/starting-with-scala/step_000_initial_state/target/scala-2.13/test-classes ...
org.cmt.MathSuite:
  + Adding 0 to any integer value should return the same value 0.01s
  + Verify basic addition 0.001s
[info] Passed: Total 2, Failed 0, Errors 0, Passed 2
[success] Total time: 3 s, completed 11 Aug 2020, 09:46:53

man [e] > Starting with Scala > initial state > project step_001_add_multiplication
[info] set current project to step_001_add_multiplication (in build file:/Users/ericloots/tmp/starting-with-scala/)

man [e] > Starting with Scala > add multiplication > test
[info] Compiling 2 Scala sources to /Users/ericloots/tmp/starting-with-scala/step_001_add_multiplication/target/scala-2.13/classes ...
[info] Compiling 1 Scala source to /Users/ericloots/tmp/starting-with-scala/step_001_add_multiplication/target/scala-2.13/test-classes ...
org.cmt.MathSuite:
  + Adding 0 to any integer value should return the same value 0.007s
  + Verify basic addition 0.001s
  + Multiplying any integer value by 1 should return the same value 0.0s
  + Verify basic multiplication 0.001s
[info] Passed: Total 4, Failed 0, Errors 0, Passed 4
[success] Total time: 1 s, completed 11 Aug 2020, 09:47:04

man [e] > Starting with Scala > add multiplication >
 
```
<br>
A few things to note from the above output:

- We see that the name of the project, _Starting with Scala_ is reflected
  in:
  - the name of the cloned CMT template: _starting-with-scala_
  - the sbt prompt: (man [e] > **Starting with Scala** > initial state >_
- Running the sbt `projects` command shows us that there are two exercises. We
  see that the name of the exercises has been "tuned" with a _step_ prefix
- The tests are run on the first exercise (named _step_000_initial_state_) by
  running the sbt `test` command
- We switch to the second exercise by running the sbt
  `project step_001_add_multiplication` command and run the tests by running
   the sbt `test` command

### _Studentifying_ your first project

With the cloned project in place, we can now _studentify_ it and try out some of
the functionality of the _studentified_ repository. We will create the _studentified_
repository in the `/Users/ericloots/tmp/stu` folder:

```
$ cmt-studentify -dot /Users/ericloots/tmp/starting-with-scala /Users/ericloots/tmp/stu
CHECKING WORKSPACE in /Users/ericloots/tmp/starting-with-scala
Initialized empty Git repository in /private/var/folders/rq/vhwkgm9x2rs33jl4t2x6t6jr0000gn/T/sbt_6a77e07a/starting-with-scala.git/
To /var/folders/rq/vhwkgm9x2rs33jl4t2x6t6jr0000gn/T/sbt_6a77e07a/starting-with-scala.git
 * [new branch]      HEAD -> 5E8824BB-46D7-4246-8D8F-9981934B1D67
Cloning into 'starting-with-scala'...
done.
Processing exercises:
    step_000_initial_state
    step_001_add_multiplication

Setting student repository bookmark to step_000_initial_state

```

The _studentified_ repository is an sbt build with a number of extra custom commands:

- `listExercises`
- `man` and `man e` to display the global project README file and the exercise
  instructions respectively
- Moving around between exercises:
  - `nextExercise` and `prevExercise`: move to the next or previous exercise respectively.
  - `gotoExercise <exercise_name>`. Basically the same as `nextExercise`, but allowing
  an move to a specific exercise
  - `gotoFirstExercise`. Jump to the first exercise in the course
    - All the above commands share a common property: they will pull in the test code
      for the "selected" exercise. Any other code that was present prior to executing
      the command is left unchanged
- `pulSolution` will "pull" the solution for the current exercise. Note that this will
  overwrite any code present in the current exercise.
- `saveState` will "checkpoint" the current state (code) for the current exercise. An
  exercise's state can be save as many time as desired, but only the last saved state
  is retained
- `savedStates` will list any previously saved states
- `restoreState <saved state>`. Will restore the state of a previously saved state
  of an exercise. Note that this command will automatically reposition the current
  exercise to the restored one 


Let's give this a spin by switching to the _linearized_ repository and fire-up sbt
in its root folder.

```
$ cd /Users/ericloots/tmp/stu/starting-with-scala

~/tmp/stu/starting-with-scala on  main ⌚ 12:50:08
$ sbt
[info] welcome to sbt 1.3.13 (N/A Java 13.0.2)

<elided>

man [e] > Starting with Scala > initial state > listExercises
  1. * step_000_initial_state
  2.   step_001_add_multiplication

man [e] > Starting with Scala > initial state > man
# Basic Scala CMT master master project

## Description

This project can be used as a template for a master
project managed by the [Course Management Tools](https://github.com/eloots/course-management-tools)

It contains 2 simple steps (or exercises)

```
<br>
Ok, we see the two exercises, with the current exercise set to `step_000_initial_state`.
We also ran the `man` command to display the main README file.

Let's display the exercise instructions for the current exercise and run the tests for
that exercise.

```
# Initial state - basic addition

## Introduction

- Add basic addition in object Math
- Adds a main method


man [e] > Starting with Scala > initial state > test
[info] Compiling 2 Scala sources to /Users/ericloots/tmp/stu/starting-with-scala/exercises/target/scala-2.13/classes ...
[info] Compiling 1 Scala source to /Users/ericloots/tmp/stu/starting-with-scala/exercises/target/scala-2.13/test-classes ...
org.cmt.MathSuite:
  + Adding 0 to any integer value should return the same value 0.01s
  + Verify basic addition 0.001s
[info] Passed: Total 2, Failed 0, Errors 0, Passed 2
[success] Total time: 3 s, completed 11 Aug 2020, 10:55:17
```
<br>
We can now move to the next exercise by issuing the `nextExercise` command. To
demonstrate that we pull in the test code for the second exercise, but not the
solution, we will run the tests and witness that compilation errors are reported
as the solution code is missing:

```
man [e] > Starting with Scala > initial state > nextExercise
[INFO] Moved to step_001_add_multiplication

man [e] > Starting with Scala > add multiplication > man e
# Add multiplication

## Background

- Add multiplication to object Math

man [e] > Starting with Scala > add multiplication > test
[info] Compiling 1 Scala source to /Users/ericloots/tmp/stu/starting-with-scala/exercises/target/scala-2.13/test-classes ...
[error] /Users/ericloots/tmp/stu/starting-with-scala/exercises/src/test/scala/org/cmt/MathSuite.scala:24:20: not found: value multiply
[error] Error occurred in an application involving default arguments.
[error]     } assertEquals(multiply(i, 1), i)
[error]                    ^
[error] /Users/ericloots/tmp/stu/starting-with-scala/exercises/src/test/scala/org/cmt/MathSuite.scala:30:18: not found: value multiply
[error] Error occurred in an application involving default arguments.
[error]     assertEquals(multiply(100, 0), 0)
[error]                  ^
[error] /Users/ericloots/tmp/stu/starting-with-scala/exercises/src/test/scala/org/cmt/MathSuite.scala:31:18: not found: value multiply
[error] Error occurred in an application involving default arguments.
[error]     assertEquals(multiply(100, -1), -100)
[error]                  ^
[error] /Users/ericloots/tmp/stu/starting-with-scala/exercises/src/test/scala/org/cmt/MathSuite.scala:32:18: not found: value multiply
[error] Error occurred in an application involving default arguments.
[error]     assertEquals(multiply(13, 14), 182)
[error]                  ^
[warn] /Users/ericloots/tmp/stu/starting-with-scala/exercises/src/test/scala/org/cmt/MathSuite.scala:20:17: Unused import
[warn]     import Math._
[warn]                 ^
[warn] /Users/ericloots/tmp/stu/starting-with-scala/exercises/src/test/scala/org/cmt/MathSuite.scala:28:17: Unused import
[warn]     import Math._
[warn]                 ^
[warn] two warnings found
[error] four errors found
[error] (Test / compileIncremental) Compilation failed
[error] Total time: 0 s, completed 11 Aug 2020, 10:58:00
man [e] > Starting with Scala > add multiplication >
```
<br>
Of course, in a class room setting, the student's task would add the code required to
make the tests pass. For the sake of demonstration, we will simply "pull" that code
and run the tests again:

```
man [e] > Starting with Scala > add multiplication > pullSolution
[INFO] Solution for exercise step_001_add_multiplication pulled successfully
man [e] > Starting with Scala > add multiplication > test
[info] Compiling 2 Scala sources to /Users/ericloots/tmp/stu/starting-with-scala/exercises/target/scala-2.13/classes ...
[info] Compiling 1 Scala source to /Users/ericloots/tmp/stu/starting-with-scala/exercises/target/scala-2.13/test-classes ...
org.cmt.MathSuite:
  + Adding 0 to any integer value should return the same value 0.008s
  + Verify basic addition 0.001s
  + Multiplying any integer value by 1 should return the same value 0.0s
  + Verify basic multiplication 0.001s
[info] Passed: Total 4, Failed 0, Errors 0, Passed 4
[success] Total time: 1 s, completed 11 Aug 2020, 11:00:51
man [e] > Starting with Scala > add multiplication >
``` 
<br>
As expected, the test now pass.

We will finish of by demonstrating the `saveState`, `savedStates`, and `restoreState`
commands.

```
man [e] > Starting with Scala > add multiplication > listExercises
  1.   step_000_initial_state
  2. * step_001_add_multiplication

man [e] > Starting with Scala > add multiplication > savedStates
[WARN] No previously saved exercise states found

man [e] > Starting with Scala > add multiplication > saveState
[INFO] State for exercise step_001_add_multiplication saved successfully

man [e] > Starting with Scala > add multiplication > savedStates
[INFO] Saved exercise states are available for the following exercise(s):
        step_001_add_multiplication

man [e] > Starting with Scala > add multiplication > gotoFirstExercise
[INFO] Moved to first exercise in course

man [e] > Starting with Scala > add multiplication > listExercises
  1. * step_000_initial_state
  2.   step_001_add_multiplication

man [e] > Starting with Scala > initial state > pullSolution
[INFO] Solution for exercise step_000_initial_state pulled successfully

man [e] > Starting with Scala > initial state > test
[info] Compiling 2 Scala sources to /Users/ericloots/tmp/stu/starting-with-scala/exercises/target/scala-2.13/classes ...
[info] Compiling 1 Scala source to /Users/ericloots/tmp/stu/starting-with-scala/exercises/target/scala-2.13/test-classes ...
org.cmt.MathSuite:
  + Adding 0 to any integer value should return the same value 0.006s
  + Verify basic addition 0.0s
[info] Passed: Total 2, Failed 0, Errors 0, Passed 2
[success] Total time: 1 s, completed 11 Aug 2020, 11:04:22

man [e] > Starting with Scala > initial state > restoreState step_001_add_multiplication
[INFO] Exercise step_001_add_multiplication restored

man [e] > Starting with Scala > add multiplication > listExercises
  1.   step_000_initial_state
  2. * step_001_add_multiplication

man [e] > Starting with Scala > add multiplication > test
[info] Compiling 2 Scala sources to /Users/ericloots/tmp/stu/starting-with-scala/exercises/target/scala-2.13/classes ...
[info] Compiling 1 Scala source to /Users/ericloots/tmp/stu/starting-with-scala/exercises/target/scala-2.13/test-classes ...
org.cmt.MathSuite:
  + Adding 0 to any integer value should return the same value 0.006s
  + Verify basic addition 0.0s
  + Multiplying any integer value by 1 should return the same value 0.001s
  + Verify basic multiplication 0.0s
[info] Passed: Total 4, Failed 0, Errors 0, Passed 4
[success] Total time: 1 s, completed 11 Aug 2020, 11:04:58

man [e] > Starting with Scala > add multiplication >
```

### Non-interactive mode

We can create a clone of the CMT master template in non-interactive mode by
specifying the required parameters on the command line as shown here:

```
mainadm init --template scala-cmt-template-no-common --name "Starting with Scala" --target /Users/ericloots/tmp
 Template name: scala-cmt-template-no-common
 Course name: Starting with Scala
 Target directory [/Users/ericloots/Trainingen/LBT/course-management-tools]:/Users/ericloots/tmp
 Checkout template into /var/folders/rq/vhwkgm9x2rs33jl4t2x6t6jr0000gn/T/sbt_78941d08
 Initialized empty Git repository in /private/var/folders/rq/vhwkgm9x2rs33jl4t2x6t6jr0000gn/T/sbt_78941d08/.git/
 Updating origin
 From https://github.com/eloots/course-management-tools
  * [new branch]      add-documentation      -> origin/add-documentation
  * [new branch]      add-social-media-preview-image-and-fix-footer-links -> origin/add-social-media-preview-image-and-fix-footer-links
  * [new branch]      gh-pages               -> origin/gh-pages
  * [new branch]      main                   -> origin/main
  * [new branch]      mainadm-validation-fix -> origin/mainadm-validation-fix
  * [new tag]         1.0.0                  -> 1.0.0
 From https://github.com/eloots/course-management-tools
  * branch            main       -> FETCH_HEAD
 Template created in /var/folders/rq/vhwkgm9x2rs33jl4t2x6t6jr0000gn/T/sbt_78941d08/course-templates/scala-cmt-template-no-common/
 Copying template to /Users/ericloots/tmp/starting-with-scala
 Initialized empty Git repository in /Users/ericloots/tmp/starting-with-scala/.git/
 [main (root-commit) dedb4fc] Initial commit
  28 files changed, 827 insertions(+)
  create mode 100644 .courseName
  create mode 100644 .gitignore
  create mode 100644 .sbtopts
  create mode 100644 README.md
  create mode 100644 build.sbt
  create mode 100644 course-management.conf
  create mode 100644 project/AdditionalSettings.scala
  create mode 100644 project/CommonSettings.scala
  create mode 100644 project/CompileOptions.scala
  create mode 100644 project/Dependencies.scala
  create mode 100644 project/MPSelection.scala
  create mode 100644 project/Man.scala
  create mode 100644 project/Navigation.scala
  create mode 100644 project/StudentCommandsPlugin.scala
  create mode 100644 project/StudentKeys.scala
  create mode 100644 project/build.properties
  create mode 100644 step_000_initial_state/README.md
  create mode 100644 step_000_initial_state/src/main/resources/logback.xml
  create mode 100644 step_000_initial_state/src/main/scala/org/cmt/Main.scala
  create mode 100644 step_000_initial_state/src/main/scala/org/cmt/Math.scala
  create mode 100644 step_000_initial_state/src/test/resources/logback-test.xml
  create mode 100644 step_000_initial_state/src/test/scala/org/cmt/MathSuite.scala
  create mode 100644 step_001_add_multiplication/README.md
  create mode 100644 step_001_add_multiplication/src/main/resources/logback.xml
  create mode 100644 step_001_add_multiplication/src/main/scala/org/cmt/Main.scala
  create mode 100644 step_001_add_multiplication/src/main/scala/org/cmt/Math.scala
  create mode 100644 step_001_add_multiplication/src/test/resources/logback-test.xml
  create mode 100644 step_001_add_multiplication/src/test/scala/org/cmt/MathSuite.scala

```
