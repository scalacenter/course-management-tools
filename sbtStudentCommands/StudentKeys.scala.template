package sbtstudent

/**
 * Copyright © 2014 - 2017 Lightbend, Inc. All rights reserved. [http://www.lightbend.com]
 */

import sbt._
import sbt.complete.Parser

object StudentKeys {
  val bookmarkKeyName = "bookmark"
  val mapPrevKeyName = "map-prev"
  val mapNextKeyName = "map-next"
  val allExercisesKeyName = "all-exercises"

  val bookmark: AttributeKey[File] = AttributeKey[File](bookmarkKeyName)
  val mapPrev: AttributeKey[Map[String, String]] = AttributeKey[Map[String, String]](mapPrevKeyName)
  val mapNext: AttributeKey[Map[String, String]] = AttributeKey[Map[String, String]](mapNextKeyName)
  val allExercises: AttributeKey[Parser[(Seq[Char], String)]] = AttributeKey[Parser[(Seq[Char], String)]](allExercisesKeyName)
}
