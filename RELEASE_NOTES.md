# Release Notes

## Introduction

Release notes added on January 11, 2017

## Releases

### 2017-04-10 Studentify version 2.0

- Generalized `studentify`:
  - Add configuration option for test code folders
    - configuration file `.student-settings.conf` in course main contains a settings line:
  - `studentify` will now abort if the course main source folder contains uncommited changes
  - `studentify` will carry over a `.sbtopts` config file from course main repo to the generated student repo
  - `pullSolution` will now pull in _**all**_ code for an exercise instead of just the `src` folder  

### 2017-01-11 Studentify version 1.4

- Addition of new commands:
    - `listExercises`: print a list of the exercises based on information in exercise README.md files
    - `gotoExerciseNr <ddd>`: jump to an arbitrary exercise in a course by specifying the exercise number
- Reorganisation of main repo project layout:
    - Previous layout had multiple, interdependent .sbt build files in the project root folder. This introduced race conditions that could lead to failure when deployed on certain OSs
    - Removal of some commands that were available in the main repo:
        - `nextExercise`, `prevExercise` have been removed as the sbt native `project` and `projects` give comparable functionality
    - The hidden solution folder `.cue` now contains one zip archive per exercise instead of the _raw_ solution folder. This solves an issue with IntelliJ trying to refactor code in the `.cue` folder.
    