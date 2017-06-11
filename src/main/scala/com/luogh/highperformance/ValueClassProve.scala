package com.luogh.highperformance

import scala.util.Random

/**
  * 值类的优势: 减少了不必要的运行时对象的创建，减少GC压力
  * 约束:
  *   1)只能只有一个主构造器，并且主构造器只能是一个非值类型的变量，否则编译报错.
  *   2) 值类中不能声明成员变量，只能是def方法
  * 特殊情况：
  *   在某些特殊情况下，JVM还是需要去实例化值类，比如：模式匹配、运行时类型检查、实例化值类数组
  *
  * 所以，可以选择使用Tagged types 来弥补Value Class 的不足。
  *
  * 使用javap -c 查看当前类的反编译字节码
  * 使用scalac -print 查看去除scala语法糖后的代码
  */
object ValueClassProve {

  // case class Name(value: Price) extends AnyVal 编译失败
  case class Price(value: BigDecimal) extends AnyVal
  case class OrderId(value: Long) extends AnyVal {
    def test(): OrderId = {
      OrderId(1)
    }
  }

  /**
    * 反编译回来printInfo 方法签名为： public static void printInfo(scala.math.BigDecimal, long),
    * 说明值类只是在编译时期做了类型约束,不会再运行期new一个OrderId对象，减少GC压力，但是增加更多的执行指令
    * @param p
    * @param oId
    */
  def printInfo(p: Price, oId: OrderId): Unit =
    println(s"Price: ${p.value}, Id:${oId.value}")

  /**
    * 在值类中定义的方法在执行过程中也不会去new一个OrderId，而是在编译期间转为了普通的方法调用
    * @return
    */
  def newObject():OrderId = OrderId(10L).test()

  /**
    * 在往数组中添加值类对象的时候，就需要实例化值类对象。通过javap -c 可以看到
    * @param count
    * @return
    */
  def newPriceArray(count: Int): Array[Price] = {
    val a = new Array[Price](count)
    for (i <- 0 until count) {
      a(i) = Price(BigDecimal(Random.nextInt()))
    }
    a
  }
}
