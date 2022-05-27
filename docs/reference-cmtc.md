---
id: reference-cmtc
title: cmtc Command Reference
sidebar_label: cmtc
---

## Command Summary

`cmtc` takes a command as the first parameters and it operates on a studentified
CMT main repository.

The available commands and their function is as follows:

- [`list-exercises`](#cmtc-list-exercises): list exercises
- [`next-exercise`](#cmtc-next-exercise): go to the exercise after the current one
- [`previous-exercise`](#cmtc-previous-exercise): go to the exercise before the current one
- [`pull-solution`](#cmtc-pull-solution): pull-in the complete reference solution for the current exercise
- [`pull-template`](#cmtc-pull-template)    : pull-in a specific file or a complete folder from the reference solution
- [`goto-exercise`](#cmtc-goto-exercise): go to a specific exercise in the exercise series
- [`goto-first-exercise`](#cmtc-goto-first-exercise): go to the first exercise in the exercise series
- [`save-state`](#cmtc-save-state): save the state of th current exercise
- [`list-saved-states`](#cmtc-list-saved-states) : list all previously saved exercise states
- [`restore-state`](#cmtc-restore-state) : restore a previously saved state

The remainder of this section describes these commands in further detail.

### cmtc list-exercises

#### Synopsys

`cmtc list-exercises <studentified repo parent folder>`

#### Description

Generate a list of all exercises in the exercise series. A `*` is used to indicate
the exercise that is currently active. For example:

```bash
$ cmtc list-exercises ~/tmp/stu/lunatech-beginner-quarkus-course-v2
  1.      exercise_001_initial_state
  2.      exercise_002_a_qute_hello_world
  3.  *   exercise_003_qute_products
  4.      exercise_004_even_qute_products
  5.      exercise_005_products_from_the_database
  6.      exercise_006_CDI_and_ArC
  7.      exercise_007_Convert_endpoints_to_JSON
  8.      exercise_008_Adding_REST_data_Panache
  9.      exercise_009_Hook_up_the_React_app
 10.      exercise_010_Validation_and_PUT
 11.      exercise_011_Going_Reactive
 12.      exercise_012_Reactive_search_endpoint
 13.      exercise_013_Listen_and_Notify
 14.      exercise_014_Internal_Channels
 15.      exercise_015_Connecting_to_Kafka
 16.      exercise_016_Dead_Letter_Queue_and_Stream_filtering
```

This output shows that there are 16 exercises in the repo. It also show their
corresponding exercise IDs. We also see that the current active exercise is
`exercise_003_qute_products`.

### cmtc next-exercise

#### Synopsys

`cmtc next-exercise <studentified repo parent folder>`

#### Description

Move to the next exercise. This command will pull in the test and README files for the
next exercise. Which files are pulled in is defined in settings
[`cmt.test-code-folders`](reference-config.md#test-code-folders) and
[`cmt.read-me-files`](reference-config.md#read-me-files). All other files are left as-is.
This command will be part of the normal workflow used by someone solving all exercises in-sequence:
After finishing an exercise successfully (by making all tests pass), the student executes
the `cmtc next-exercise` command, which pulls in the instructions and tests for the next
exercise and the exercise solving process can continue.

> Warning: this command will overwrite any modification that a student has made to tests.
> Any custom tests have to be saved in a location that is not part of the configured
> set of test code folders (see setting [`cmt.test-code-folders`](reference-config.md#test-code-folders))

### cmtc previous-exercise

#### Synopsys

`cmtc previous-exercise <studentified repo parent folder>`

#### Description

Move to the previous exercise. This command will pull in the test and README files for the
previous exercise. Which files are pulled in is defined in settings
[`cmt.test-code-folders`](reference-config.md#test-code-folders) and
[`cmt.read-me-files`](reference-config.md#read-me-files). All other files are left as-is.

### cmtc pull-solution

#### Synopsys

`cmtc pull-solution <studentified repo parent folder>`

#### Description

The main use case for this command is to help students sync-up while participating in
instructor-led courses: if a students gets stuck or runs out of time while doing an exercise,
they can catch-up by pulling the solution for the current exercise. In such a situation, most
students will want to save the current state of their work _before_ pulling the solution.
They can do so by executing the `cmtc save-state` command before executing `cmtc pull-solution`.
Any saved state can be restored later by means of the `cmtc restore-state` command.

`cmtc pull-solution` will pull _all_ files for the current exercise. As a result, all code present
in the source code folder will be overwritten. 

### cmtc pull-template

#### Synopsys

`cmtc pull-template <template file or folder> <studentified repo parent folder>`

#### Description

This command allows a student to selectively pull a part of the solution for the current
exercise.

It is useful in the case where an exercise requires supporting code or files (data, ...)
that is too complex to have the students create themselves or that are not really relevant
in the context of what the course aims to teach.

If the template is a single file, that file will be pulled from the reference solution.
If the template is a folder, the folder and its content will be pulled recursively.

### cmtc goto-exercise

#### Synopsys

`cmtc goto-exercise <exercise-id> <studentified repo parent folder>`

#### Description

Go to a specific exercise in the current exercise series. Test code and read me files
for this exercise willbe pulled-in from the reference solution. Any other code or files
in the exercise workspace will be left as-is. In general, this has the effect that the
current code and the pulled-in tests are uncorrelated and running the tests will probably
generate [compilation] errors. Hence, in a typical scenario, running this command is
usually followed by running the `cmtc pull-solution` command to reset the state of the
exercise to reference solution.

### cmtc goto-first-exercise

#### Synopsys

`cmtc goto-first-exercise <studentified repo parent folder>`

#### Description

Go to the first exercise in the current exercise series. Test code and read me files
for the first exercise will be pulled-in from the reference solution. Any other code or files
in the exercise workspace will be left as-is. In general, this has the effect that the
current code and the pulled-in tests are uncorrelated and running the tests will probably
generate [compilation] errors. As with the `cmtc goto-exercise` command, running this command
will usually be followed by running a `cmtc pull-solution` command.

### cmtc save-state

#### Synopsys

`cmtc save-state <studentified repo parent folder>`

#### Description

Saves the complete state of the current exercise for later retrieval. For more info on
the use-case for this command, read the section of [`cmtc pull-solution`](#cmtc-pull-solution).

> Note: only _one_ state can be saved for a given exercise: if the command is executed it will
> overwrite a previously saved state for that exercise

### cmtc list-saved-states

#### Synopsys

`cmtc list-saved-states <studentified repo parent folder>`

#### Description

Print a list of exercise IDs for all saved states. For example:

```bash
$ cmtc list-saved-states .
Saved states available for exercises:

   exercise_003_qute_products
   exercise_011_Going_Reactive
```

The exercise IDs can be used when one wants to restore a particular exercise's state.

### cmtc restore-state

#### Synopsys

`cmtc restore-state <exercise-id> <studentified repo parent folder>`

#### Description

Restore the state of a previously saved exercise. For more info on
the use-case for this command, read the section of [`cmtc pull-solution`](#cmtc-pull-solution).