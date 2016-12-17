package com.luogh.test

/**
  * Functor Category (函子范畴)
  *   Functor abstract the map operation.
  * Here, we first defining the abstraction and then implementing it for three concrete types, Seq,Option and A => B
  *
  * @author luogh
  */
object FunctorCategoryTrial {
 def main(args: Array[String]): Unit = {
    val seq = Seq(1,2,4)
    val option = Option(1)
    val funct: Int => Int = _ * 2
    val funct1: Int => String = _.toString

   SeqFunctor.map(seq)(_ * 2).foreach(println)
   OptionFunctor.map(option)(_ * 3).foreach(println)
   val f:Int => String = FunctionFunctor.map(funct)(funct1)
   val f1: Int => String = funct andThen funct1
   val f2: Int => String = funct1 compose funct
   println(f(2))

 }

  trait Functor[F[_]] {
    def map[A,B](fa: F[A])(f: A => B): F[B]
  }

  object SeqFunctor extends Functor[Seq] {
    override def map[A, B](fa: Seq[A])(f: (A) => B): Seq[B] = fa.map(f)
  }

  object OptionFunctor extends Functor[Option] {
    override def map[A, B](fa: Option[A])(f: (A) => B): Option[B] = fa.map(f)
  }

  object FunctionFunctor {
    def map[A,B,C](fa: A => B)(f: B => C): (A => C) = {
      val functor = new Functor[({ type λ[α] = A => α})#λ] {
        override def map[B, C](fa: (A) => B)(f: (B) => C): (A) => C = (x: A) => f(fa(x))
      }

      functor.map(fa)(f)
    }
  }
}
