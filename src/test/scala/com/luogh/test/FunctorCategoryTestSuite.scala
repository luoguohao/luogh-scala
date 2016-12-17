package com.luogh.test

import org.scalatest.FunSpec
import org.scalatest.prop.PropertyChecks

/**
  * Functor Category (函子范畴)
  *   含义: 对指定值域中的每个元素，都执行相同的态射f (即函数f),得到一个新的结果域
  *   对应scala中的map操作。
  *     def map[A,B](seq: Seq[A])(f: A => B): Seq[B] = seq map f
  *   颠倒参数顺序：
  *     def map[A,B](f: A => B)(seq: Seq[A]): Seq[B] = seq map f
  *   对map方法 部分函数应用：
  *     val fm = map((i: Int) => i * 2.1) _
  *   此时，fm类型为: Seq[Int] => Seq[Double]的函数类型。
  *   也就是map方法将函数类型f: A => B 转化为了 Seq[A] => Seq[B].
  *
  *   In general,Functor.map morphs A => B, for all types A and B, to F[A] => F[B]
  *   for many F. Put another way, Functor allows us to apply a pure function(f: A=>B)
  *   to a "context" holding one or more A values. We don't have to extract those values
  *   ourselves to apply f, then put the results into a ne instance of the "context".
  *
  *   The Term Functor is meant to capture this abstraction of enabling the use of pure functions
  *   in this way.
  *
  *   Functor 有两条公理：
  *     1) A Functor F preserves identity . That is , the identity of the domain maps to the identity of the codomain. // 即 满足一一映射
  *     2) A Functor F preserves composition. F(f.g) = F(f).F(g) // 满足复合运算
  *
  *   For an example of the first property, an empty list is the "unit" of lists.think of what happens when you concatenate it with another
  *   list. Mapping over an empty list always returns a new empty list, possibly with a different list element type.
  *
  *   以下将验证map方法是否满足一般性范畴公理以及Functor特有的两条公理。
  *
  * @author luogh
  */
class FunctorCategoryTestSuite extends FunSpec with PropertyChecks {

  def id[A] = identity[A] _

  def testSeqMorphism(f2: Int => Int) = {
    val f1: Int => Int = _ * 2
    import FunctorCategoryTrial._
    forAll { (l: List[Int]) =>
      assert(SeqFunctor.map(SeqFunctor.map(l)(f1))(f2) === SeqFunctor.map(l)(f2 compose f1))
    }
  }

  def testFunctionMorphism(f2: Int => Int) = {
    val f1: Int => Int = _ *2
    import FunctorCategoryTrial._
    forAll { (i: Int) =>
      assert(FunctionFunctor.map(f1)(f2)(i) === (f2 compose f1)(i))
    }
  }

  describe("Functor morphism composition") {
    it("works for Sequence Functors") {
      testSeqMorphism(_ + 3)
    }
    it("works for Function Functors") {
      testFunctionMorphism( _ + 3)
    }
  }

  describe("Functor identity composed with a another function commutes") {
    it("works for Sequence Functors") {
      testSeqMorphism(id[Int])
    }
    it("works for Function Functors") {
      testFunctionMorphism(id)
    }
  }

  describe("Functor identity maps between the identities of the categories") {
    it("works for Sequence Functors") {
      val f1: Int => String = _.toString
      import FunctorCategoryTrial._
      assert(SeqFunctor.map(List.empty[Int])(f1) === List.empty[String])
    }

    it("works for Functions Functors") {
      val f1: Int => Int = _ *2
      import FunctorCategoryTrial._
      forAll { (i: Int) =>
        assert(FunctionFunctor.map(id[Int])(f1)(i) === f1.compose(id[Int])(i))
      }
    }
  }

  describe("Functor morphism composition is associative") {
    it("works for Sequence Functors") {
      val f1: Int => Int = _ * 2
      val f2: Int => Int = _ + 3
      val f3: Int => Int = _ * 5
      import FunctorCategoryTrial._
      forAll { (l: Seq[Int]) =>
        val m12 = SeqFunctor.map(SeqFunctor.map(l)(f1))(f2)
        val m23 = (seq: Seq[Int]) => SeqFunctor.map(SeqFunctor.map(seq)(f2))(f3)
        assert(SeqFunctor.map(m12)(f3) === m23(SeqFunctor.map(l)(f1)))
      }
    }

    it("works for Function Funtors") {
      val f1: Int => Int = _ * 2
      val f2: Int => Int = _ + 3
      val f3: Int => Int = _ * 5
      val f: Int => Int = _ + 21
      import FunctorCategoryTrial.FunctionFunctor._
      val m12 = map(map(f)(f1))(f2)
      val m23 = (g: Int => Int) => map(map(g)(f2))(f3)
      forAll {(i: Int) =>
        assert(map(m12)(f3)(i) === m23(map(f)(f1))(i))
      }
    }
  }


}
