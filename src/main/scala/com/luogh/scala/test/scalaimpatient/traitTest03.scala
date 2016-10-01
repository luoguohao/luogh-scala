package com.luogh.scala.test.scalaimpatient

import java.io.PrintStream
import java.io.IOException
import javax.swing.JFrame
import java.awt.Point

/**
 * @author Kaola
 * 
 * 特质中的具体字段和抽象字段以及特质的构造顺序
 * 构造器以如下顺序执行：
 * 1）首先调用超类的构造器
 * 2）特质构造器在超类构造器之后、类构造器之前执行
 * 3）特质由左到右被构造
 * 4）每个特质中，父特质被先构造
 * 5）如果多个特质中共有一个父特质，而那个父特质已经被构造，则不会被再次构造
 * 6）所有特质构造完毕，子类被构造
 * 
 * 自身类型:当特质扩展类时，编译器能够确保的一件事是所有混入该特质的类都认这个
 * 类作为超类，scala还有另一套机制可以保证这一点：自身类型（self type）
 * 当特质以如下代码开始定义是：
 *    this:类型 =>
 * 他便只能被混入指定的子类
 * 
 * 
 */


trait Logged03 {
  println("Logged03 constructed!!")
  def log(msg:String) {println("Logged:")}
}

trait ConsoleLogger03 extends Logged03 {
  println("ConsoleLogger03 constructed!!")
  override def log(msg:String){println("ConsoleLogger:"+msg)}
}

trait FileLogger03 extends Logged03 {
  
  val fileName:String  //抽象字段
  println("FileLogger03 constructed!!"+fileName)
  lazy val out = new PrintStream(fileName)
  override def log(msg:String){out.println(msg);out.flush()}
}

/**********
 * 叠加在一起的特质
 * 使用super.log方法，调用的是特质层级中的下一个特质，而不是像类定义的super方法调用超类方法。具体哪里，要根据特质的添加顺序来决定。
 * 一般来说特质是从最后一个开始处理的。
 * *****************/
trait TimestampLogger03 extends Logged03 {
  println("TimestampLogger03 constructed!!")
  override def log(msg:String){
    super.log("TimestampLogger:" + msg)
  }
}

trait ShortLogger03 extends Logged03 {
  println("ShortLogger03 constructed!!")
  var maxLength = 15  //混入该特质的类自动获得maxLength字段。特质中的字段只是简单的加入到类中，而不是被继承的。
	var minLength:Int   //特质中未被初始化的字段在具体的子类中必须被重写
  override def log(msg:String) {
    super.log( if(msg.length <= maxLength) "ShortLogger:"+msg else "ShortLogger:"+msg.substring(0,maxLength-3)+"...........")
  }
}

/**
 * 特性可以扩展类。这个类自动成为所有混入该特质的超类
 */
trait LoggedException extends Exception with Logged {
  def log(){log(getMessage)}
}

/**
 * 此时的UnhappyException自动变成了Exception的子类
 */
class UnhappyException extends LoggedException {
  override def getMessage="arggh!!"
}
/**
 * 如果当前类的父类不是Expetion的子类则编译不能通过，
 */
//class SavingAccount04 extends Account03 with LoggedException{}  //编译不通过
class SavingAccount05 extends IOException with LoggedException{}  

/**
 * 使用自身类型约束混入指定特性的子类类型,
 * 注意该特质并不扩展Exception类，而是有个一自身类型Exception.
 * 这意味着他只能被混入Exception的子类
 * 
 * 在特质方法中，我们可以调用自身类型的任何方法。
 */

trait LoggedException01  extends Logged {
  
  this:Exception =>
    
      def log(){getMessage()}  //getMessage是自身类型的Exception中的方法
}

/**
 * 自身类型也可以处理结构类型（structural type）:
 * 这种类型只给出类必须拥有的方法，而不是类名称
 */

trait LoggedException02  extends Logged {
  
  this:{def getMessage():String} =>  //这个特质可以混入任何拥有getMessage方法的类
    
      def log(){getMessage()}  //getMessage是自身类型的Exception中的方法
      
}


class OrderedPoint extends Point with Ordered[Point] {
  def compare(that: Point): Int = {
    ???
  }
}
////////////////////////////////////////////////////////////////////////////////////////////////////
abstract class Account03{
  println("Account03 constructed !!")
  var balance:Double = 10
  def withDraw(amount:Double)
}


class SavingAccount03 extends Account03 with ShortLogger03  with ConsoleLogger03 {
  println("SavingAccount03 constructed !!")
  var minLength:Int = 2
  def withDraw(amount: Double): Unit = {
    if(amount > balance) log("Insufficient funds" + " and maxLength is :"+maxLength +" and minLength:"+minLength)
    else balance -= amount
  }
}

class SavingAccount0301 extends Account03 with Logged03 {
  println("SavingAccount0301 constructed !!")
  def withDraw(amount: Double): Unit = {
    if(amount > balance) log("Insufficient funds")
    else balance -= amount
  }
}

///////////////////////////////////////////////////////////////////////////////////////////////////

object mainClass09 extends App {
  
  //val obj01 = new SavingAccount03().withDraw(11)  //print ConsoleLogger:Insufficient funds and maxLength is :15 and minLength:2
  
//////////////////////特性的构造顺序问题//////////////////////////////////////  
//  val obj02 = new SavingAccount0301 with FileLogger03 {
//      println("obj02")
//      val fileName = "myapp.log"  //这样行不通，问题出在特质的构造顺序上，这里重写的fileName字段在FileLogger实例化PrintStream对象时并没有被初始化，因为
//                                   //特质的构造优先于类对象的构造顺序，因此这里会报空指针异常。解决方法：1）对构造PrintStream使用lazy模式 ；2）使用提前定义
//  }
  
  
/////////////////////特性的构造顺序问题，使用lazy方式来解决///////////////////////////////////////////
  
  val obj03 = new SavingAccount0301 with FileLogger03 {
      println("obj02")
      val fileName = "myapp.log"  //解决方法：1）对构造PrintStream使用lazy模式 ；2）使用提前定义
  }
  obj03.log("test")  //在使用lazy方法解决以上问题时，只有在PrintStream对象第一次使用时，才会去获取fileName的值，而此时，fileName已经被子类实例化完成，但是使用lazy的坏处就是效率
                     // 问题，因为没有都要检查是否已经初始化
  
///////////////////特性的构造顺序问题，使用提前定义的方式来解决/////////////////////////
  
  val obj04 = new { val fileName = "myapp.log"  //解决方法：2）使用提前定义
                   } with SavingAccount0301  with FileLogger03
                   
                   
///使用提前定义的方式，也可以在类定义的时候使用///////////////////////
                   
   class SavingAccount0302 extends { val fileName = "myapp01.log" } with Account03 with Logged03 {
     def withDraw(amount: Double): Unit = { ??? }
   }  
   
   
 /////////////////自身类型////////////////////////////////////////////
   
   //val f = new JFrame with LoggedException01  //JFrame不是Exception的子类，不能添加该特性
   
}