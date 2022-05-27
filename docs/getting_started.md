---
id: getting_started
title: Getting Started
sidebar_label: Introduction
---

## Why CMT (Course Management Tools) ?

![The premise](https://i.imgur.com/52a0JhX.png)

The Course Management Tools (CMT) simplify the process of setting up and maintaining programming exercises for training courses.

It starts from the premise that a complex problem can be broken down into smaller pieces that can be digested easily and learned by an individual. For a given domain, a course may build a service in small steps in a series of exercises where each exercise builds on the content of the exercise preceding it.

One particular challenge that one has to deal with when building and maintaining training exercises is that there are two dimensions to the problem: we've already touched on the first one, the evolution of the exercises through consecutive exercises in a training course. The second dimension comes into play when the course as a whole is versioned. Experience has shown that one needs tooling that deals with these two [orthogonal] dimensions. CMT fits the bill as will be explained in the sections that follow.

Before describing the principle behind the tooling, we will start with having a look at a possible approach which only deals with one dimension of the problem and which we will call _"The Naive Approach"_. 


### The naive approach to CMT

When faced with the problem of maintaining a series of exercises, one approach applied by people who are familiar with version control software such as git, is to maintain the exercises as a series of commits in a git repository.

Imagine we have a 16-exercise course —let's label it as version 1.0— in a git repository as depicted here:

![The Naive Approach 1](https://imgur.com/1aNEEgu.png)

Nice, but suppose we want now want to create version 1.1 of the course in which exercise 5 is changed. Applying the change can be done with git's interactive rebasing. The change in exercise 5 will ripple through the consecutive exercises (potentially resulting in some merge conflicts which we will have to resolve). After the edit process is finished, we end up with a git repository with a rewritten history. The before and after situation is as follows:

![The Naive Approach 2](https://imgur.com/b8eJUkC.png)

So, we've achieved our goal, which was to change exercise 5, however, there are a number of problems with this approach and a number of questions that are rather difficult to answer...

- We've had to rewrite the git history. As a result, if the repo is on Github, we'll have to force push the change, effectively destroying version 1.0. At best, we can, before force pushing version 1.0, 'park' version 1.0 on a dedicated branch. Not very useful.
- Any open Pull Request on version 1.0 becomes detached and of little use on the current 1.1 version of the course. This is bad for collaboration.
- Having to use git interactive basing for _all_ updating tasks can be awkward in some cases. For example, each exercise may have a README file, and all these files in a course are uncorrelated. So, updating a README file in one exercise will result in a merge conflict in the rebasing process. Not a big deal, but rather inconvenient.
- What do we give users of the training course? This question actually leads to a series of other questions:
  - Do we want to hand the students the solution for each exercise? The answer is probably no in most cases.
  - After having completed an exercise, students should be able to move to the next exercise by "pulling" in the exercise instructions and possibly the tests for that exercise. How can we do that?
  - In the case of instructor-led courses, one will often run into the situation where some students haven't managed to complete an exercise in the allotted time. In that case, these students should be able to save the state of the exercise as-is and get the reference solution so that they can continue with the remainder of the exercises.
  - Students should be able to restore a previously saved state of an exercise.

In summary, the naive approach clearly is just a part of a larger puzzle. Let's now have a look at the CMT approach and see how it tackles the issues we highlighted.

## The CMT approach

A key characteristic of the CMT approach is that it adds the possibility to track the version of a course in its entirety. In other words, it allows tracking of changes in the two dimensions we referred to in the introduction of this chapter.

It makes this tracking in the second dimension possible by taking an unusual first step: it introduces the so-called _main repository_ from which, by means of one of the CMT tools, a number of artefacts can be derived for specific use cases.

In essence, the _main repository_ has a simple structure. It is a git repository, containing a CMT configuration file in the root folder, and a folder that holds all exercises in the course. In turn, the exercises are folders, one per exercise.

The following rules apply to the exercise folders:

- Each exercise folder holds the complete state of the exercise. As such, CMT doesn't really care about its structure. _CMT is neither concerned about build tools, nor programming languages used in exercises_.
- Exercise folder names have to follow a simple naming convention: `prefix_ddd_description`, where `ddd` is a 3 digit exercise number.
- Versioning of a course as a whole is done using regular git commits.
- Exercises can be edited by modifying individual files directly without having to resort to interactive rebasing. This is very convenient in the case of exercise README files.

Of course, the attentive reader will note that the ability to change exercise content via interactive rebasing as described in the naive approach is still highly desirable, and they are absolutely right.

Also, if we maintain a course in a _"main repository"_, then what do we give to the student?

Let's have a look at how CMT tackles the issues and questions raised above one by one in the following sections.

## Changing the content via interactive rebasing

Obviously, using interactive rebasing to change exercises can't be used directly on the _main repository_. For that reason, the tooling can create what is called a _linearised_ version of the _main repository_ in which each exercise is a commit in a git repository. Interactive rebasing can be used to change one or multiple exercises. After successfully completing the rebase process, the changes can be reflected back into the _main repository_ by a process called _delinearisation_.

After completing this editing process, the _linearised_ artefact can be discarded.

The following illustration summarizes the interactive rebasing process in the context of CMT.

![Lin-Delin process](https://imgur.com/tAdPsbL.png)

> Note: a _linearised_ repo has other use cases. For example, it can be utilised to inspect the changes between subsequent exercises. In some cases, this can be used as part of the educational experience because it allows students to quickly inspect the aforementioned changes.

The [workflows section](workflows.md) dives into more detail on the linearize/delinearize workflow.

## Generating and using the studentified artifact

The ultimate goal of CMT is to generate an artifact that can be distributed to students so that
they can run through the series of exercises. With the help of `cmtc`, the CMT client program,
students can:

- move to the next exercise
- 'pull' the solution for the current exercise
- save the state of the current exercise for later retrieval
- restore the state of a previously saved exercise state

