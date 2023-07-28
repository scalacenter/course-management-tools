---
id: v1-install
title: Installing Course Management Tools (Version 1)
sidebar_label: Installation
---

## Supported Operating Systems and Prerequisites

> NOTE: You're browsing an older version of the tooling. You can find the docs of the current version [here](../install.md).

### Supported OS

The Course Management Tools have been tested on the following Operating Systems:

- MacOS 10.14 or higher
- Ubuntu 18.04.4 LTS (tested via CI/CD)
- Windows 10 with WSL2 (not tested but likely to work)

### Prerequisites

Make sure to have a [recent version of **_git_**](https://git-scm.com/downloads) installed on your system.

## Installation

The preferred way to install the Course Management Tools is to download
the binaries from [the latest (version 1) release](https://github.com/lunatech-labs/course-management-tools/releases/tag/1.0.3).

The installation procedure is rather straightforward:

- download the `course-management-tools.zip` file from the [release page](https://github.com/lunatech-labs/course-management-tools/releases)
- unzip the content in a folder
- update your PATH to include the `course-management-tools/bin` folder in
  the folder in which you unzipped the downloaded zip file
- You now have access to the following CMT commands:
  - `cmt-mainadm`
  - `cmt-studentify`
  - `cmt-linearize`
  - `cmt-delinearize`

If you're starting with the Course Management Tools, you're now ready to create a first
project. The easiest way to do this is to start from one of the available CMT master
repository templates. How this works is explained in the
[next chapter](v1-your-first-project.md). 
