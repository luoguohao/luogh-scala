package com.luogh.test.scalaFP

import scala.annotation.tailrec

/**
  * @author luogh
  */
class FibonacciTrial {

  /**
    * 费波纳茨数列
    * 0 1 1 2 3 5 ...
    * 0 1 f(n-2) + f(n-1)
    * 递归方式实现fibonacci，在n非常大的时候，容易StackOverFlow.
    *
    * @param n
    * @return
    */
  def fib(n: Int): Int = {
    n match {
      case 1 => 0
      case 2 => 1
      case _ => fib(n - 2) + fib(n - 1)
    }
  }

  /**
    * 考虑使用循环的方式，将每次计算的结果保存在List中，保证在下次计算的时候，
    * 直接去已计算好的值，而不需要通过递归去计算，避免栈溢出
    *
    * @param n
    * @return
    */
  def fib_2(n: Int): Int = {
    import collection.mutable.ArrayBuffer

    val list = new ArrayBuffer[Int]()
    for (i <- 0 to n) {
      if (i == 0) list += 0
      else if (i == 1) list += 1
      else {
        val number = list(i - 2) + list(i - 1)
        println(s"index:$i -> $number")
        list += number
      }
    }
    list(n)
  }

  /**
    * 使用尾递归模拟for循环
    *
    * @param n
    * @return
    */
  def fib_3(n: Int): Int = {
    @tailrec
    def f(cur: Int, pre_1: Int, pre_2: Int): Int = {
      if (cur == n) pre_1 + pre_2
      else f(cur + 1, pre_2, pre_1 + pre_2)
    }

    if(n == 0) 0
    else if (n == 1) 1
    else f(2, 0, 1) // 从第三位开始有规律
  }

  def findInArray(array: Array[String], key: String): Int = {

    @tailrec
    def find(index: Int): Int = {
      if (index >= array.length) -1
      else if (array(index) == key) index
      else find(index + 1)
    }
    find(0)
  }


  def findInArray2[A](array: Array[A], p: A => Boolean): Int = {

    @tailrec
    def find(index: Int): Int = {
      if (index >= array.length) -1
      else if (p(array(index))) index
      else find(index + 1)
    }
    find(0)
  }


  /**
    * 检查Array[A]是否按照给定的比较函数排序
    *
    * array中的每个元素需要和它之后的元素比较，如果都满足ordered返回True,说明，array就是以该排序方式排序的
    */
  def isSorted[A](as: Array[A], ordered: (A, A) => Boolean): Boolean = {
    val result = for {i <- 0 until as.length
         j <- i until as.length
    } yield {
      println(s"i=$i,j=$j")
      if(ordered(as(i), as(j))) true
      else false
    }
    result.reduceLeft(_ && _)
  }

  /**
    * 检查Array[A]是否按照给定的比较函数排序
    *
    * array中的每个元素需要和它之后的元素比较，如果都满足ordered返回True,说明，array就是以该排序方式排序的
    */
  def isSorted2[A](as: Array[A])(ordered: (A, A) => Boolean): Boolean = {
    val result = for {i <- 0 until as.length
                      j <- i until as.length
    } yield {
      println(s"i=$i,j=$j")
      if(ordered(as(i), as(j))) true
      else false
    }
    //result.reduceLeft(_ && _)
    matchAll(result.toList)(_ == true)
  }

  /**
    * 检查Array[A]是否按照给定的比较函数排序
    *
    * array中的每个元素需要和它之后的元素比较，如果都满足ordered返回True,说明，array就是以该排序方式排序的
    */
  def isSorted3[A](as: Array[A])(ordered: (A, A) => Boolean): Boolean = {
    @tailrec
    def f(first: Int,second: Int, pre: Boolean): Boolean = {
      if(first >= as.length && second >= as.length) pre
      else if(second < as.length) {
        println(s"1 -- first:$first -> second:$second")
        f(first, second + 1, ordered(as(first), as(second)) && pre)
      } else {
        println(s"2 -- first:$first -> second:$second")
        f(first + 1, first + 1, ordered(as(first), as(first)) && pre)
      }
    }

    f(0,0,true)
  }

  def matchAll[A](list: List[A])(p: (A) => Boolean): Boolean = {

      @tailrec
      def f(n: Int, pre: Boolean): Boolean = {
        if(n == list.size) pre
        else f(n + 1,p(list(n)) && pre)
      }
     f(0,true)
  }


  // 函数部分应用 partial apply
  /**
    * 一个高阶函数接收一个带两个参数的匿名函数,进行部分应用。
    * 即我们有一个A和一个需要A 和 B产生 C的函数，可以生成一个只需要
    * B 就可以产生一个C的函数(因为我们已经有一个A了)
    * @param a
    * @param f
    * @tparam A
    * @tparam B
    * @tparam C
    * @return
    */
  def partial[A,B,C](a: A, f: (A, B) => C): B => C =
    (b: B) => f(a,b)


  def partial2[A,B,C](a: A)(f: (A, B) => C): B => C = (b: B) => f(a, b)

  def partial3[A,B](a: A)(f: (A, A) => B): A => B = (b: A) => f(a, b)

  def partial4[A,B](a: A, f: (A, A) => B): A => B = (b: A) => f(a, b)

  /**
    * 将带有两个参数的函数f转化为只有一个参数的部分应用函数f.
    * @param f
    * @tparam A
    * @tparam B
    * @tparam C
    * @return
    a*/
  def curry[A,B,C](f: (A, B) => C): A => (B => C) = (a: A) => ((b: B) => f(a, b))


  val f: Int => Int => Int = curry((x: Int, y: Int) => x + y)
  val f1: Int => Int = f(2)

  /**
    * 反柯里化，右箭头是右结合的，所以，A => B => C 等于 A => (B => C)
    * @param f
    * @tparam A
    * @tparam B
    * @tparam C
    * @return
    */
  def uncurry[A,B,C](f: A => B => C): (A, B) => C = (a: A, b: B) => f(a)(b)

  /**
    * 实现一个高阶函数，可以这两个函数为一个函数
    * @param f
    * @param g
    * @tparam A
    * @tparam B
    * @tparam C
    * @return
    */
  def compose[A,B,C](f: B => C, g: A => B): A => C = (a: A) => f(g(a))

  val f11: Double => String = ???
  val f2: String => Double = ???
  val f3: String => String = compose(f11, f2)
  val f4: Double => Double = f2.compose(f11)
  val f5: Double => Double = f11.andThen(f2)

  class Test(var p: Int) {
  }
  new Test(2).p_=(2)
  new Test(2).p
  new Test(2)

  class Test2(p: Int) {
    var p2: Int = p
    def _p3: Int = p2
//    def p2_=(a: Int):Unit = this.p2 = a
  }

  new Test2(2)._p3
  new Test2(2).p2_=(3)

  case class Complex(real: Double, imag: Double) {
    def unary_- : Complex = Complex(-real, imag)
    def -(another: Complex): Complex = Complex(real - another.real, imag - another.imag)
  }

  val complex: Complex = Complex(1, 2)
  val complex2: Complex = -complex
  val complex3: Complex = complex.unary_-

  case class Person(name: String, age: Int)

  class Student(name: String, age: Int, val score: Int) extends Person(name, age)

}

object FibonacciTrial {
   def main(args: Array[String]): Unit = {
    val obj = new FibonacciTrial
    //    println(fib(10))
    println(obj.fib_2(10000))
    println(obj.fib_3(10000))
    val array = Array("a", "b", "c", "d", "e", "f")
    println(obj.findInArray(array, "c"))

    val array2 = Array(1,2,3,4,9,6,9,8,9)
    println(s"is sorted by :${obj.isSorted(array2,(a:Int,b:Int) =>a <= b)}")
    println(s"is sorted by :${obj.isSorted2(array2)(_ <= _)}")
    println(s"is sorted by :${obj.isSorted3(array2)(_ <= _)}")


    println(obj.partial(2,(a: Int, b: Int) => a + b)(3))
    println(obj.partial2(2)((a: Int, b: Int) => a + b)(3))
    println(obj.partial3(2)(_ + _)(2))
    println(obj.partial4(2, (a: Int,b: Int) => a + b)(2))
  }
}

