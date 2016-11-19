package com.luogh.test.scalaTypeSystem

/**
  * scala中的参数类型的可变性,对函数可变性的讨论
  *
  * Scala规定，协变类型只能作为方法的返回类型，而逆变类型只能作为方法的参数类型。类比函数的行为，结合Liskov替换原则，就能发现这样的规定是非常合理的。
    里氏替换原则（Liskov Substitution Principle LSP）
    里氏替换原则是面向对象设计的基本原则之一。任何基类可以出现的地方，子类一定可以出现。LSP是继承复用的基石，只有当子类可以替换基类，
软件单位的功能不受影响时，基类才能真正的被复用，而子类也可以在基类的基础上增加新的行为。

      里氏替换原则通俗的来讲就是：子类可以扩展父类的功能，但不能改变父类原有的功能。它包含以下4层含义：
                                子类可以实现父类的抽象方法，但不能覆盖父类的非抽象方法。
                                子类中可以增加自己特有的方法。
                                当子类的方法重载父类的方法时，方法的前置条件（即方法的形参）要比父类方法的输入参数更宽松。
                                当子类的方法实现父类的抽象方法时，方法的后置条件（即方法的返回值）要比父类更严格。
  * @author luogh
  */
object VarianceParameterTypTrial {
  def main(args: Array[String]): Unit = {

  }


  class CSuper {
    def mSuper() = println("CSuper")
  }

  class C extends CSuper {
    def m() = println("C")
  }

  class CSub extends C {
    def mSub() = println("CSub")
  }

  /**
    * scala函数声明如下：
    * trait Function1[-A, +R]
    * 参数类型A,R分别表示函数的参数以及返回值。
    * 函数的参数类型是逆变的(contravariant),而返回值时协变的(covariant)
    *
    * 如下例子，对于函数变量 f,它声明的参数类型是C,返回值类型是C。
    * 那么对于这样函数变量f,我们希望所有的类型为C或者C的子类型都可以被处理，
    * 那么如果我们真正传递给f变量的函数的参数类型是C的子类型，(如：CSub=> C )那么f函数变量将无法处理其他C的子类型，所以，
    * 这个是不能满足的，因此，如果我们传递给f变量的函数参数类型是C的父类型,(如：CSuper=>C),那么f函数变量可以处理C类型以及
    * 他的所有子类型，当然，f函数也可以处理除C类型外的所有其他CSpuer子类型。但是这已经满足f变量声明的那样所有的C类型都可以处理。
    * 因此，函数的参数类型应该声明为逆变的。
    *
    * 同理，对于函数的返回值类型，因为f变量声明的返回值类型是C,所以在赋值给变量f的函数的返回值一定是只能返回C类型或者C类型的
    * 子类，如果返回的类型是C类型的父类型，那么对于其他使用f函数的变量来说，就不能处理除的C类型及子类型之外的其他类型了。
    * 所以，函数的返回值应该声明为协变的。
    */
  var f: C => C = (c: CSuper) => new C
      f         = (c: CSuper) => new CSub
  //  f         = (c: CSuper) => new CSuper  // failed
      f         = (c: C) => new C
      f         = (c: C) => new CSub
  //  f         = (c: C) => new CSuper //failed
  //  f         = (c: CSub) => new C // failed

  class VarianceTest[+A] {
    /**
      * test(x: A): Int 编译失败，分析如下：
      *  因为参数化类型A为协变，因此以下声明是正确的，即VarianceTest[CSub]是VarianceTest[C]的子类型：
      *  val obj: VarianceTest[C] = new VarianceTest[CSub]
      *  因此,对于声明类型为VarianceTest[C]的obj,它的test方法签名如下：
      *     def test(x: C): Int 即该函数类型是f: C => Int
      *  而对于VarianceTest的子类型VarianceTest[CSub],他的test方法签名如下：
      *     def test(x:CSub): Int 即该函数类型是f: CSub => Int
      *  因为VarianceTest[CSub]是VarianceTest[C]的子类型，所以obj在调用test:C => Int方法的时候,实际会调用
      *  子类型VarianceTest[CSub]中的test: CSub => Int方法，但是因为因为函数的可变性质，
      *  var f:C => Int = (x: CSub) => Int 是不成立的。
      *  所以当A为协变的时候，test(x: A)是不能编译通过的。因为函数参数是逆变的。
      *
      */
//    def test(x: A): Int = ???  // failed
  }

  class VarianceContravariantTest[-A] {
    /**
      * test(x: A): Int 编译成功，分析如下：
      *  因为参数化类型A为逆变，因此以下声明是正确的，VarianceContravariantTest[C]是VarianceContravariantTest[CSub]的子类型：
      *  val obj: VarianceContravariantTest[CSub] = new VarianceContravariantTest[C]
      *  因此,对于声明类型为VarianceContravariantTest[CSub]的obj,它的test方法签名如下：
      *     def test(x: CSub): Int 即该函数类型是f: CSub => Int
      *  而对于VarianceContravariantTest[CSub]的子类型VarianceContravariantTest[C],他的test方法签名如下：
      *     def test(x:C): Int 即该函数类型是f: C => Int
      *  因为VarianceContravariantTest[C]是VarianceContravariantTest[CSub]的子类型，所以obj在调用test:CSub => Int方法的时候,实际会调用
      *  子类型VarianceContravariantTest[C]中的test: C => Int方法，根据函数的可变性质，
      *  var f:CSub => Int = (x: C) => Int 是成立的。
      *  所以当A为逆变的时候，test(x: A)是编译通过的。满足函数参数是逆变的。
      *
      */
    def test(x: A): Int = 0
  }

  val obj: VarianceContravariantTest[CSub] = new VarianceContravariantTest[C]

  class VarianceWithHighOrderFunctionTest[+A] {
    /**
      * test(f: A=>Int): Int 编译成功，分析如下：
      *  因为参数化类型A为协变，因此以下声明是正确的，即VarianceWithHighOrderFunctionTest[CSub]是VarianceWithHighOrderFunctionTest[C]的子类型：
      *  val obj: VarianceWithHighOrderFunctionTest[C] = new VarianceWithHighOrderFunctionTest[CSub]
      *  因此,对于声明类型为VarianceWithHighOrderFunctionTest[C]的obj,它的test方法签名如下：
      *     def test(f: C => Int): Int 即该函数类型是f: C => Int => Int
      *  而对于VarianceWithHighOrderFunctionTest[C]的子类型VarianceWithHighOrderFunctionTest[CSub],他的test方法签名如下：
      *     def test(f: CSub => Int): Int 即该函数类型是f: CSub => Int => Int
      *  因为VarianceWithHighOrderFunctionTest[CSub]是VarianceWithHighOrderFunctionTest[C]的子类型，所以obj在调用test:C => Int => Int方法的时候,实际会调用
      *  子类型VarianceWithHighOrderFunctionTest[CSub]中的test: CSub => Int => Int方法，但是因为因为函数的可变性质，
      *  var f: (C => Int) => Int = (CSub => Int) => Int 是不成立的。
      *  所以当A为协变的时候，test(f: A=>Int): Int是编译通过的。因为函数参数是逆变的。
      *
      */
    def test(f: A=>Int): Int = 12
    def test1(f: Function1[A, Int]): Int = 12

  }

  val varianceTest: VarianceWithHighOrderFunctionTest[C] = new VarianceWithHighOrderFunctionTest[CSub]
  var f1: C => Int => Int = (x: CSuper) => (y: Int) => y
  var f2: (C => Int) => Int = (y: CSub => Int) => 1
//  var f3: (C => Int) => Int = (y: CSuper => Int) => 1
//  f1((x: CSuper) => 4)
//  f1((x: C) => 4)
//  f1((x: CSub) => 4)
  varianceTest.test((x: CSuper) => 11)
  varianceTest.test((x: C) => 11)
//    varianceTest.test((x: CSub) => 11) //failed


  class VarianceTest3 {
    def test(x: VarianceTest[C]): Int = 1
    def test1(x: VarianceContravariantTest[C]): Int = 1

  }

  val varianceTest3 = new VarianceTest3
  varianceTest3.test(new VarianceTest[C])
//  varianceTest3.test(new VarianceTest[CSuper])
  varianceTest3.test(new VarianceTest[CSub])
  varianceTest3.test1(new VarianceContravariantTest[C])
  varianceTest3.test1(new VarianceContravariantTest[CSuper])
//  varianceTest3.test1(new VarianceContravariantTest[CSub])
  class VarianceMultiTest[-A, B] {
    //      def test(x: A)(f: A => B): B = f(x)
  }
}

