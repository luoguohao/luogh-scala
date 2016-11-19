package com.luogh.test

import com.luogh.test.HigherKinder.Reduce
import com.luogh.test.HigherKinder.Reduce._
import com.luogh.test.Normal.Add
import com.luogh.test.Normal.Add._

import scala.language.higherKinds


/**
  * Higher-Kinder Type
  *
  *   def sum(seq: Seq[Int]): Int = seq reduce(_ + _)
  *   sum(Vector(1,2,4,5))
  * 高级泛化
  *
  * @author luogh
  */
object HigherKinderTypeTrial {
  def main(args: Array[String]): Unit = {
    Normal.sumSeq(Seq(1,2,3))
    Normal.sumSeq(Seq(1 -> 2, 2 -> 3, 3 -> 4))
//  Normal.sumSeq(Option(2))
    val s = implicitly[Reduce[Int,Seq]]
    HigherKinder.sum1(Seq(1, 2, 3))
    HigherKinder.sum(List(1, 2, 3))
    HigherKinder.sum(1 to 4)
    HigherKinder.sum(Option(3))
//    HigherKinder.sum[Int, Option](None) // ERROR, it`s error to reduce an empty container.

    HigherKinder_Pro.sum(List(1, 2, 3))
    HigherKinder_Pro.sum(1 to 4)
    HigherKinder_Pro.sum(Option(3))

    HigherKinder_Pro.sum1(List(1, 2, 3))
    HigherKinder_Pro.sum1(1 to 4)
    HigherKinder_Pro.sum1(Option(3))
  }
}

object Normal {
  trait Add[T] {
    def add(t1: T, t2: T): T
  }

  object Add {
    implicit val intAdd: Add[Int] = new Add[Int] {
      override def add(t1: Int, t2: Int): Int = t1 + t2
    }

    implicit val addIntPair: Add[(Int, Int)] = new Add[(Int, Int)] {
      override def add(t1: (Int, Int), t2: (Int, Int)): (Int, Int) = (t1._1 + t2._1, t1._2 + t2._2)
    }
  }

  /**
    * 该方式只能是支持Seq类型的集合做加减操作，需要将集合类型也做进一步的抽象
    *
    * @param seq
    * @tparam T
    * @return
    */
  def sumSeq[T: Add](seq: Seq[T]): T = {
    seq.reduceLeft(implicitly[Add[T]].add(_, _))
  }
}

object HigherKinder {

  trait Reduce[T, -M[T]] { // 使用逆变，在隐式值的获取时使用多态
    def reduce(m: M[T])(op: (T, T) => T): T
  }

  object Reduce {
    // 此处使用隐式函数而不是隐式值的原因是，类型T 依然需要在运行时决定，隐式值不能做相关推导
    // implicit val seq: Reduce[T, Seq] = new Reduce[T, Seq]{} //ERROR
    implicit def seqReduce[T]: Reduce[T, Seq] = {
      new Reduce[T, Seq] {
        override def reduce(m: Seq[T])(op: (T, T) => T): T = m.reduceLeft(op)
      }
    }

    /**
      * 同时，注意，定义的时候如果写成：
      *     implicit def optionReduce[T]: Reduce[T, Option[T]] = {
      * new Reduce[T, Option[T]] {
      * override def reduce(m: Option[T])(op: (T, T) => T): T = m.reduceLeft(op)
      * }
      * }
      * 编译报错：
      *     Option[T] takes no type parameters, expected: one
      *     reason : http://stackoverflow.com/questions/4614376/bug-in-scalas-type-system
      * 解决方式：
      *     Reduce[T, Option[T]] 改为 Reduce[T, Option] 即可，编译器会通过 Reduce的定义去推断Option的类型
      *
      * @tparam T
      * @return
      */
    implicit def optionReduce[T]: Reduce[T, Option] = {
      new Reduce[T, Option] {
        override def reduce(m: Option[T])(op: (T, T) => T): T = m.reduceLeft(op)
      }
    }
  }

  def sum[T: Add, M[T]](col: M[T])(implicit red: Reduce[T, M]): T = {
    red.reduce(col)(implicitly[Add[T]].add(_, _))
  }

//  def sum[T, M[T]](col: M[T])(implicit red: Reduce[T, M]): Unit = {
//    val s = implicitly[Add[Int]] // SUCCESS,获取隐式类型为：Add[Int] 的隐式值
//    val s1 = implicitly[Add[T]] //  ERROR,  找不到获取隐式类型为：Add[T] 的隐式值,在编译时期，T类型无法知道，对T类型加上上下文约束. T:Add, 即可
//    println(s)
//  }

  def sum1[T: Add, M[T]](col: M[T]): Unit = {
    val s = implicitly[Reduce[T,Seq]]
    println(s)
//    implicitly[Reduce[T,M]].reduce(col)(implicitly[Add[T]].add(_, _))

  }
}

object HigherKinder_Pro {

  trait ReducePro[-M[_]] { // 存在类型
  def reduce[T](m: M[T])(op: (T, T) => T): T
  }

  object ReducePro {
    // 此处可以使用隐式值的原因是，只有一个 M类型
     implicit val seq: ReducePro[Seq] = new ReducePro[Seq] {
        override def reduce[T](m: Seq[T])(op: (T, T) => T): T = m.reduceLeft(op)
    }

    implicit val opt: ReducePro[Option] = new ReducePro[Option] {
      override def reduce[T](m: Option[T])(op: (T, T) => T): T = m.reduceLeft(op)
    }
  }

  def sum[T: Add, M[T]](col: M[T])(implicit red: ReducePro[M]): T = {
    red.reduce(col)(implicitly[Add[T]].add(_, _))
  }

  /**
    * ReducePro使用 上下文类型约束也是可以的。但是之前的Reduce[T,M[T]]就不行，因为Reduce包含两个类型参数 T、M
    * @param col
    * @tparam T
    * @tparam M
    * @return
    */
  def sum1[T: Add, M[_]: ReducePro](col: M[T]): T = {
    implicitly[ReducePro[M]].reduce(col)(implicitly[Add[T]].add(_, _))
  }
}

