package cmt

import Console.*

def toConsoleRed(msg: String): String = Console.RED + msg + Console.RESET
def toConsoleGreen(msg: String): String = Console.GREEN + msg + Console.RESET
def toConsoleYellow(msg: String): String = Console.YELLOW + msg + Console.RESET
def toConsoleCyan(msg: String): String = Console.CYAN + msg + Console.RESET

def printError(msg: String): Unit = {
  System.err.println(toConsoleRed(msg))
  System.exit(1)
}

def printNotification(msg: String): Unit =
  println(toConsoleGreen(msg))