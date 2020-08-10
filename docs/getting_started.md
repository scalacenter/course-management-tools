---
id: getting_started
title: Getting Started
sidebar_label: Introduction
---

## A brief history of CMT (Course Management Tools)

Soon after joining Lightbend in 2016, I started working on training course
development and maintenance. For versioning purposes, the code for the exercises in
each course was maintained in a **_git_** repository in which each exercise was mapped
to a commit. So, in a course with say, 15 exercises, the
**_git_** repository would have 15 commits. That was very nice in principle, but it made it very difficult from a maintenance point of
view: updating of exercises was done via **_git_** interactive rebasing. Doing so
rewrites the history of the exercise repository which is pretty bad because:

- when tracking previous versions of a course, the best one can do is to save
  that version in a separate branch
- If there were any open Pull Requests on the course, the interactive rebasing
  process will render these pretty much useless

One of my Lightbend colleagues at that time had already made a few attempts at
coming up with a more flexible approach. I picked up the thread and implemented a
system that became the Course Management Tools (CMT). The main goals I set out to
achieve were:

- put all course exercises in a **_git_** repository, the so-called _CMT Main
  Repository_ (_CMT MR_), that allows versioning of the exercises without being
  _forced_ to use interactive rebasing when editing exercises. Also, open
  Pull Requests are preserved when making changes to the exercises

- create a tool to derive an artifact from the _CMT MR_ that is optimised for
  student usage:
  - self-contained (i.e. not a git repository)
  - embedded exercise instructions
  - navigation of the exercises and automatically pull in tests
  - allow the student to:
    - "pull" the reference solution for an exercise
    - save the current state of an exercise
    - restore a previously saved state of an exercise
- create a tool that allows a course creator or maintainer to change exercises using **_git_**
  interactive rebasing when this makes sense

Over time, practical experience showed that there are other very useful use cases for the tooling.
That's what is described in the following section.

## Why CMT?

What do the following have in common?

- *Build and maintain a series of exercises for a training course*:
  - where each exercise builds on the previous one
  - the exercises history is tracked using **_git_**
  - the exercises can be converted into an artifact that gives students the
    possibility to navigate the exercises, save the current state of
    their work for each exercise, and, if needed, *pull* the solution
    for an exercise
- *Build and maintain a non-trivial sample application and*:
  - decompose it in bite-sized steps that can be grasped by users who
    are not yet comfortable with the used software libraries and/or
    frameworks
  - the application history is tracked using **_git_**
  - the application can be converted in an artifact that can be used
    to run the application at any of the intermediate steps in an
    easy manner
  - the application can be converted in an artifact that can be used
    to inspect the code changes between consecutive steps
- *Build and maintain code that will be used during a live-coding session*:
  - maintain a series of "checkpoints"
  - track the checkpoint history in **_git_**
  - provide for a safetynet by given a user the option to revert to a
    checkpoint that is known to _"work"_
  - save any changes made during the session for later retrieval

The answer to the question in the beginning of this section is that all 
of the above features are offered by the Course Management Tools (**CMT**).

## Overview

The Course Management Tools use a command line interface with the
following commands:

- **cmt-mainadm**: as the name suggest, this command is used for administration
  purposes, such as renumbering exercises, generating the root `build.sbt`
  build definition file, generate CMT test scripts, ...
- **cmt-studentify**: this command is used to generate an artifact that is suited
  for different purposes:
  - in a teaching context, it will be used by students to:
      - display course and exercise instructions
      - navigate between different exercises
      - pull in exercise specific tests
      - pull the reference solution for an exercise if needed
      - save the current state of an exercise
      - restore a previously saved exercise state
      - print the list of exercises and mark the current exercise
  - in live coding sessions, the exact same features used in a learning
    context allow one to:
      - quickly restore the code for a checkpoint by pulling the solution
        for it
      - save the current state of a modified checkpoint for later retrieval

- **cmt-linearize** & **cmt-delinearize**

`cmt-linearize` is used to generate an artifact, named the _linearized_ repository
that is used to edit the code in exercises using **_git_** interactive rebasing.
Once the edit process is finished, `cmt-delinearize` is used to reflect the
changes in the _linearized_ repository in the main exercise repository.

In general, the _linearized_ repository can be considered to be a scratch pad that is
discarded after the editing process is finished. However, it can used to inspect
the differences between consecutive exercises (this is because the linearized repo
is a **_git_** repository where each exercise is a commit).

The following picture shows the different respositories and flows in the management
of a course:

![CMT overview](https://i.imgur.com/5FzwpLa.png)

It is the _CMT Main Repository_ that contains the complete history of a course (or
demo/POC) repository.

The _CMT Main Repository_ is multi-project sbt build with one project per exercise.
