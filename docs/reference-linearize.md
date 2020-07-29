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

# Utilisation

![Linearize process](https://i.imgur.com/k5HRSDw.png)

![Interactive rebasing process](https://i.imgur.com/ng0s9VU.png)
