package sbtstudent

/**
  * Copyright © 2014 - 2017 Lightbend, Inc. All rights reserved. [http://www.lightbend.com]
  */

import sbt._

object Zip {
  def withZipFile(state: State, exercise: String)(transformState: () => State): State = {
    val cueFolder = new sbt.File(new sbt.File(Project.extract(state).structure.root), s".cue")
    val srcZip = new sbt.File(cueFolder, s"${exercise}.zip")
    val unzippedSrc = new sbt.File(cueFolder, s"${exercise}")
    sbt.IO.unzip(srcZip, cueFolder)
    val newState = transformState()
    sbt.IO.delete(unzippedSrc)
    newState
  }
}
