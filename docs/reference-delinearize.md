---
id: reference-delinearize
title: cmt-delinearize Command Reference
sidebar_label: cmt-delinearize
---

# Command Invocation

```
Usage: cmt-delinearize [options] mainRepo linearRepo

  mainRepo                 base folder holding main course repository
  linearRepo               linearized version repo
  -cfg, --config-file <value>
                           configuration file
  --help                   Prints the usage text
  -v, --version            Prints the version info
```

# Utilisation

After having gone through one or more rounds of **git** interactive rebasing
on a linearized repository, we can apply these changes on the main
repository via a process of de-linearization as depicted here: 

![Delinearize process](https://i.imgur.com/GpE8jbS.png)

> IMPORTANT NOTE: `cmt-delinearize` will only write modifications applied
> in the `exercises` project. Any other changes will be discarded. If you
> want to change any other file, apply these changes directly on the main
> course repo.

