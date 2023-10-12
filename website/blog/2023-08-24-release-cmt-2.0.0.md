---
title: Releasing CMT 2.0
author: Eric Loots
authorURL: http://github.com/eloots
---

## Releasing Course Management Tools Version 2

We're proud to announce the release of a new major version of the Course Management
Tools (CMT)!

Whereas version 1.0 had rather restrictive requirements on the structure of the main
repository, the new version is agnostic to the choice of programming language or
build tool(s) used in a course.

### Notable changes in CMT 2.0

- Simple Installation using Coursier
- Native image binaries for MacOS, Linux, and Windows (10 & 11)
- `cmtc` client interface (primarily used by students)
- `cmta` administrator interface (used by course creators and maintainers)
- Build tool and programming language agnostic


### How To Install

Installation is a breeze using Coursier: you can find the instructions [here](https://scalacenter.github.io/course-management-tools/docs/install)

### Contributors

`git shortlog -sn --no-merges 75f058d8..2add26b7`

```
   129  Eric Loots
    23  Trevor Burton-McCreadie
     6  Chris Kipp
     4  Willem Jan Glerum
     1  Rui Balau
```
