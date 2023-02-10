---
id: reference-config
title: CMT configuration reference
sidebar_label: CMT configuration
---

## Configuration

The behaviour of the `cmta` command can be changed via configuration.

If a file named `course-management.conf`  is present in the root folder of
the CMT main repository, CMT settings in that file will override the default
value from the [reference configuration](https://github.com/eloots/course-management-tools/blob/main/core/src/main/resources/reference.conf).


`cmta` takes the `-cfg <cfg file>` option which can be used
to specify an alternative location and name of a CMT configuration file.
Note that the path to the configuration file is a path relative to the root
folder of the CMT main repository.

The following section gives an overview of the available CMT configuration
settings and their default value.

## CMT configuration settings

### main-repo-exercise-folder

Default value = `code`

`main-repo-exercise-folder` is the path of the folder containing the course exercises.

Note that exercise folder should follow a naming convention: a prefix followed by
an underscore, a 3 digit exercise sequence numner, an underscore, and an exercise
description. The exercise prefix and the exercise description should not contain white
space characters.

### studentified-repo-active-exercise-folder

Default value = `code`

The folder in which the active exercise is stored in a studentified repository 
can be set to a different vlaue than the folder that stores the exercises in the main
repository.

### linearized-repo-active-exercise-folder

Default value: is the value of the `studentified-repo-active-exercise-folder` setting

This setting is similar to the `studentified-repo-active-exercise-folder` setting, but it
applies to the _linearized_ CMT artifact

### test-code-folders

Default value = `[ "src/test" ]`

This is a list of files or folders, relative to the exercise folder's path, in which
test code is located.

If the CMT main repository has exercises that have test code in other folders
than `src/test`, the paths of these folders should be added to this configuraton
setting. This will ensure that, when a student switches to a different exercise
(by using the `cmtc` command), the relevant testing code is _"pulled in"_.

### read-me-files

Default value = ` [ "README.md" ]`

A list of files or folders that contain exercise specific "Read me" information, such
as exercise instructions, reading materials, images, etc.

### cmt-studentified-dont-touch

Default value = `[ ".idea", ".bsp", ".bloop" ]`

`cmt-studentified-dont-touch` is a list of files and folders that should not be changed
when running `cmta pull-solution`, `cmta-restore-state`.

Tweak this setting in function of the tooling that students will use to do the course
exercises. In general, IDEs and build tools tend to use hidden folders to store tool
specific data and the tools may not react in a user-friendly way when `cmtc` changes
or deletes such folders while performing its tasks.

## Reference configuration

The complete reference configuration can be found [here](https://github.com/eloots/course-management-tools/blob/main/core/src/main/resources/reference.conf).
