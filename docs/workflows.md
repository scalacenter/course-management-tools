---
id: workflows
title: Workflows
sidebar_label: Workflows
---

## Introduction 

In this section, we will describe typical CMT workfows used when building
and maintaining a training course.

We will use a sample main repository named `lunatech-beginner-quarkus-course-v2` that contains
sixteen exercises. Let's have a look at what is in the master repo.

```bash
$ cd simple-repo

$ ls
code                   course-management.conf

$ ls -l code
$ ls -l
total 0
drwxr-xr-x  11 ericloots  staff  352 May  4 19:41 exercise_001_initial_state
drwxr-xr-x  11 ericloots  staff  352 May  4 19:41 exercise_002_a_qute_hello_world
drwxr-xr-x  11 ericloots  staff  352 May  4 19:41 exercise_003_qute_products
drwxr-xr-x  11 ericloots  staff  352 May  4 19:41 exercise_004_even_qute_products
drwxr-xr-x  11 ericloots  staff  352 May  4 19:41 exercise_005_products_from_the_database
drwxr-xr-x  11 ericloots  staff  352 May  4 19:41 exercise_006_CDI_and_ArC
drwxr-xr-x  11 ericloots  staff  352 May  4 19:41 exercise_007_Convert_endpoints_to_JSON
drwxr-xr-x  11 ericloots  staff  352 May  4 19:41 exercise_008_Adding_REST_data_Panache
drwxr-xr-x  11 ericloots  staff  352 May  4 19:41 exercise_009_Hook_up_the_React_app
drwxr-xr-x  11 ericloots  staff  352 May  4 19:41 exercise_010_Validation_and_PUT
drwxr-xr-x  11 ericloots  staff  352 May  4 19:41 exercise_011_Going_Reactive
drwxr-xr-x  11 ericloots  staff  352 May  4 19:41 exercise_012_Reactive_search_endpoint
drwxr-xr-x  11 ericloots  staff  352 May  4 19:41 exercise_013_Listen_and_Notify
drwxr-xr-x  11 ericloots  staff  352 May  4 19:41 exercise_014_Internal_Channels
drwxr-xr-x  11 ericloots  staff  352 May  4 19:41 exercise_015_Connecting_to_Kafka
drwxr-xr-x  11 ericloots  staff  352 May  4 19:41 exercise_016_Dead_Letter_Queue_and_Stream_filtering
```

As explained in the [CMT approach](getting_started.md#the-cmt-approach) section,
CMT doesn't care about what build tool, if any, is used in the different exercises.
However, we do need to configure a couple of things to get the correct behaviour
for the student. The `course-management.conf` file in the root folder of the main
repo looks as follows:

```bash
$ cat course-management.conf
cmt {
  test-code-folders = [
    "src/test"
  ]

  read-me-files = [
    "README.md"
  ]

  cmt-studentified-dont-touch = [
    .idea
    .vscode
    .mvn
    target
    pom.xml
  ]
}
```

> Note: the configuration file shown above contains just three settings. It is useful to
note that these settings _all_ relate to functionality in a studentified artifact of the
main repo.

Let's have a look at each of these settings.

### Setting `test` folders

The first setting we find in the configuration file is `cmt.test-code-folders`. This setting is
a list that, in the case of `lunatech-beginner-quarkus-course-v2`, contains a single item `src/test`.

When a student moves between different exercises, CMT will pull in any code residing in the
folder(s) contained in this setting while leaving any other code unchanged.

### Setting `README` files

The setting `cmt.read-me-file` is a list of files (in this example a single file name `README.md`)
that are assumed to provide exercise specific information (such as exercise instructions).

As with the `test` setting, this file or files are pulled in when moving between exercises.

### Setting files and folders that shouldn't be touched

When a student executes one of the `cmtc` commands, for example to move to the next exercise or to
pull the solution for the current exercise, what happens is that the set of files that comprises
the exercise is manipulated. Files may be deleted, created, of the content of existing files may be
altered.

Without any extra measure, this file manipulation may interfere with the tooling or editors the
student utilises. For example, when using _Maven_ as build tool, a `.mvn` folder will be created
by _Maven_ and the content of that folder should not be touched by CMT. Similarly, when using say,
the IntelliJ IDE, a `.idea` folder is created who's content should also be left untouched by CMT.

So, this is where the `cmt.cmt-studentified-dont-touch` setting comes into play: it contains a list
of folders and files that `cmtc` should leave untouched when performing its job.

The `lunatech-beginner-quarkus-course-v2` project uses _Java_ as programming language and _Maven_
as build tool. As it is quite common to use either [_vscode_](https://code.visualstudio.com) or
the [_IntelliJ IDE_](https://www.jetbrains.com/idea/) we end up with the configuration shown above.

## Studentifying a CMT main repository

A _studentified_ artifact can be generated from a CMT main repository
by running the `cmta studentify` command:

```bash
$ cd lunatech-beginner-quarkus-course-v2 ; ls
code                   course-management.conf

$ cmta studentify -f . ~/tmp/stu
Studentifying /Users/ericloots/Trainingen/LBT/lunatech-beginner-quarkus-course-v2 to /Users/ericloots/tmp/stu
<elided>
Processed exercises:
  exercise_001_initial_state
  exercise_002_a_qute_hello_world
  exercise_003_qute_products
  exercise_004_even_qute_products
  exercise_005_products_from_the_database
  exercise_006_CDI_and_ArC
  exercise_007_Convert_endpoints_to_JSON
  exercise_008_Adding_REST_data_Panache
  exercise_009_Hook_up_the_React_app
  exercise_010_Validation_and_PUT
  exercise_011_Going_Reactive
  exercise_012_Reactive_search_endpoint
  exercise_013_Listen_and_Notify
  exercise_014_Internal_Channels
  exercise_015_Connecting_to_Kafka
  exercise_016_Dead_Letter_Queue_and_Stream_filtering

$ ls ~/tmp/stu
lunatech-beginner-quarkus-course-v2

$ ls ~/tmp/stu/lunatech-beginner-quarkus-course-v2
code

$ ls ~/tmp/stu/lunatech-beginner-quarkus-course-v2/code
EXERCISES.md       docker-compose.yml mvnw               pom.xml
README.md          materials          mvnw.cmd           src

```

As one can see, `cmta studentify` has created a folder `lunatech-beginner-quarkus-course-v2`
that contains a  sub-folder `code` that contains the code for the first exercise.

In summary, the process can be depicted as follows:

![studentify process](https://imgur.com/eEKgbye.png)

The _studentified_ artifact is self-contained (and can optionally be generated
as a **_git_** repository).

A student can "manipulate" the studentified repo using `cmtc` by passing the
appropriate sub-command. The available subcommands are summarised in the following
table:

![Studentified repo - commands](https://imgur.com/HueGxyg.png)

## Evolving the content of a CMT main repository

During the lifetime of a CMT main repository, the need will arise to change
its content. For example, one may need to:

- add an exercise at the end of the existing series of exercises
- insert a new exercise between two consecutive exercises
- change the title of an exercise
- change the code in an exercise and make the required changes
  to subsequent exercises

In general, for each of the above changes, there's an optimal way to
implement them. There are two approaches to applying changes:

- direct changes on the CMT main repository. For example, when the exercise
  instructions for a particular exercise need to be changed, this
  approach is optimal
- indirect changes via the so-called _linearize_/_delinearize_ process.
  This approach is recommended when code changes are applied in an
  exercise that is followed by one or more exercises: the "effect" of
  the changes needs to be applied to subsequent exercises. The generic
  approach when using **_git_** is to apply interactive rebasing.
  Obviously, there's no way we can do this on the CMT main repository
  and that's where `cmta linearize` and `cmta delinearize` come in.

A _linearized_ repo is a git repository in which each exercise in the CMT main
repo repository is "mapped" to a commit. We'll use a different main repository
named `lunatech-beginner-quarkus-course-v2` to illustrate the
_linearize_/_delinearize_ workflow.

The following diagrams depicts the _linearization_ process:

![Linearize process](https://imgur.com/qCMgsDk.png)

To illustrate the process, assume we run the following command to linearize
a CMT main repository:

```
$ cd lunatech-beginner-quarkus-course-v2; ls
README.md              code                   course-management.conf slides

$ cmta linearize . ~/tmp/lin
Linearizing /Users/ericloots/Trainingen/LBT/lunatech-beginner-quarkus-course-v2 to /Users/ericloots/tmp/lin
<elided>
Successfully linearized /Users/ericloots/Trainingen/LBT/lunatech-beginner-quarkus-course-v2
```

After a successful completion of this command, the _linearized_ repo will be
in a subfolder of `/Users/ericloots/tmp/lin` named `lunatech-beginner-quarkus-course-v2`.

We can verify a couple of things on the _linearized_ repo.

```
$ cd ~/tmp/lin/lunatech-beginner-quarkus-course-v2

$ git log --oneline
9aed004 (HEAD -> main) exercise_016_Dead_Letter_Queue_and_Stream_filtering
2259623 exercise_015_Connecting_to_Kafka
af38c66 exercise_014_Internal_Channels
05afccd exercise_013_Listen_and_Notify
9129021 exercise_012_Reactive_search_endpoint
229338a exercise_011_Going_Reactive
af2d53b exercise_010_Validation_and_PUT
76ee852 exercise_009_Hook_up_the_React_app
5ce44e9 exercise_008_Adding_REST_data_Panache
0544507 exercise_007_Convert_endpoints_to_JSON
537d732 exercise_006_CDI_and_ArC
15193f7 exercise_005_products_from_the_database
4087296 exercise_004_even_qute_products
a291630 exercise_003_qute_products
b51a7a9 exercise_002_a_qute_hello_world
6288f05 exercise_001_initial_state
```

We can observe that the last commit (HEAD) corresponds to the last exercise
on the main repository.

We can also inspect the differences between, say, exercises 12 and 13 as illustrated
here. Let's first see which files were changed between these exercises.

```
$ git diff --name-only HEAD~3 HEAD~2
code/README.md
code/pom.xml
code/src/main/java/com/lunatech/training/quarkus/PriceUpdate.java
code/src/main/java/com/lunatech/training/quarkus/PriceUpdateStreams.java
code/src/main/java/com/lunatech/training/quarkus/PriceUpdatesResource.java
```

It is to be expected that the README file has changed. Let's see what changed in
file `PriceUpdate.java`.

```
$ git diff HEAD~3 HEAD~2 code/src/main/java/com/lunatech/training/quarkus/PriceUpdate.java
diff --git a/code/src/main/java/com/lunatech/training/quarkus/PriceUpdate.java b/code/src/main/java/com/lunatech/training/quarkus/PriceUpdate.java
new file mode 100644
index 0000000..1d466cd
--- /dev/null
+++ b/code/src/main/java/com/lunatech/training/quarkus/PriceUpdate.java
@@ -0,0 +1,19 @@
+package com.lunatech.training.quarkus;
+
+import java.math.BigDecimal;
+
+public class PriceUpdate {
+    public Long productId;
+    public BigDecimal price;
+
+    public PriceUpdate(){}
+
+    public PriceUpdate(Long productId, BigDecimal price) {
+        this.productId = productId;
+        this.price = price;
+    }
+
+    public String toString() {
+        return "Price(" + productId + ", " + price.toString() + ")";
+    }
+}
```

This illustrates another use case of a _linearized_ repository: figuring out
what changes between exercises.

Let's return to the topic of this section: editing the content of a CMT
main repository. Typically, when making a change to the code in a particular
exercise, one wants to let the effect of such a change ripple through all
subsequent exercises. In some cases, it may be relatively straightforward to
apply such changes directly on the CMT main repository. However, in general,
doing so is prone to errors: necessary changes may be overlooked and annoying
minor differences in formatting may slip in. Therefore, it is recommended to
use **_git_** interactive rebasing instead as depicted in the following
diagram.

![Interactive rebasing process](https://imgur.com/cac7Ls4.png)

When making large changes, it is recommended to split these in a series
of smaller steps. This may simplify the process of merge conflict resolution
if these arise during the completion of the interactive rebasing process.

Note that during the interactive rebasing process, the code can be loaded in
an IDE to assist in the rebasing process and to test the changes.

Once the refactoring of the code in the _linearized_ repository is complete,
the applied changes need to be reflected in the CMT main repository. This is
done via the _delinearization_ process as depicted in the following diagram:

![Delinearize process](https://imgur.com/t0Wahtt.png)

In our sample scenario, we run the following `cmta delinearize` command to perform
the _delinearization_:

```
$ cd lunatech-beginner-quarkus-course-v2; ls
README.md              code                   course-management.conf slides

$ cmta delinearize . ~/tmp/lin
De-linearizing /Users/ericloots/Trainingen/LBT/lunatech-beginner-quarkus-course-v2 to /Users/ericloots/tmp/lin
<elided>
Successfully delinearised /Users/ericloots/tmp/lin

```

Running the `git status` command on the CMT master repository will show _all_
the files that were changed in the editing process.

With a refactoring cycle completed, we can repeat the process. As long as we
don't make any direct edits on any of the exercises in the CMT main repo,
we can repeat the **_git_** interactive rebasing process/_delinearization_
as many times as needed.

![Repeat interactive rebasing process](https://imgur.com/3rNbSqd.png)

Between iterations, we can also do the following:

- "checkpoint" what we already have on the CMT master repository by committing
  it. This doesn't hurt and if needed this can be undone easily.
- run the tests on all exercises. As the tooling is oblivious to the build tool
  and the testing tools used in the exercises, there's no pre-baked solution
  to automate this process.  Imagine that a CMT project uses Maven as build tool.
  Chances are that we can test any exercise by running `mvn test`. It would be
  trivial to build a small test script to automate the testing. The script would
  loop over each of the exercise folders, cd into them one by one and execute
  the tests.

## Inserting, deleting, and renumbering exercises

`cmta` is your friend for these kind of tasks. See the following
sections in the reference chapter:

- [inserting an exercise](reference-cmta.md#cmta-dib)
- [renumbering exercises](reference-cmta.md#cmta-renum)

Finally, deleting an exercise is as simple as deleting the corresponding exercise
folder.
