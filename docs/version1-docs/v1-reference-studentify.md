---
id: v1-reference-cmtstudentify
title: cmt-studentify Command Reference
sidebar_label: cmt-studentify
---

## Command Invocation

```
Usage: studentify [options] mainRepo out

  mainRepo                 base folder holding main course repository
  out                      base folder for student repo
  -mjvm, --multi-jvm       generate multi-jvm build file
  -fe, --first-exercise <value>
                           name of first exercise to output
  -le, --last-exercise <value>
                           name of last exercise to output
  -sfe, --selected-first-exercise <value>
                           name of initial exercise on start
  -cfg, --config-file <value>
                           configuration file
  -g, --git                initialise studentified repository as a git
                           repository
  -dot, --dotty            studentified repository is a Dotty project
  -nar, --no-auto-reload-sbt
                           no automatic reload on build definition change
  --help                   Prints the usage text
  -v, --version            Prints the version info
```

## _Studentifying_ a CMT main repository

The process of creating a _studentified_ version of a CMT main repository
looks as follows:

![studentify process](https://i.imgur.com/8gH7Y7a.png)

In its simplest form, this can be accomplished with the following invocation
of `cmt-studentify`:

```
cmt-studentify /Users/ericloots/Trainingen/LBT/lunatech-scala-2-to-scala3-course \
               /Users/ericloots/tmp/stu
```

<br>
> NOTE: `cmt-studentify` requires your working directory to have
> no unstaged files and your staging area to be clean. If this is not the case, you
> will see the following error message:
> 
>`YOU HAVE UNCOMMITTED CHANGES IN YOUR GIT INDEX. COMMIT CHANGES AND RE-RUN THE COMMAND`
>
> As the error message suggest commit all changes and re-run the command.

After a successful completion of this command, the _studentified_ repository
will be in subfolder `lunatech-scala-2-to-scala3-course` of folder
`/Users/ericloots/tmp/stu`.

The process of _studentifying_ a main repository can be customised in a
number of ways either by passing some options when invoking `cmt-studentify`
or via configuration.

The following sections cover the customisation via command-line options.

For the sake of demonstration, the examples shown below work on a CMT main
repository with the following exercises:

![Sample CMT repository](https://i.imgur.com/6iUQQPi.png)

### `-dot`: Dotty compatible build definition

With this option, the Scala version is assumed to be set exactly in the
`Version.scalaVersion` variable in your build definition. If this is not
the case, you will set the sbt build's `scalaVersion` settings elsewhere.

### `-nar`: Do not automatically reload the build on build changes

This option is used to overrule the default behavior which is to
automatically reload the build when a change in the build definition
is detected by sbt.

### Selecting a range of exercises to _studentify_

In some cases, it can be handy to select a subset of the available exercises
in a CMT main repository to _studentify_.

For example, the CMT main repository may have alternative implementations of
a series of exercises (e.g. using different libraries or programming languages)
in the CMT main repository and depending on the scenario, one may wish to only
export an exercise series for one specific alternative.

The `-fe` and `-le` options allow to do just that. Here's an example that will
export all exercises between exercises number 5 and 10 from a CMT main
repository:

```
cmt-studentify -fe exercise_005_extension_methods   \
               -le exercise_010_opaque_type_aliases \
               /Users/ericloots/Trainingen/LBT/lunatech-scala-2-to-scala3-course \
               /Users/ericloots/tmp/stu
```

Starting an sbt session in the _studentified_ repository shows the result:

```
$ cd /Users/ericloots/tmp/stu/lunatech-scala-2-to-scala3-course

$ $ sbt
[info] welcome to sbt 1.3.12 (N/A Java 13.0.2)

<elided>

man [e] > Scala 2 to Scala 3 > extension methods > listExercises
  1. * exercise_005_extension_methods
  2.   exercise_006_using_and_summon
  3.   exercise_007_givens
  4.   exercise_008_enum_and_export
  5.   exercise_009_union_types
  6.   exercise_010_opaque_type_aliases
man [e] > Scala 2 to Scala 3 > extension methods >
```

The `*` in the exercise list marks the position of the current exercise.

### Changing the default initial exercise

By default, _cmt-studentify_ will set the current position of the exercises to
the first exercise in the series of _studentified_ exercises. This can be changed
via the `-sfe` option as shown here:

```
cmt-studentify -fe  exercise_005_extension_methods   \
               -le  exercise_010_opaque_type_aliases \
               -sfe exercise_008_enum_and_export     \
               /Users/ericloots/Trainingen/LBT/lunatech-scala-2-to-scala3-course \
               /Users/ericloots/tmp/stu
```

Starting an sbt session in the _studentified_ repository shows the result and
confirms that the current position is indeed at `exercise_008_enum_and_export`:

```
$ cd /Users/ericloots/tmp/stu/lunatech-scala-2-to-scala3-course

$ sbt
[info] welcome to sbt 1.3.12 (N/A Java 13.0.2)

<elided>

man [e] > Scala 2 to Scala 3 > extension methods > listExercises
  1.   exercise_005_extension_methods
  2.   exercise_006_using_and_summon
  3.   exercise_007_givens
  4. * exercise_008_enum_and_export
  5.   exercise_009_union_types
  6.   exercise_010_opaque_type_aliases
man [e] > Scala 2 to Scala 3 > enum and export >
```

### Initialise a _studentified_ repository as a **_git_** repository

The `-g` option will _studentify_ a CMT main repository and initialise it as a
**_git_** repository with a single commit.

Here's a sample run and the output of `git status` on the _studentified_
repository:

```
$ cmt-studentify -g \
                 /Users/ericloots/Trainingen/LBT/lunatech-scala-2-to-scala3-course \
                 /Users/ericloots/tmp/stu
<elided>

$ cd /Users/ericloots/tmp/stu/lunatech-scala-2-to-scala3-course

$ git log --oneline
8d09228 (HEAD -> main) Initial commit
```

### The sbt build definition of a _studentified_ CMT main repository

The build definition of a studentified CMT repository is constructed as
illustrated in the following diagram:

![](https://i.imgur.com/kozerak.png)

### `cmt-studentify` configuration settings

A number of configuration settings can be used to influence the behaviour of `cmt-studentify`:

- [test-code-folders](v1-reference-config.md#test-code-folders)
- [exercise-project-prefix](v1-reference-config.md#exercise-project-prefix)
- [studentified-base-folder](v1-reference-config.md#studentified-base-folder)
- [readme-in-test-resources](v1-reference-config.md#readme-in-test-resources)
- [relative-source-folder](v1-reference-config.md#relative-source-folder)
- [main-base-project-name & studentified-project-name](v1-reference-config.md#main-base-project-name--studentified-project-name)
- [exercise-preamble](v1-reference-config.md#exercise-preamble)
- [studentify-files-to-clean-up](v1-reference-config.md#studentify-files-to-clean-up)
- [console-colors](v1-reference-config.md#console-colors)
- [common-project-enabled](v1-reference-config.md#common-project-enabled)
- [use-configure-for-projects](v1-reference-config.md#use-configure-for-projects)

