package cmt.support

import sbt.io.IO
import sbt.io.syntax.File
import org.scalatest.BeforeAndAfterEach
import org.scalatest.Suite

trait CMTSupport extends BeforeAndAfterEach { this: Suite =>

  override def beforeEach(): Unit = {
    println("creating temp directory")
  }

  override def afterEach(): Unit = {
    println("deleting temp directory")
  }
}
