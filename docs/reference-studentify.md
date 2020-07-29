---
id: reference-cmtstudentify
title: cmt-studentify Command Reference
sidebar_label: cmt-studentify
---

## Command Invocation

```
Usage: studentify [options] mainRepo out

  mainRepo                 base folder holding main course repository
  out                      base folder for student repo
  -mjvm, --multi-jvm       generate multi-jvm build file
  -fe, --first-exercise <value>
                           name of first exercise to output
  -le, --last-exercise <value>
                           name of last exercise to output
  -sfe, --selected-first-exercise <value>
                           name of initial exercise on start
  -cfg, --config-file <value>
                           configuration file
  -g, --git                initialise studentified repository as a git repository
  -dot, --dotty            studentified repository is a Dotty project
  -nar, --no-auto-reload-sbt
                           no automatic reload on build definition change
  --help                   Prints the usage text
  -v, --version            Prints the version info
```

# Utilisation

![studentify process](https://i.imgur.com/RFTpBYR.png)
