---
id: FAQ
title: Frequently asked questions
sidebar_label: CMT FAQs
---

## What programming languages are supported by CMT?

The tooling is programming language agnostic. In other words, you can choose any language you like, or use multiple languages in a course. In fact, you may even have exercises that
don't contain any code at all.

## What build tools are supported by CMT?

Again, CMT is build tool agnostic. As a consequence, you may use different build tools (sbt, mill, graddle, maven, ...) between exercises. You may also have exercises that don't use a
build tool. In fact, you could use CMT to build a course on build tools, starting from an
initial state where no build tool is present.

## What do I need in terms of other tools, binaries to work with CMT?

This depends on whether you'll be using it as a course author or a course user (ie. a student).

For a student, you need to have the `cmtc` binary installed. Depending on whether the course
you're working on has been initialised as a `git` repository, you may need to have `git` 
installed on your computer.

As a course author, you need to have `git` and the `cmta` binary installed.
