---
id: install
title: Installing Course Management Tools
sidebar_label: Installation
---

## Supported Operating Systems and Prerequisites

### Supported OS

The Course Management Tools have been tested on the following Operating Systems:

- MacOS 10.14 or higher
- Ubuntu 20.04 LTS or higher (tested via CI/CD)
- Windows 10 & 11

### Prerequisites

Course creators/maintainers should have a
[recent version of ***git***](https://git-scm.com/downloads) installed on
their system.

Course users may need to have ***git*** installed under certain conditions
(for example, when the studentified artefact was initialised as a ***git***
repository.

## Installation

The preferred way to install the Course Management Tools to use
[Coursier](https://github.com/coursier/coursier/).

**If you haven't installed Coursier, install it first by following the instructions here: [Install Scala on your computer](https://docs.scala-lang.org/getting-started/index.html#install-scala-on-your-computer).**

With Coursier, the installation procedure is rather straightforward as shown in the
following sections.

### Install `cmtc` - the CMT Client CLI

```bash
$ cs install --contrib cmtc
Wrote cmtc
```

And verify it's working properly:

```bash
$ cmtc
Usage: cmtc <COMMAND>

Commands:
  goto-exercise        Move to a given exercise. Pull in tests and readme files for that exercise
  goto-first-exercise  Move to the first exercise. Pull in tests and readme files for that exercise
  install              Install a course - from either a local directory, a zip file on the local file system or a Github project
  list-exercises       List all exercises and their IDs in the repo. Mark the active exercise with a star
  list-saved-states    List all saved exercise states, if any.
  next-exercise        Move to the next exercise. Pull in tests and readme files for that exercise
  previous-exercise    Move to the previous exercise. Pull in tests and readme files for that exercise
  pull-solution        Pull in all code for the active exercise. All local changes are discarded
  pull-template        Selectively pull in a given file or folder for the active exercise
  restore-state        Restore a previously saved exercise state
  save-state           Save the state of the active exercise
  set-current-course   Sets the current course to point to a directory
  version              Print version info
```

### Install `cmta` - the CMT Administrator CLI

```bash
$ cs install --contrib cmta
Wrote cmta
```

And verify it's working properly:

```bash
$ cmta
Usage: cmta <COMMAND>

Commands:
  new                      Create a new course from an existing course template in a Github repository - by default the `lunatech-labs` organisation is used.
  delinearize              'Delinearizes' an existing master repository
  duplicate-insert-before  Duplicates a given exercise in a 'main' repository shifting subsequent exercises if needed
  linearize                'Linearizes' a 'main' repository in the target directory where the linearized repo has one commit per exercise
  renumber-exercises       Renumbers the exercises in the main repository
  studentify               'Studentifies' an existing repository - taking the 'main' repository and creating a CMT project in the target directory
  version                  Print version info
```
