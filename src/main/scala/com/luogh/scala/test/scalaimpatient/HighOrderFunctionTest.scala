package com.luogh.scala.test.scalaimpatient

import scala.math._
/**
 * @author Kaola
 * 
 * 高阶函数(一个接受函数参数的函数，称为高阶函数 (higher-order function))
 * 
 */
class HighFunctionTest {
  /**
   * 带函数参数的函数，即接受另一个函数作为参数的函数
   * 这里的参数接受任何接受Double并且返回值为Double的函数
   * 
   * 此时valueAtOneQuarter的类型是:((Double)=>Double)=>Unit
   * 由于valueAtOneQuarter是一个接受函数参数的函数，所以称为高阶函数(higher-order function)
   */
  def valueAtOneQuarter1(f:Double=>Double) = println("result is :"+f(0.25))
  
  /**
   * valueAtOneQuarter2的类型是:((Double,Double) => Double) => Unit 
   */
  def valueAtOneQuarter2(f:(Double,Double)=>Double) = println("result is :"+f(2,3))
   
  /**
   * 高阶函数也可以产出另一个函数
   * mulBy的类型是：(Double) => ((Double) => Double)
   */
  def mulBy(factor:Double)= {(x:Double) =>factor*x}
  
  
}

object mainClass12 extends App {
  val fun01 = (x:Double)=>x*3 // 匿名函数
  Array(3.14,1.43,2.0).map(fun01).foreach(println _)  //等价于 Array(3.14,1.43,2.0).map((x:Double)=>x*3).foreach(println _)
  Array(3.14,1.43,2.0).map((x:Double)=>x*3).foreach(println _)
  
  val obj01 = new HighFunctionTest()
  obj01.valueAtOneQuarter1(fun01)  // 0.25*3 = 0.75
  obj01.valueAtOneQuarter1(sqrt _) // 0.5 (0.5*0.5=0.25)
  val fun02 = obj01.mulBy(2)
  println(fun02(3)) // 2*3=6
  println(obj01.mulBy(2)(23)) // 2*23 = 46
  
  /*** 参数(类型)推断***/
  obj01.valueAtOneQuarter1((x:Double)=>x*3) //0.75
  /**当将一个匿名函数传递给另一个函数或方法时，scala会尽可能推断出类型信息。因此以上匿名函数可以写成：**/
  obj01.valueAtOneQuarter1 { (x) => x*3 } 
  /**同时对于只有一个参数的函数，可以略去参数外围的(),即以上函数可以写成：*/
  obj01.valueAtOneQuarter1 { x => x*3 } 
  /**同时如果参数在=>右侧只出现一次，可以用_替换他**/
  obj01.valueAtOneQuarter1(_*3) 
  
 obj01.valueAtOneQuarter2((x:Double,y:Double)=>x*y*2)
 obj01.valueAtOneQuarter2((x,y)=>x*y*2)
 obj01.valueAtOneQuarter2(_-_*2)  //这样可以。第一个_表示第一个参数x,第二个_表示第二个参数y
 
 val fun = 3*(_:Double)  //等价与 fun = (x:Double)=>3*x
 val fun2:(Double) => Double = 3*_  //可以 同上
 
 (1 to 9).map("*" * _).foreach(println _)
 
 /******************
  * 二元函数
  * 
  * 等同于1 * 2 * 3 * 4 * 5 ,计算过程为: ((((1*2) * 3) * 4) * 5) 两两结合 
  * ***************/
 println((1 to 5).reduceLeft(_ * _)) //_ * _ 表示函数类型为：(Int,Int)=>Int   ，等价于(x:Int,y:Int)=>x*y
 
 //输出一个按长度递增排序的数组
 "this is a test , can you see your".split("\\s+").sortWith(_.length <= _.length).foreach { println _ }
 
 /**
  * 以下排序规则：先按字母排序，再按字符个数排序
  * 元组的排序原则是：元组中第一个元素和其他的元组对应的第一个元素进行比较...然后是第二个元素比较....直到最后。
  */
 "this is a test , can you see your".split("\\s+").sortBy(x => (x.head,x.length)).foreach { println _ }  
 
 
 
 /*************************************************************************
  * 闭包：
  *    在函数体内可以访问到相应作用于的任何变量。同时函数可以在变量不再处于作用域时被调用
  *    
  * 以下调用过程如下：
  *   mulBy首次调用将参数变量factor设为3，该变量在(x:Double)=> factor * x 函数体中
  *   引用，然后该函数被triple引用。
  *   mulBy再次调用将参数变量factor设置为0.5,该变量在(x:Double)=> factor * x 函数体中
  *   引用，然后该函数被half引用。
  *   每个返回的函数都有自己的factor设置
  * 
  * 这样的函数被称为闭包(closure)。 闭包由代码和代码用到的任何非局部变量定义构成
  * 这些函数实际上以类的对象方式实现的，该类有一个实例变量factor和一个包含函数体的apply方法。
  *************************************************************************/
  def mulBy(factor:Double) = (x:Double)=> factor * x
  
  val triple = mulBy(3)
  val half = mulBy(0.5)
  println(triple(14) + " " + half(14)) //打印43 7
 
  
  /***************************************************************************
   * SAM转化
   *  java中不支持函数，需要将动作放在一个实现某个接口的类中，然后将该类的对象传递给某个方法，
   *  一般是匿名类。很多时候这些接口只有一个单个抽象方法(Single Abstract Method)。在java
   *  中他们被称为SAM类型。
   *  
   *  比方说：
   *    var counter = 0
   *    val button = new JButton("test")
   *    button.addActionListener(new ActionListener {
   *      override def actionPerformed(event:ActionEvent) {
   *        couner += 1
   *      }
   *    })
   *    
   *    
   *  如果可以只传一个函数actionPerformed给addActionListener就好了，如下：
   *    button.addActionListener((event:ActionEvent) => count += 1)
   *    
   *    
   *  为了启用这个语法，需要使用隐式转化。如下：
   *    implicit def makeAction(action : (ActionEvent) => Unit) = {
   *      new ActionListener {
   *         override def actionPerformed(event:ActionEvent) {
   *          action(event)
   *        }
   *      }
   *    }
   **************************************************************************/
 
  
  /****************************************************************************
   * 柯里化(curring)
   *     将原来接收两个参数的函数变成接受一个参数的函数的过程。新的函数 返回一个以原有第二个参数
   *     作为参数的函数
   ***************************************************************************/
  //mul接收两个参数
  def mul(x:Int,y:Int) = x*y
  //mulOneAtATime接收一个参数，并返回接收一个参数的函数
  def mulOneAtATime(x:Int) = (y:Int)=>x*y
  //计算两个数据的乘积，调用：
  mul(2,4)
  mulOneAtATime(2)(4)
  
  //scala支持如下简写来定义这样的柯里化函数：
  def mulOneAtATime1(x:Int)(y:Int) = x*y  //使用柯里化可以将某个函数参数单独拎出来，已提供更多用于类型推断的信息
  //如下
  val a = Array("Hello","World")
  val b = Array("hello","world")
  a.corresponds(b)(_.equalsIgnoreCase(_))  //函数_.equalsIgnoreCase(_) 经过柯里化传递的参数 ，函数类型为：(String,String) => Boolean
  
  /*****************************************************************************
  *corresponds的定义如下：
  *   def corresponds[B](that: GenSeq[B])(p: (A,B) => Boolean): Boolean
  *   
  *   在这里 ，that序列 和前提函数p 是分开的两个柯里化的参数。类型推断器可以分析出B出自that的类型。因此可以利用这个信息分出作为参数p传入的函数
  * 拿本例来说，that是String类型的序列。因此前厅函数应有的类型为：(String,String)=>Boolean,因此有了这个信息，编译器就可以接受_.equalsIgnoreCase(_)
  * 作为(a:String,b:String)=> a.equalsIgnoreCase(b) 的简写了
  * ****************************************************************************/
  
  
  
  
  /*******************************************************************************
   * 控制抽象
   * 在scala中将一系列语句归组为不带参数也没有返回值的函数。如下函数在线程中执行某段代码：
   * 
   *******************************************************************************/
   def runInThread(block:() => Unit) {
      new Thread {
        override def run() { block() }
      }.start()
  } 
  
  //这段代码以类型() => Unit的函数形式给出，不过，当调用该函数时，需要写一段()=>
  runInThread( () => {println("H");Thread.sleep(1100)} )
  
  //要省略() => 需要使用换名调用表示法：在参数声明和调用该函数参数的地方省略(),保留=>
  def runInThread01(block: => Unit) {
    new Thread {
        override def run() { block }
      }.start()
  }
  
  runInThread01 { println("H");Thread.sleep(1100) }
  
  //可以构建控制抽象：看上去显示编程语言的关键字的函数。我们可以实现一个完全像是在使用while语句的函数，，比如定义
  //一个until语句，工作原理类似while,只不过把条件反过来用：
  def until(condition : =>Boolean)(block: => Unit){
    if(!condition) {
      block
      until(condition)(block)
    } 
  }
  
  var x = 100
  until(x<50){  //x<50 这种函数参数称为：换名调用参数，函数在调用的时候，该参数表达式不会被求值。表达式将成为无参数函数的函数体，该函数被当做参数传递下去
    println(x)
    x-=1
  }
  
  
  /*****************************************************************************
   * return 表达式
   * scala中不需要return语句返回函数值，函数的返回值就是函数体的值。可以用return来从一个
   * 匿名函数中返回值给包含这个匿名函数的带名函数。这对于控制抽象很有用。
   *****************************************************************************/
  def indexOf(str:String,ch:Char):Int = {
    var i = 0
    //匿名函数{if(str(i) == ch) return i;i+=1} 被传递给until,当return表达式被执行时，包含他的带名函数indexOf终止并返回给定的值
    until(i==str.length) {
      if(str(i) == ch) return i 
      i += 1
    }
    println("i:"+i)
    return -1
  }
  
  //实例一  
  def values(fun:(Int)=>Int,low:Int,high:Int)={
    for(value <- (low to high)) yield (value,fun(value)) //方法一
    //(low to high) zip (low to high).map(fun)   //方法二
    //(low to high).map(fun).zipWithIndex
  }
  
  println(values(x=>x*x,-5,5))
  
  //实例二，使用reduceLeft获取数组中的最大元素
  println(Array(1,25,5,23,5,7,45,6,32).reduceLeft(max(_,_)))
  //实例三，使用to,reduceLeft实现阶乘函数，不使用循环，递归
  def factorialFunc(num:Int) = {
   if(num == 0) 1
   else {
     (1 to num).reduceLeft(_*_)
   }
  }
  println(factorialFunc(3))
  
  def largest(fun:(Int)=>Int,inputs:Seq[Int])= {
    inputs.map(fun).max
  }
  println(largest(x=>10*x-x*x,1 to 10))
  
  println("实例四,返回最大的输出对应的输入")
  
  def largestAtIndex(fun:(Int)=>Int,inputs:Seq[Int])= {
    val tuple = (inputs zip inputs.map(fun)) // Vector((1,9), (2,16), (3,21), (4,24), (5,25), (6,24), (7,21), (8,16), (9,9), (10,0))
    tuple.maxBy {x => x._2}._1  //tuple使用元组中的第二个元素比较大小获取最大的元组(5,25),然后获取该最大元组的输入值：_1
  }
  
  println(largestAtIndex(x=>10*x-x*x,1 to 10))
  println(((1 to 10) zip (1 to 10)).map((x) => x._1 + x._2))
  
  val array01 = Array("this","is","a","test")
  val array02 = Array("aere","ou","k","test")
  
  println(array01.corresponds(array02)(_.length == _.length))
  
  //使用unless控制抽象，工作机制类似if，但是条件是反过来的
  def unless(condition : => Boolean)(block: => Any)={
    if(!condition) {
      block
    }
  }
  var value01 = 1
  var value = unless(1!=1){value01+=1; value01}
  println(value)
  
  
}