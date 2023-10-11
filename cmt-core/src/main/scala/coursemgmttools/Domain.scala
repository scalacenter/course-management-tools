package coursemgmttools

import sbt.io.syntax.{File, file}

object Domain {

  final case class StudentifiedRepo(value: File)
  object StudentifiedRepo:
    val default: StudentifiedRepo = StudentifiedRepo(file(".").getAbsoluteFile.getParentFile)

  sealed trait InstallationSource
  object InstallationSource:
    final case class LocalDirectory(value: File) extends InstallationSource
    final case class ZipFile(value: File) extends InstallationSource
    final case class GithubProject(organisation: String, project: String, tag: Option[String])
        extends InstallationSource {
      val displayName: String =
        if tag.isEmpty then s"$organisation/$project" else s"$organisation/$project/${tag.getOrElse("")}"
    }
  end InstallationSource
}
