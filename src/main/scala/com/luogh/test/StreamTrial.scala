package com.luogh.test

/**
  * @author luogh
  */
object StreamTrial {

  def main(args: Array[String]): Unit = {
        lazy val fibs: Stream[BigInt] = BigInt(0) #:: BigInt(1) #:: fibs.zip(fibs.tail).map { n => n._1 + n._2 }
        fibs take 5 foreach println

//    val lists = List(1, 2, 3, 4, 5, 6, 7)
//    MyStream(lists: _*).filter(_ > 3).take(3).toList.foreach(println)
  }
}

trait MyStream[+A] {

  import MyStream._

  def filter(p: A => Boolean): MyStream[A] = {
    this match {
      case Cons(head, tail) =>
        if (p(head())) cons(head(), tail().filter(p))
        else tail().filter(p)
      case Empty => empty
    }
  }

  def take(n: Int): MyStream[A] = {
    if (n > 0) {
      this match {
        case Cons(head, tail) if n == 1 => cons(head(), empty)
        case Cons(head, tail) => cons(head(), tail().take(n - 1))
        case _ => empty
      }
    } else empty
  }

  def toList: List[A] = {
    this match {
      case Cons(head, tail) => head() :: tail().toList
      case Empty => Nil
    }
  }
}

case object Empty extends MyStream[Nothing]

case class Cons[+A](h: () => A, t: () => MyStream[A]) extends MyStream[A]

// Cons(()=>1,Cons(()=>2,()=>Empty))  包含1和2的Stream了。

object MyStream {
  def apply[A](elem: A*): MyStream[A] = {
    if (elem.isEmpty) empty
    else cons(elem.head, apply(elem.tail: _*))
  }

  def cons[A](hd: => A, tl: => MyStream[A]): MyStream[A] = {
    lazy val lhd = hd
    lazy val ltl = tl
    Cons(() => lhd, () => ltl)
  }

  def empty[A]: MyStream[A] = Empty
}