package coursemgmt

import sbt.io.IO as sbtio
import sbt.io.syntax.*

import scala.util.Using.Releasable

object Releasables:
  final case class TmpDir(directory: File)

  object TmpDir:
    def apply(): TmpDir = TmpDir(sbtio.createTemporaryDirectory)

  given Releasable[TmpDir] with {
    def release(resource: TmpDir): Unit = sbtio.delete(resource.directory)
  }
