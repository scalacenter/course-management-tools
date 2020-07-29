---
id: reference-mainadm
title: cmt-mainadm Command Reference
sidebar_label: cmt-mainadm
---
## Command Invocation

```
Usage: mainadm [options] mainRepo

  mainRepo                 base folder holding main course repository
  -mjvm, --multi-jvm       generate multi-jvm build file
  -b, --build-file-regen   regenerate project root build file
  -d, --delete <value>
  -r, --renumber           renumber exercises
  -ro, --renumber-offset <value>
                           renumber exercises - offset
  -rs, --renumber-step <value>
                           renumber exercises - step
  -dib, --duplicate-insert-before <value>

  -cfg, --config-file <value>
                           configuration file
  -c, --check-main-repo    verify soundness of main repository
  -amc, --add-main-commands
                           add command files to main repository
  -t, --generate-tests-script <value>
                           generate a script that tests main repo, studentified repo functionality and linearize/delinearize
  -g, --init-studentified-test-repo-as-git
                           Generate studentified repo as a git repo in tests
  -dot, --dotty            studentified repository is a Dotty project
  -nar, --no-auto-reload-sbt
                           no automatic reload on build definition change
  --help                   Prints the usage text
  -v, --version            Prints the version info
 ```

## Main administration tasks

The `cmt-mainadm` command takes a single argument: the absolute path of the
CMT main repository.

Depending on the action specified, the following options may apply:

### `-dot`: Dotty compatible build definition

With this option, the Scala version is assumed to be set exactly in the
`Version.scalaVersion` variable in your build definition. If this is not
the case, you will set the sbt build's `scalaVersion` settings elsewhere.

### `-nar`: Do not automatically reload the build on build changes

This option allows one to overrule the default behavior which is to
automatically reload the build when a change in the build definition
is detected by sbt.

## Deleting an exercise

The following command will delete the exercise with number `n` from the main
course repository and re-generate the root `build.sbt` file:

```
$ cmt-mainadm -d <n> [-cfg config_file] [-dot] [-nar] <main_repo>
```

## Duplicate a given exercise and insert it before that exercise

The following command will duplicate the exercise with number `n`.
The duplicate will retain the number of the exercise that is duplicated
while the latter and subsequent exercises to be shifted "down".

```
$ cmt-mainadm -dib <n> [-cfg config_file] [-dot] [-nar] <main_repo>
``` 

For example, imagine a main repo with a list of exercises with exercise
numbers increasing by 1. The following table shows the before and after
when running the `cmt-mainadm -dib 3 ...` command:

![DIB exercise # 3 - no gap](https://i.imgur.com/oSp1KXY.png)

If there is "space" to insert the duplicate, no renumbering will occur as
the before and after shows when running the `cmt-mainadm -dib 6 ...` command:

![DIB exercise # 3 - gap](https://i.imgur.com/CYJafQH.png)

## Renumber exercises with a given offset and step size

In some situations, it can be useful to renumber the exercises in a main
repository. Perhaps you want to create a "gap" in which some new exercises
will be put, or you want to shift all exercises to some offset.

This can be achieved with the _renumber_ option on `cmt-mainadm`.

Here are a couple of examples with a before and after situation.

Running `cmt-mainadm -r -rs 5 -ro 10 ...`

![Renumber with non-default offset and stepsize](https://i.imgur.com/pm7m6f9.png)

Running `cmt-mainadm -r -rs 7 ...`

![Renumber with non-default stepsize](https://i.imgur.com/URbqTV3.png)

Running `cmt-mainadm -r -ro 10 ...`

![Renumber with non-default offset](https://i.imgur.com/kBAaEaC.png)

## Generation of a repository test script

In the course of the life-time of a CMT project, the exercise series most
probably will change quite a bit by changing exercises, moving them around,
deleting and adding exercises and you may want to switch to a new version of
the CMT tools when it becomes available.

You want to make sure that your project is still running fine: the code
compiles without errors and all tests pass for _**each and every**_ exercise.
Verifying this manually is tedious and error prone.

The alternative is to automate the process and this can be done with the
help of `cmt-mainadm`.

Running `cmt-mainadm -t <some_test_file> [-cfg config_file] [-dot] [-nar] <main_repo>`
will generate a script (with the file name passed as an argument to the `-t` option)
that, when executed, will run the following steps:

- for each of the exercises in the main repository:
  - compile the exercise code
  - run the exercise tests
  - run the `man e` command
- it _linearizes_ the main repository and it executes the test on the _last_
exercise (at the HEAD of the **git** repo)
- it _studentifies_ the main repository and in this repo it runs through
_all_ the exercises in a random manner and for each of these exercise it:
  - jumps to that exercise
  - pulls the solution
  - runs the tests
  - saves the state
  - lists the saved states
  - restores the saved state

Ideally, you should set-up CI/CD to perform this test process on each PR and
PR merge. For inspiration, you may want to have a look at
[the way this is done](https://github.com/lunatech-labs/lunatech-scala-2-to-scala3-course/blob/main/.github/workflows/ci.yml)
via Github Actions in the Lunatech Labs [Moving from Scala 2 to Scala 3 Github
course](https://github.com/lunatech-labs/lunatech-scala-2-to-scala3-course).

## Rebuild the root `build.sbt` of a CMT main repository

Whenever manual changes are made to the exercise names in the main repository,
the sbt build definition must be adapted to reflect this change. It is
recommended to use `cmt-mainadm` to do this and it is done with the following
command:

```
$ cmt-mainadm -b [-cfg config_file] [-dot] [-nar] <main_repo>
```



