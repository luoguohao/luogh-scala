package com.luogh.scala.test.scalaimpatient

/**
 * @author Kaola
 */
trait Logged {
  println("init Logged")
  def log(msg:String) {println("Logged:")}
}

trait ConsoleLogger extends Logged {
  println("init ConsoleLogger")
  override def log(msg:String){println("ConsoleLogger:"+msg)}
}

trait FileLogger extends Logged {
   println("init FileLogger")
  override def log(msg:String){println("FileLogger:"+msg)}
}

/**********
 * 叠加在一起的特质
 * 使用super.log方法，调用的是特质层级中的下一个特质，而不是像类定义的super方法调用超类方法。具体哪里，要根据特质的添加顺序来决定。
 * 一般来说特质是从最后一个开始处理的。
 * *****************/
trait TimestampLogger extends Logged {
  println("TimestampLogger")
  override def log(msg:String){
    super.log("TimestampLogger:" + msg)
  }
}

/**
 * 如果需要控制具体是哪个特质的方法被调用，则可以在方括号中给出名称，super[TimestampLogger].log(....)。这里给出的类型
 * 必须是直接超类型，不能使用继承关系中的更远的特质和类
 */
trait TimestampLoggerChild extends TimestampLogger {
  println("TimestampLoggerChild")
  override def log(msg:String) {
    super[TimestampLogger].log("TimestampLoggerChild:" + msg)
  }
}
trait ShortLogger extends Logged {
  println("ShortLogger")
  val maxLength = 15  //混入该特质的类自动获得maxLength字段。特质中的字段只是简单的加入到类中，而不是被继承的。
  override def log(msg:String) {
    super.log( if(msg.length <= maxLength) "ShortLogger:"+msg else "ShortLogger:"+msg.substring(0,maxLength-3)+"...........")
  }
}
///////////////////////////////////////////

abstract class Account{
  println("init Account")
  var balance:Double = 10
  def withDraw(amount:Double)
}

class SavingAccount extends Account with Logged {
  println("init SavingAccount")
  def withDraw(amount: Double): Unit = {
    if(amount > balance) log("Insufficient funds")
    else balance -= amount
  }
}

object mainClass7 extends App {
  //val obj01 = new SavingAccount().withDraw(11)  //print nothing
  //在构造对象的时候加入特质
  //val obj02 = new SavingAccount() with ConsoleLogger  //print  ConsoleLogger :Insufficient funds  
  //obj02.withDraw(11)
    
  //查看特质的执行顺序
  val obj03 = new SavingAccount with ConsoleLogger with TimestampLogger with ShortLogger
//  val obj04 = new SavingAccount with ConsoleLogger with ShortLogger with TimestampLogger
//  val obj05 = new SavingAccount with TimestampLogger with ShortLogger with ConsoleLogger
//  val obj06 = new SavingAccount with ConsoleLogger  with TimestampLogger with TimestampLoggerChild
//  val obj07 = new SavingAccount with ConsoleLogger with TimestampLoggerChild
  obj03.withDraw(11)  //print ConsoleLogger:TimestampLogger:ShortLogger:Insufficient...........
//  obj04.withDraw(11)  // print ConsoleLogger:ShortLogger:TimestampLog...........
//  obj05.withDraw(11)  //ConsoleLogger:Insufficient funds
//  obj06.withDraw(11)  //ConsoleLogger:TimestampLogger:TimestampLoggerChild:Insufficient funds
//  obj07.withDraw(11)  //ConsoleLogger:TimestampLogger:TimestampLoggerChild:Insufficient funds
  
}