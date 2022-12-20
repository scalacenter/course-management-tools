package cmt

/** Copyright 2022 - Eric Loots - eric.loots@gmail.com / Trevor Burton-McCreadie - trevor@thinkmorestupidless.com
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *
  * See the License for the specific language governing permissions and limitations under the License.
  */

import sbt.io.syntax.File

import Console.*

def toConsoleRed(msg: String): String = Console.RED + msg + Console.RESET
def toConsoleGreen(msg: String): String = Console.GREEN + msg + Console.RESET
def toConsoleYellow(msg: String): String = Console.YELLOW + msg + Console.RESET
def toConsoleCyan(msg: String): String = Console.CYAN + msg + Console.RESET

def printError(msg: String): Unit =
  System.err.println(toConsoleRed(msg))

def printErrorAndExit(message: String): Unit =
  System.err.println(toConsoleRed(s"Error: $message"))
  System.exit(1)

def printMessage(msg: String): Unit =
  println(toConsoleGreen(msg))

def printNotification(msg: String): Unit =
  println(toConsoleGreen(msg))

extension (result: Either[CmtError, String])
  def printResult(): Unit =
    result match
      case Left(errorMessage) =>
        printErrorAndExit(errorMessage.toDisplayString)
      case Right(message) =>
        printMessage(message)
