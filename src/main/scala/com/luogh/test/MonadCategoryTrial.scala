package com.luogh.test


import scala.util.{Failure, Success, Try}

/**
  * Monad Category
  *   对类型A的值域中的每一个元素，使用函数f: A => M[B], 将每一个元素都转为M[B]类型，最后得到的结果集为 M[B]
  *   应用广泛的是flatMap操作。
  *
  * @author luogh
  */
object MonadCategoryTrial {
  def main(args: Array[String]): Unit = {
    val seqf: Int => Seq[Int] = (x: Int) => 1 to x
    val optf: Int => Option[Int] = (x: Int) => Option(x + 1)
    SeqMonad.flatMap(Seq(1,2,3))(seqf).foreach(print)
    println()
    SeqMonad.flatMap(Seq.empty[Int])(seqf).foreach(print)
    println()
    OptionMonad.flatMap(Option(1))(optf).foreach(print)
    println()
    OptionMonad.flatMap(None)(optf).foreach(print)

    type Step = Int => Try[Int]

    val successfulSteps: Seq[Step] = List(
      (i: Int) => Success(i + 5),
      (i: Int) => Success(i + 10),
      (i: Int) => Success(i + 25)
    )

    val partialSuccessfulSteps: Seq[Step] = List(
      (i: Int) => Success(i + 5),
      (i: Int) => Failure(new RuntimeException("Fail")),
      (i: Int) => Success(i + 25)
    )

    def sumCounts(countSteps: Seq[Step]): Try[Int] = {
        val zero: Try[Int] = Success(0)
        countSteps.foldLeft(zero){ (sumTry,step) =>
          sumTry.flatMap(step)
        }
    }

    println(sumCounts(successfulSteps).get)
    println(sumCounts(partialSuccessfulSteps).get)
  }


}

trait Monad[M[_]] {
  def flatMap[A,B](fn: M[A])(f: A => M[B]): M[B]

  /**
    * Monad has a second function that takes a (by-name) value and returns it inside a Monad instance.
    * In scala ,this is typically implemented with constructors and case class apply method.
    *
    * More commonly, an abstraction with just flatMap is called Bind. and an abstraction with just unit or pure
    * is called Applicative.
    *
    * @param fn
    * @tparam A
    * @return
    */
  def unit[A](fn: => A): M[A]

  //some common aliases
  def bind[A,B](fa: M[A])(f: A => M[B]): M[B] = flatMap(fa)(f)

  def >>=[A,B](fa: M[A])(f: A => M[B]): M[B] = flatMap(fa)(f)

  def pure[A](fn: => A): M[A] = unit(fn)

  def `return`[A](fn: => A): M[A] = unit(fn)
}

object SeqMonad extends Monad[Seq] {
  override def flatMap[A, B](fn: Seq[A])(f: (A) => Seq[B]): Seq[B] = fn flatMap(f)

  override def unit[A](fn: => A): Seq[A] = Seq(fn)
}

object OptionMonad extends Monad[Option] {
  override def flatMap[A, B](fn: Option[A])(f: (A) => Option[B]): Option[B] = fn.flatMap(f)

  override def unit[A](fn: => A): Option[A] = Option(fn)
}
