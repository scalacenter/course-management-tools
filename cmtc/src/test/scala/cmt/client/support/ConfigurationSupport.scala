package cmt.client.support

import cmt.client.Domain.StudentifiedRepo
import cmt.client.{Configuration, CoursesDirectory, CurrentCourse}
import cmt.client.Configuration.CurrentCourseToken
import org.scalatest.BeforeAndAfterEach
import org.scalatest.wordspec.AnyWordSpecLike
import sbt.io.syntax.*

trait ConfigurationSupport {

  protected def createConfiguration(baseDir: File): Configuration = {
    val coursesDirectory = CoursesDirectory(baseDir / "courses")
    val currentCourse = CurrentCourse(StudentifiedRepo(baseDir / CurrentCourseToken))
    Configuration(coursesDirectory, currentCourse)
  }
}
