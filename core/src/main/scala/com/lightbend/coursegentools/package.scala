package com.lightbend

/**
  * Copyright © 2016 Lightbend, Inc
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *
  * NO COMMERCIAL SUPPORT OR ANY OTHER FORM OF SUPPORT IS OFFERED ON
  * THIS SOFTWARE BY LIGHTBEND, Inc.
  *
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{ Files, Paths }

import scala.util.matching.Regex

package object coursegentools {

  def toConsoleRed(msg: String): String = Console.RED + msg + Console.RESET
  def toConsoleGreen(msg: String): String = Console.GREEN + msg + Console.RESET
  def toConsoleCyan(msg: String): String = Console.CYAN + msg + Console.RESET

  def printError(msg: String)(implicit eofe: ExitOnFirstError): Unit = {
    println(toConsoleRed(msg))
    if (eofe.exitOnFirstError) System.exit(-1)
  }

  def printNotification(msg: String): Unit =
    println(toConsoleGreen(msg))

  type Seq[+A] = scala.collection.immutable.Seq[A]
  val Seq = scala.collection.immutable.Seq

  val ExerciseNumberSpec: Regex = """.*_(\d{3})_.*""".r

  def extractExerciseNr(exercise: String): Int = {
    val ExerciseNumberSpec(d) = exercise
    d.toInt
  }

  def renumberExercise(exercise: String, newNumber: Int)(implicit config: MainSettings): String = {
    val newNumerLZ = f"${config.exerciseProjectPrefix}_$newNumber%03d_"
    val oldNumberPrefix = f"${config.exerciseProjectPrefix}_${extractExerciseNr(exercise)}%03d_"
    exercise.replaceFirst(oldNumberPrefix, newNumerLZ)
  }

  def getExerciseName(exercises: Vector[String], exerciseNumber: Int): Option[String] =
    exercises.find(exercise => extractExerciseNr(exercise) == exerciseNumber)

  def folderExists(folder: File): Boolean =
    folder.exists() && folder.isDirectory

  def dumpStringToFile(string: String, filePath: String): Unit =
    Files.write(Paths.get(filePath), string.getBytes(StandardCharsets.UTF_8))
}
