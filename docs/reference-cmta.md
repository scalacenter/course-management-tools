---
id: reference-cmta
title: cmta Command Reference
sidebar_label: cmta
---
## Command Summary

`cmta` takes a command as the first parameters and it operates on a CMT main repository.

The available commands and their function is as follows:

- [`studentify`](#cmta-studentify): generate a studentified artifact
- [`linearize  `](#cmta-linearize): generate a linearized artifact
- [`delinearize`](#cmta-delinearize): reflect the changes made in a linearized artifact back into the corresponding main repository
- [`renum      `](#cmta-renum): renumber exercises in a main repository
- [`dib        `](#cmta-dib): duplicate a selected exercise in a main repository and insert it before that exercise

The remainder of this section describes these commands in further detail.


### cmta studentify

#### Synopsys

`   cmta studentify [-fg] <Main repo> <studentified repo parent folder>`

#### Description

Generate a studentified artifact from a CMT main respository. The former is a self-contained artifact
that contains all exercise materials and a code folder that holds the complete state for the first
exercise. It can be distributed to users, in general people following a course, who can use `cmtc` to
navigate between exercises, "pull" an exercise solution, save the current state of their work for a given
exercise with the possibility to restore it at a later time.

> Note: `cmta studentify` will generate an error is the CMT main repository's git workspace isn't clean.
> So, commit any unsaved work before trying to studentify a repository.

The following options are available:

&nbsp;&nbsp;&nbsp;&nbsp;**-f**: Force-delete a pre-existing studentified artifact.

&nbsp;&nbsp;&nbsp;&nbsp;**-g**: Initialise the studentified artifact as a git repository.
<pre>
          This option can be useful in some use cases. For example, students may
          commit specific exercise state in git, then move to the next exercise
          and then use `git diff` to explore any changed test code.
</pre>

### cmta linearize

#### Synopsys

`   cmta linearize [-f] <Main repo> <linearized repo parent folder>`

#### Description

Generate a linearized artifact from a CMT main repository. The former is a git repository in which
each commit corresponds to the state of an exercise of the main repository. The first (oldest) commit
in the repo corresponds to the first exercise in the main repository. The commit message for each
commit is a single line with the name of the exercise in it.

The linearized repository can be used for different purposes, but the most important one is to
assist in editing the content of exercise code: this can be done using interactive rebasing on a
linearized repository. The changes applied on the latter via such process, can be applied by _delinearizing_
the linearized repository into the main repository.

The following options are available:

&nbsp;&nbsp;&nbsp;&nbsp;**-f**: Force-delete a pre-existing studentified artifact.

> Note: `cmta linearize` will generate an error is the CMT main repository's git workspace isn't clean.
> So, commit any unsaved work before trying to linearize a repository.

> Warning: It is important to leave the commit messages in the linearized repository as-is. Also
> never delete or insert commits. Not adhering to these rules will make it impossible to delinearize
> the repository.

### cmta delinearize

#### Synopsys

`   cmta delinearize <Main repo> <linearized repo parent folder>`

#### Description

Apply the state of a linearized repository to its corresponding main repository. Before performing
its task, de delinearization command will check that the liinearized repository and the main
repository "match", i.e. they should have the same number of exercises, with matching exercise names.

### cmta dib

#### Synopsys

`   cmta dib -n <exercise number N> <Main repo>`

#### Description

> Note: **_dib_** is an acronym for **_d_**uplicate [and] **_i_**nsert **_b_**efore.

The exercise number (N), passed in via the -n parameter, will be duplicated and inserted as a new exercise
before the exercise with that number.

It there is a gap before the original exercise in the exercise
series, the duplicated exercise will have as sequence number N - 1. Otherwise, room will be made for the
duplicated exercise by shifting exercises around. Note that if the exercises after the insertion point
have one or more gaps in the numbering, those gaps will be removed and the exercises will be renumbered as a
continuous series.

In general, the description of the new, duplicated exercise will be what it was followed
by __copy_.

### cmta renum

#### Synopsys

`   cmta renum [--from <value>,--to <value>,--step <value>] <Main repo>`

#### Description

By default, this command will renumber all exercises to a series starting at exercise
number 1, with the consecutive exercises number being incremented by one.

The different available command options allow to start the renumbering starting from a specific
exercise, move exercises to a different starting offset, and to specify by how much the exercise
numbers are incremented between consecutive exercises.

The following options are available:

&nbsp;&nbsp;&nbsp;&nbsp;**--from**: number of the exercise at which the renumbering needs to be started.
<pre>
          This option enables partial renumbering of the exercise set. The value passed as an
          argument to this option is the number of the exercise at which the renumbering shoulds
          start. All subsequent exercises, if any, will be renumbered.
</pre>