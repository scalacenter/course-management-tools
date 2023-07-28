---
id: install
title: Installing Course Management Tools
sidebar_label: Installation
---

## Supported Operating Systems and Prerequisites

### Supported OS

The Course Management Tools have been tested on the following Operating Systems:

- MacOS 10.14 or higher
- Ubuntu 18.04.4 LTS (tested via CI/CD)
- Windows 10



### Prerequisites

Make sure to have a [recent version of **_git_**](https://git-scm.com/downloads) installed on your system.

## Installation

The preferred way to install the Course Management Tools is to download
the binaries from [the latest release](https://github.com/lunatech-labs/course-management-tools/releases).

The installation procedure is rather straightforward:

- download the `course-management-tools.zip` file from the [release page](https://github.com/lunatech-labs/course-management-tools/releases)
- unzip the content in a folder
- update your PATH to include the `course-management-tools/bin` folder in
  the folder in which you unzipped the downloaded zip file
- You now have access to the two CMT commands:
  - `cmta` admin command (studentify, linearize, delinearize, ...) to manipulate CMT main repositories
  - `cmtc` client command to manipulate studentified artifacts

> Note: the plan is to publish native binaries of `cmta` and `cmtc` for MacOS, *nix, and Windows. Also, the goal is to publish CMT binary artifacts on Maven Central which should enable installation of the CMT binaries using [Coursier](https://github.com/coursier/coursier).
