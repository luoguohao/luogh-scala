package com.luogh.test

import scala.annotation.tailrec

/**
  * @author luogh
  */
object RecursiveTrial {

  def main(args: Array[String]): Unit = {
    1 to 50 foreach (x => println(fact(x)))

    1 to 5 foreach( x => println(s" $x is odd ? ${isOdd(1 to x toList).result}"))
  }

  //非尾递归实现阶乘,
  // 使用递归的话，如果fact的值非常大，那么factorial()方法一直在自我调用
  // 导致方法一直被压入栈中，最后导致stackOverFlow.如果该递归被优化为for循环，那么就
  // 不会出现栈溢出的问题。当然不是所有递归都可以被优化为for循环来表示。只有尾递归才可以。
  // scala 中 可以将尾递归 通过增加注解 @tailrec来 在编译期将尾递归优化为for循环表示。
  // 如果当前不是尾递归，但是依然使用注解@tailrec,那么编译报错。

  //  @tailrec
  def factorial(fact: Int): Int = {
    if (fact == 1) 1 else fact * factorial(fact - 1)
  }


  //尾递归实现方式
  def fact(fact: Int): Int = {
    @tailrec
    def f(step: Int, acc: Int): Int = {
      if (step == 1) acc else f(step - 1, step * acc)
    }
    f(fact, 1)
  }


  /**
    * Trampoline for Tail Calls (交替执行的递归)
    * 有一种递归，是函数A调用函数B，函数B又调用函数A，这样交替调用...，这样也可以使用for循环来代替。
    * scala中提供 TailCalls来满足这样需要。
    *
    * 比如用一种低效的方式计算某个数是否奇数或偶数。
    */

  import scala.util.control.TailCalls._

  def isOdd(list: List[Int]): TailRec[Boolean] =
    if (list.isEmpty) done(false) else tailcall(isEvent(list.tail))

  def isEvent(list: List[Int]): TailRec[Boolean] =
    if (list.isEmpty) done(true) else tailcall(isOdd(list.tail))
}
