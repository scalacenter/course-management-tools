---
id: reference-intro
title: Introduction
sidebar_label: Introduction
---

In this section you will find the CMT reference documentation. The
behaviour of the CMT commands can be controlled in two ways:

Each CMT command has a number of:

- command line options that control its function
  and behaviour
- settings that are controlled via a 
  configuration file. A complete overview of all the available
  settings can be found in the
  [CMT configuration section](reference-config.md). If there's
  a configuration file named `course-management.conf` in the
  main course repository's root folder, CMT command will apply
  the settings provided in that file. One can also pass an
  alternative configuration file to a CMT command with the `-cfg`
  option. The path of the passed in configuration file is
  relative to the main course repository's root folder.

### Note

All Course Management Tools commands take as first argument
the _absolute_ path to the **_root_** folder of the **git** main
course repository. In examples in this section, we will use
_<main_repo>_ as a placeholder.

`cmt-delinearize` takes as second argument the _absolute_ path to
the folder containing the delinearized artifact. In examples in
this section, we will use _<linearized_repo>_ as a placeholder.



