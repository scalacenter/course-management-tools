---
id: v1-reference-config
title: CMT configuration reference
sidebar_label: CMT configuration
---

## Configuration

The behaviour of the different CMT commands can be influenced by changing
the tool settings.

If a file named `course-management.conf`  is present in the root folder of
the CMT main repository, the CMT tools will take customised settings from
that file.

All CMT commands take the `-cfg <cfg file>` option which can be used
to specify an alternative location and name of a CMT configuration file.
Note that the path to the configuration file is a path relative to the root
folder of the CMT main repository.

The following section gives an overview of the available CMT configuration
settings and their default value.

## test-code-folders

This is a list of folders, relative to the exercise folder's path, in which
test code is located.

Applies to `cmt-studentify`.

#### Default value

```
studentify.test-code-folders = [
    "src/test"
  ]
```

If your CMT main repository has exercises that have test code in other folders
than `src/test`, you should add the paths of these folders to this configuraton
setting. This will ensure that, when one switches to a different exercise, the
relevant testing code is _"pulled in"_.

## exercise-project-prefix

Applies to _all_ CMT commands.

#### Default value

```
studentify.exercise-project-prefix = exercise
```

By default, the exercise folder names in a CMT main repository have to follow
a naming convention and consists of three parts

- an exercise prefix
- an exercise number consisting of a 3-digit number with leading zeros,
  preceded and followed by a single underscore character
- a free format exercise description

By default the exercise prefix is `exercise`. In a non-course setting, it makes
sense to change this to for example `step` as shown here:

```
studentify.exercise-project-prefix = step
```

With this modified setting, the names of exercises will be `step_nnn_*`.

## studentified-base-folder

This can be used to set the name of the base folder for `studentified` or
`linearized` code. In general, when changing the [exercise-project](v1-reference-config.md#exercise-project-prefix)
setting, you probably want to change the `studentified-base-folder` too.

Applies to `cmt-studentify`, `cmt-linearize`, `cmt-delinearize` 

#### Default value

```
studentify.studentify-mode-classic.studentified-base-folder = exercises
```

## readme-in-test-resources

This setting is used to set the location of exercise instruction files
(README.md).

For historical reasons that are no longer relevant, the convention is
that these files are located in each exercise's `src/test/resources` folder.

Applies to `cmt-studentify`.

#### Default value

```
studentify.readme-in-test-resources = true
```

When changing this setting to `false`, the location changes to an
exercises base folder.

## relative-source-folder

This setting is used to determine the relative location of a CMT main
repository in a **_git_** repository. This is useful in the following cases:

- there's a single CMT main repository in the **_git_** repository, but it's
  located in a subfolder for "organizational" reasons. For example, there
  may be other top level folders holding a slide-deck, demo code, etc.
  In that case, you could put the CMT main repository in an `exercises`
  subfolder
- a single **_git_** repository contains multiple CMT main repositories. A
  good example of this is when you have a course that has multiple versions
  for different languages (e.g. Java and Scala).

Applies to _all_ CMT commands.

#### Default value

```
studentify.relative-source-folder = ""
```

## main-base-project-name & studentified-project-name

These settings allow you to set the name of the aggregate root of the CMT
main repository and the _studentified_ (or _linearized_) artifacts
respectively.

This is handy when using an IDE. If one doesn't customize these settings,
your project history will contain identical references to different repos
which will make it very difficult to choose the one you're looking for.

Applies to `cmt-studentify` and `cmt-linearize`.

#### Default value

```
studentify.main-base-project-name = "base"
studentify.studentified-project-name = "base"
```

## exercise-preamble

This settings is used to add per-exercise specific sbt build code and
is useful for example to enable sbt plugins and/or sbt plugin settings
for every exercise. You can see an example of this in the [Play CMT main
template repo](https://github.com/scalacenter/course-management-tools/blob/3a8ca61fff34fe5f1a7daed81f96c9b95c167f0c/course-templates/play-cmt-template-no-common/course-management.conf#L19).

Applies to `cmt-mainadm`, `cmt-studentify`, `cmt-linearize`

#### Default value

```
studentify.exercise-preamble = ""
```

## studentify-files-to-clean-up

When _studentifying_ a CMT main repository, you may have some files that you
don't want to transfer to the _studentified_ repo. This setting allows you to
configure a list of such files.

Applies to `cmt-studentify`.

#### Default value

```
studentify.studentify-files-to-clean-up = [
    .git
    .gitignore
    man.sbt
    navigation.sbt
    shell-prompt.sbt
    Jenkinsfile
    Jenkinsfile.original
    course-management.conf
  ]
``` 

## console-colors

This set of options allows one to tune the colors of the sbt prompt in
a _studentified_ project. Use `reset` to set the color to the default
foreground color.

Applies to `cmt-studentify`.

#### Default value 

```
studentify.console-colors {
  prompt-man-color           = green
  prompt-course-name         = reset
  prompt-exercise-name-color = green
}
```

## common-project-enabled

In some cases, it is useful to include a significant amount of code that
is common to all exercises but that is not necessarily the focus of the
exercises. In such cases, the code can be moved to project `common` and
all exercises depend on it.

In the majority of use cases however, this is not needed in which case it
can be disabled.

#### Default value

```
studentify.common-project-enabled = true
```

## use-configure-for-projects

This option allows one to apply different settings per exercise than the one
applied to the overall (aggregate) project.

Applies to `cmt-studentify`, `cmt-mainadm` and `cmt-linearize`.

#### Default value

```
studentify.use-configure-for-projects = false
```

When this option is set to false, _every_ exercise, the `common` project and
the overall project will apply the settings defined in
`CommonSettings.commonSettings`. When set to true, _every_ exercise will
apply the settings defined in `CommonSettings.configure`. 

## Reference configuration

The complete reference configuration can be found [here](https://github.com/scalacenter/course-management-tools/blob/76b476ccaba3cf8b5a9f1ae499cd56aa342e59a0/core/src/main/resources/reference.conf#L1).
