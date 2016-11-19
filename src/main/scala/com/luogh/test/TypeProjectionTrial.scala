package com.luogh.test

/**
  * 类型映射
  * Project Type
  * @author luogh
  */
object TypeProjectionTrial {
  def main(args: Array[String]): Unit = {
//    val logger: ServiceImp.Log = new ConsoleLogger
    val logger: ServiceImp#Log = new ConsoleLogger
    val log: ServiceImp#Log = new ServiceImp().logger
    new ServiceImp().logger = new ConsoleLogger
//    val logger1: Service1#Log = new ConsoleLogger

  }
}

trait Logger {
  def log(message: String): Unit
}

class ConsoleLogger extends Logger {
  def log(message: String): Unit = {
    println(message)
  }
}

trait Service1 {
  type Log <: Logger
  var logger: Log
}

class ServiceImp extends Service1 {
  type Log = ConsoleLogger
  var logger: Log = new ConsoleLogger
}