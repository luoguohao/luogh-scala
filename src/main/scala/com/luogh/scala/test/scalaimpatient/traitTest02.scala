package com.luogh.scala.test.scalaimpatient

/**
 * @author Kaola
 */
trait Logger{
  def log(msg:String)
  def info(msg:String){log("INFO:"+msg)}
  def warn(msg:String){log("WARN:"+msg)}
  def severe(msg:String){log("SEVERE:"+msg)}
}

/**
 * scala认为TimestampLogger01依旧是抽象的，他需要混入一个具体的log方法。
 * 因为根据正常的继承规则，直接调用super.log是错的，因为Logger的log方法是抽象
 * 的。但是实际上我们没法知道哪个log方法最终被调用。这取决于特质被混入的顺序。
 * 因此必须给方法打上abstract 和 override关键字
 */
trait TimestampLogger01 extends Logger {
 /* def log(msg:String) {
    super.log(msg)
  }*/
  abstract override def log(msg:String) {
    super.log("TimestampLogger01:"+msg)
  }
}

trait ConsoleLogger01 extends Logger {
   abstract override def log(msg:String) {
    super.log("ConsoleLogger01:"+msg)
  }  
}

trait FileLogger01 extends Logger {
   def log(msg:String) {
    println("ConsoleLogger01:"+msg)
  }  
}

class Test extends Logger {
  def test(){
    log("this is a test")
  }
  def testInfo(){
    info("this is test for info log level")
  }

  def log(msg: String): Unit = {
    println("Test:"+msg)
  }
}
object mainClass08 extends App {
  val obj01 = new Test with ConsoleLogger01
  obj01.test()  //Test:ConsoleLogger01:this is a test
  obj01.testInfo() //Test:ConsoleLogger01:INFO:this is test for info log level
 // val obj02 = new Test with FileLogger01  编译报错,FileLogger01中实现的log方法与Test中重写的log方法冲突
}