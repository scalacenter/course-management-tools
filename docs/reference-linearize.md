---
id: reference-linearize
title:  cmt-linearize Command Reference
sidebar_label: cmt-linearize
---

## Command Invocation

```
Usage: linearize [options] mainRepo linearRepo

  mainRepo                 base folder holding main course repository
  linearRepo               base folder for linearized version repo
  -mjvm, --multi-jvm       generate multi-jvm build file
  -f, --force-delete       Force-delete a pre-existing destination folder
  -cfg, --config-file <value>
                           configuration file
  -dot, --dotty            studentified repository is a Dotty project
  -nar, --no-auto-reload-sbt
                           no automatic reload on build definition change
  -m, --bare-lin-repo      create a linearized repo without any of the CMT plugin functionality
  --help                   Prints the usage text
  -v, --version            Prints the version info
```

## Linearizing a CMT main repository

A _linearized_ repo is a git repository in which each exercise in the CMT main
repo repository is "mapped" to a commit. The following diagrams depicts
the process:

![Linearize process](https://i.imgur.com/hsJy9ZT.png)

In its simplest form, a linearized repo is created by invoking the
`cmt-linearize` command as follows:

```
cmt-linearize -dot /Users/ericloots/Trainingen/LBT/lunatech-scala-2-to-scala3-course \
                   /Users/ericloots/tmp/lin
```

Apart from the creation of a git repository, the sbt build definition
of the _linearized_ repo is slightly different from the CMT main repository's
build definition. This is shown in the following diagram:

![](https://i.imgur.com/jqihk1w.png)

An important takeaway from this diagram is that 4 files are key with respect
to the build definition of your project: `CommonSettings.scala`, `CompileOptions.scala`,
`Dependencies.scala`, and `AdditionalSettings.scala`.

The following sections cover the customisation via command-line options.

For the sake of demonstration, the examples shown below work on a CMT main
repository with the following exercises:

![Sample CMT repository](https://i.imgur.com/6iUQQPi.png)

### `-dot`: Dotty compatible build definition

With this option, the Scala version is assumed to be set exactly in the
`Version.scalaVersion` variable in your build definition. If this is not
the case, you will set the sbt build's `scalaVersion` settings elsewhere.

### `-nar`: Do not automatically reload the build on build changes

This option allows one to overrule the default behavior which is to
automatically reload the build when a change in the build definition
is detected by sbt.

### `-b`: Create a linearized repo without any of the CMT plugin functionality

This option is useful when the _linearized_ repository's main use is to inspect
the differences between consecutive exercises: it allows users to focus on just
that. By having no CMT plugin, the user is not confused or distracted by the
plugin code. Also, the root build definition (`build.sbt`) is reduced to its
minimal form.

The following diagrams illustrates how the build definition of the _linearized_
repository is generated:

![Bare linearized build definition](https://i.imgur.com/20hu1g2.png)

An important consequence is that one shouldn't have references to members
of the plugin source files in build definition files such as
`CommonSettings.scala`, `CompileOptions.scala`, `Dependencies.scala`,
`build.sbt`, and `AdditionalSettings.scala`

### `-f`: Force delete a pre-existing destination folder

By default, `cmt-linearize` will not overwrite a destination folder to avoid
"accidents". For convenience, the `-f` option will override this behavior and
remove a pre-existing folder and create a new one from scratch.

### `cmt-linearize` configuration settings

A number of configuration settings can be used to influence the behaviour of `cmt-linearize`:

- [exercise-project-prefix](reference-config.md#exercise-project-prefix)
- [studentified-base-folder](reference-config.md#studentified-base-folder)
- [relative-source-folder](reference-config.md#relative-source-folder)
- [studentified-project-name](reference-config.md#main-base-project-name--studentified-project-name)
- [exercise-preamble](reference-config.md#exercise-preamble)
- [use-configure-for-projects](reference-config.md#use-configure-for-projects)

> **WARNING**<br>
> When a _linearized_ repository is used in a _linearize_/_delinearize_
> code refactoring process, make sure to keep the CMT main repository and
> the _linearized_ repository consistent. This means that, if a _linearized_
> repository exists and one applies changes directly on the CMT main repo,
> the _linearized_ is no longer consistent with the content of the main repo.
> In that case, regenerate the _linearized_ repo.

> **NOTE**<br>
> `cmt-linearize` requires your working directory to have no unstaged files
>  and your staging area to be clean. If that condition is not met, resolve
>  it before trying again.

> **NOTE**<br>
> Even though a _linearized_ repo contains code and files that are shared
> by different exercises, these should not be edited in this repo as any
> such changes will **NOT** be reflected back to the CMT main repository
> during the _linearize_/_delinearize_ refactoring process. Instead, apply
> such changes directly on the CMT main repository. 