package com.luogh.test

/**
  * @author luogh
  */
object TypeExpectOneProblemTrial {
  def main(args: Array[String]): Unit = {

  }

  class A[X]
  class C[M[X] <: A[X]]
  new C[A]
//  new C[A[Int]]  // 11: error: this.A[Int] takes no type parameters, expected: one

  /**
    * http://stackoverflow.com/questions/4614376/bug-in-scalas-type-system
    *
    * 失败原因：
    *   Let's see what this means in plain English.
          class A[X]
            means: let A be a class that takes one type parameter.
          class C[M[X] <: A[X]]
            means: let C be a class that takes one type parameter, which should be a class that takes one type parameter AND,
            parameterized, is a subclass of class A parameterized with the same type.

        When you write new C[A]
        you're saying: create an instance of C with A as parameter. Does A conform to the criteria above? Yes, it's a class
                       that takes one type parameter, and parameterized it is a subclass of itself parameterized.
        However, when you write new C[A[Int]]
        the type parameter you're trying to give C, A[Int], does not conform to the criteria: A[Int] does not take any type parameters,
        which the compiler kindly tells you. (And it is not a subclass of A[X] either.)
    */
}
