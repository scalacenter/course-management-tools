---
id: getting_started
title: Getting Started
sidebar_label: Introduction
---

# Introduction

What do the following have in common?

- *Build and maintain a series of exercises for a training course*:
  - where each exercise builds on the previous one
  - the exercise history is tracked using **git**
  - the exercises can be converted in a artifact that gives students the
    possibility to navigate the exercises, save the current state of
    their work for each exercise, and, if needed, *pull* the solution
    for an exercise
- *Build and maintain a non-trivial sample application and*:
  - decompose it in bite-sized steps that can be grasped by users who
    are not yet comfortable with the used software libraries and/or
    frameworks
  - the application history is tracked using **git**
  - the application can be converted in an artifact that can be used
    to run the application at any of the intermediate steps in an
    easy manner
  - the application can be converted in an artifact that can be used
    to inspect the code changes between consecutive steps
- *Build and maintain code that will be used during a live-coding session*:
  - maintain a series of "checkpoints"
  - track the checkpoint history in **git**
  - provide for a safetynet by allowing to revert to a checkpoint
  - save any changes made during the session for later retrieval

The answer to the question in the beginning of this section is that all 
of the above features are offered by the Course Management Tools (**CMT**).

The Course Management Tools use a command line interface with the
following commands:

- **cmt-admin**: as the name suggest, this command is used for administration
  purposes, such as renumbering exercises, generating the root `build.sbt`
  build definition file
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
    context allow one to
      - quickly restore the code to for any checkpoint by pulling the solution
        for it
      - save the current state of a modified checkpoint for later retrieval

- **cmt-linearize** & **cmt-delinearize**

**cmt-linearize** is used to generate an artifact, named the *linearized repo*
that is used to edit the code in exercises using **git** interactive rebasing.
Once the edit process is finished, **cmt-delinearize** is used to reflect the
changes in the *linearized repo* in the main exercise repository.

In general, the _linearized repo_ can be considered to be a scratch pad that is
discarded after the editing process is finished. However, it can used to inspect
the differences between consecutive exercises (this is because the linearized repo
is a **git** repository where each exercise is a commit).