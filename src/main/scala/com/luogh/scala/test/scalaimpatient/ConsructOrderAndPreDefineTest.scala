package com.luogh.scala.test.scalaimpatient

/**
 * @author hadoop
 * 构造顺序和提前定义
 * 
 * 子类进行构造的时候，过程如下：
 * 1） 首先调用父类Creatrue的构造器进行初始化操作。
 * 2）Creatrue构造器将他的range字段设置为10.
 * 3）Creatrue构造器初始化env字段时，需要获取range字段，此时调用range的getter方法，即为range()。
 * 4) 因为range()方法已被子类Ant重写（子类还未初始化），此时Ant的range字段值依然为0（这个是对象被分配空间时所有整型字段的初始值）
 * 5）range的getter方法返回0，那么env被初始化为长度为0的数组
 * 6）Creatrue的构造器执行完成后，接着调用子类的构造器，其中range值被初始化为：2
 * 7) 虽然range的值看上去可能为2或10 ，但是env却被设置为长度为0的数组
 * 
 * ---输出结果为：
 * 
 * Creatrue: range is 0 and env size is0
 * Ant range is :2 and env size is :0
 * ----
 * 
 * 有以下几种解决方法：
 *  1）将val声明为final.但是该字段无法被子类重写。不灵活
 *  2）在超类中将val声明为lazy.这样安全却不高效
 *  class Creatrue {
      lazy val range:Int = 10
      val env :Array[Int] = new Array[Int](range)
      println("Creatrue: range is "+range+" and env size is"+ env.length)
    }
    class Ant extends Creatrue {
      override lazy val range:Int = 2
      println("Ant range is :"+range +" and env size is :"+env.length)
    }
 *  3)使用提前定义语法
 *  
 *  class Ant extends {
 *    override range:Int = 2
 *  } with Creatrue {
 *    println("Ant range is :"+range +" and env size is :"+env.length)
 *  }
 *  
 *  可以使用-Xcheckinit编译器标志来调试构造器顺序的问题。这个标记会产生相应的代码，
 *  scalac -Xcheckinit ConsructOrderAndPreDefineTest.scala
 *  scala scalaimpatient.mainClass3
 *  
 *  --报如下错误：
 *  scala.UninitializedFieldError: Uninitialized field: ConsructOrderAndPreDefineTest.scala: 55
        at scalaimpatient.Ant.range(ConsructOrderAndPreDefineTest.scala:55)
        at scalaimpatient.Creatrue.<init>(ConsructOrderAndPreDefineTest.scala:50)
        at scalaimpatient.Ant.<init>(ConsructOrderAndPreDefineTest.scala:54)
        at scalaimpatient.mainClass3$.<init>(ConsructOrderAndPreDefineTest.scala:60)
        at scalaimpatient.mainClass3$.<clinit>(ConsructOrderAndPreDefineTest.scala)
        at scalaimpatient.mainClass3.main(ConsructOrderAndPreDefineTest.scala)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at java.lang.reflect.Method.invoke(Method.java:606)
        at scala.reflect.internal.util.ScalaClassLoader$$anonfun$run$1.apply(ScalaClassLoader.scala:70)
        at scala.reflect.internal.util.ScalaClassLoader$class.asContext(ScalaClassLoader.scala:31)
        at scala.reflect.internal.util.ScalaClassLoader$URLClassLoader.asContext(ScalaClassLoader.scala:101)
        at scala.reflect.internal.util.ScalaClassLoader$class.run(ScalaClassLoader.scala:70)
        at scala.reflect.internal.util.ScalaClassLoader$URLClassLoader.run(ScalaClassLoader.scala:101)
        at scala.tools.nsc.CommonRunner$class.run(ObjectRunner.scala:22)
        at scala.tools.nsc.ObjectRunner$.run(ObjectRunner.scala:39)
        at scala.tools.nsc.CommonRunner$class.runAndCatch(ObjectRunner.scala:29)
        at scala.tools.nsc.ObjectRunner$.runAndCatch(ObjectRunner.scala:39)
        at scala.tools.nsc.MainGenericRunner.runTarget$1(MainGenericRunner.scala:65)
        at scala.tools.nsc.MainGenericRunner.run$1(MainGenericRunner.scala:87)
        at scala.tools.nsc.MainGenericRunner.process(MainGenericRunner.scala:98)
        at scala.tools.nsc.MainGenericRunner$.main(MainGenericRunner.scala:103)
        at scala.tools.nsc.MainGenericRunner.main(MainGenericRunner.scala)
 *  以便再有未初始化的字段被访问的时候抛出异常，而不是输出缺省值
 */

class Creatrue {
  val range:Int = 10
  val env :Array[Int] = new Array[Int](range)
  println("Creatrue: range is "+range+" and env size is"+ env.length)
}

class Ant extends Creatrue {
  override val range:Int = 2
  println("Ant range is :"+range +" and env size is :"+env.length)
}

object mainClass3 extends App {
  new Ant()
}