package org.cmt

object Main {

  def main(args: Array[String]): Unit = {
  
    println("Hello world!")
  
    val a = 5
    val b = 7
    println(s"$a + $b = ${Math.add(a, b)}")
    println(s"$a x $b = ${Math.multiply(a, b)}")
    println(s"A common value = ${Common.aCommonValue}")
  }
}
