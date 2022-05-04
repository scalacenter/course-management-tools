package cmt

import Console.*

final case class ExitOnFirstError(exitOnFirstError: Boolean = false)

def toConsoleRed(msg: String): String = Console.RED + msg + Console.RESET
def toConsoleGreen(msg: String): String = Console.GREEN + msg + Console.RESET
def toConsoleCyan(msg: String): String = Console.CYAN + msg + Console.RESET

def printError(msg: String)(implicit eofe: ExitOnFirstError): Unit = {
  System.err.println(toConsoleRed(msg))
  if (eofe.exitOnFirstError) System.exit(-1)
}

def printNotification(msg: String): Unit =
  println(toConsoleGreen(msg))