package cmt.client.config

import com.typesafe.config.Config
import sbt.io.syntax.{File, file}

final case class CoursesDirectory(value: File)
final case class CurrentCourse(value: File)

final case class CmtcConfig(coursesDirectory: CoursesDirectory, currentCourse: CurrentCourse)

object CmtcConfig {

  def fromTypesafeConfig(config: Config): CmtcConfig = {
    val coursesDirectory = CoursesDirectory(file(config.getString("cmtc.courses-directory")))
    val currentCourse = CurrentCourse(file(config.getString("cmtc.current-course")))
    CmtcConfig(coursesDirectory, currentCourse)
  }
}
