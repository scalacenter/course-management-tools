package com.lightbend.studentify

/**
  * Copyright © 2014, 2015, 2016 Lightbend, Inc. All rights reserved. [http://www.lightbend.com]
  */

import com.typesafe.config.ConfigFactory

object Settings {

  val config = ConfigFactory.load()

  val solutionsFolder = config.getString("studentify.exercises.solution-folder")

  val studentBaseProject = config.getString("studentify.exercises.student-base-project")

}
