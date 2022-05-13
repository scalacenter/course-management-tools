---
id: v1-reference-delinearize
title: cmt-delinearize Command Reference
sidebar_label: cmt-delinearize
---

## Command Invocation

```
Usage: cmt-delinearize [options] mainRepo linearRepo

  mainRepo                 base folder holding main course repository
  linearRepo               linearized version repo
  -cfg, --config-file <value>
                           configuration file
  --help                   Prints the usage text
  -v, --version            Prints the version info
```

## Delinearizing a linearized repository

After having gone through one or more rounds of **_git_** interactive rebasing
on a linearized repository, we can apply these changes on the main
repository via a process of de-linearization as depicted here: 

![Delinearize process](https://i.imgur.com/BYlAaPh.png)

## `cmt-delinearize` configuration settings

A number of configuration settings can be used to influence the behaviour
of `cmt-delinearize`:

- [exercise-project-prefix](v1-reference-config.md#exercise-project-prefix)
- [studentified-base-folder](v1-reference-config.md#studentified-base-folder)
- [relative-source-folder](v1-reference-config.md#relative-source-folder)

> IMPORTANT NOTE: `cmt-delinearize` will only write modifications applied
> in the `exercises` project. Any other changes will be discarded. If you
> want to change any other file, apply these changes directly on the main
> course repo.

