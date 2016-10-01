package com.luogh.bean

/**
  * @author luogh 
  * @date 2016/9/10
  */
class Person {
  private[bean] var name = new String("default name")
  private val id = 1
  val age = 23
  private[luogh] val sex = 1

  private[bean] def makeTest():Unit = {
    println("test")
  }

  private def makeTest2():Unit = {
    println("test2")
  }
}
