package com.lunatech.cmt

import sbt.io.syntax.{File, file}

object Domain {

  final case class StudentifiedRepo(value: File)
  object StudentifiedRepo:
    val default: StudentifiedRepo = StudentifiedRepo(file(".").getAbsoluteFile.getParentFile)

  sealed trait InstallationSource
  object InstallationSource:
    final case class LocalDirectory(value: File) extends InstallationSource
    final case class ZipFile(value: File) extends InstallationSource
    final case class GithubProject(organisation: String, project: String) extends InstallationSource {
      val displayName = s"$organisation/$project"
    }
  end InstallationSource
}
