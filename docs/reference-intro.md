---
id: reference-intro
title: Introduction
sidebar_label: Introduction
---

In this section you will find the CMT reference documentation.

There are three types of CMT repositories:

- The main repository which is the only source of truth. It contains
  all exercises as a series of folders.
- The so-called _studentified_ repository: an artifact derived from
  the main repository.
- To so-called _linearized_ repository: an artifact derived from the
  main repository.

As far as the tooling is concerned, there are two commands,[`cmta`](reference-cmta.md) and
[`cmtc`](reference-cmtc.md).

[`cmta`](reference-cmta.md), the CMT admin command, operates on a main
repository and is used, among other things, to generate the _studentified_
repository or the _linearized_ repository. It can also used to perform the
inverse of the linearization of a main repo via a process called
_delinearization_.

Note that, in general, a _linearized_ repository is an artifact that
can effectively be considered to be a scratch pad and as such, it is
discarded after having served its purpose.

[`cmtc`](reference-cmtc.md), the CMT client command, operates on a
studentified repository. It is used to "manipulate" the state of the
latter. Examples of this are navigating between exercises, pulling one
or more files from an exercise solution into the current exercise workpace,
etc.
