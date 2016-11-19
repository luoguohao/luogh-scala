package com.luogh.test

/**
  * type lambdas
  *
  * A type Lambdas is analogous to a function nested within another function , only at the type level.
  * They are used for situations where we need to use a parameterized type that has too many type
  * parameters for the context. this is a coding idiom, rather than a specific feature of the type system.
  *
  * @author luogh
  */
object TypeLambdasTrial {
  def main(args: Array[String]): Unit = {
     import Functor._
     val map = Map(1 -> 2, 3 -> 4)
     val list = List(1,23,4)
    map.map2( _ * 2).foreach(println)
    list.map2( _ * 2).foreach(println)
  }


  trait Functor[A,+M[_]] {
    def map2[B](f: A => B): M[B]
  }

  object Functor {
    implicit class SeqFunctor[A] (seq: Seq[A]) extends  Functor[A,Seq] {
      override def map2[B](f: (A) => B): Seq[B] = seq.map(f)
    }

    implicit class OptionFunctor[A](opt: Option[A]) extends Functor[A,Option] {
      override def map2[B](f: (A) => B): Option[B] = opt.map(f)
    }



    /**
      * type lambda explain:
      *   Functor[V,({type λ[α] = Map[K,α]})#λ]
      *
      *   ~~ after format ~~
      *
      *   (                              - start definition
      *     {                            - start define a structural type
      *       type λ[α] = Map[K,α]       - define a type member that alias for Map. The name λ is arbitrary [any ascii character is ok], type
      *                                    has its own type parameter α [any ascii character is ok], used for the Map key type in this case. 即，此处的 λ类型代表 Map[K,_]这种类型，α代表V这种类型。
      *     }                            - end the structural type
      *   )#λ                            - close the expression with a type projection of the type λ. The λ is an alias for Map with embedded
      *                                  - type parameter that will inferred in subsequent code.
      *
      * @param mapKV 对Map中的value 做map操作
      * @tparam K
      * @tparam V
      */
        implicit  class MapFunctor[K,V](mapKV: Map[K,V]) extends Functor[V,({type λ[α] = Map[K,α]})#λ] { // type lambda
          override def map2[B](f: (V) => B): Map[K, B] = mapKV.map{case (k,v) => (k, f(v))}
        }

//    implicit  class MapFunctor[K,V](mapKV: Map[K,V]) extends Functor[V,({type L[x] = Map[K,x]})#L] { // type lambda
//      override def map2[B](f: (V) => B): Map[K, B] = mapKV.map{case (k,v) => (k, f(v))}
//    }
  }
}
