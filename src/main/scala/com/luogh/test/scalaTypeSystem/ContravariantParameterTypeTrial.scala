package com.luogh.test.scalaTypeSystem

/**
  * 逆变
  * @author luogh
  */
object ContravariantParameterTypeTrial {

  def main(args: Array[String]): Unit = {}
  trait Item
  class PlasticItem extends Item
  class PaperItem extends Item
  class PlasticBottle extends PlasticItem
  class Newspaper extends PaperItem

  class GarbageCan[+A]

  def setGarbageCanForPlastic(gc: GarbageCan[PlasticItem]): Unit = {}

//  setGarbageCanForPlastic(new GarbageCan[Item])
  setGarbageCanForPlastic(new GarbageCan[PlasticItem])
  setGarbageCanForPlastic(new GarbageCan[PlasticBottle])

  class GarbageCan_Contra[-A]

  def setGarbageCanForPlastic_Contra(gc: GarbageCan_Contra[PlasticItem]): Unit = {}

  setGarbageCanForPlastic_Contra(new GarbageCan_Contra[Item])
  setGarbageCanForPlastic_Contra(new GarbageCan_Contra[PlasticItem])
//  setGarbageCanForPlastic_Contra(new GarbageCan_Contra[PlasticBottle])

  class GarbageCan_In_Contra[-A] {
    /**
      * 编译失败，因为GarbageCan_In_Contra是逆变的，所以GarbageCan_In_Contra[A_super]
      * 是GarbageCan_In_Contra[A]的子类型，同时对于函数的可变性，需要子类的test方法参数
      * 接受更广的范围，因为GarbageCan_In_Contra[A_super]的test方法的参数GarbageCan_In_Contra[A_super]
      * 应该是GarbageCan_In_Contra[A]的父类型，也就是此时的A应该是协变的，而这个是与之前的
      * 假设冲突。所以编译失败。
      */
//    def test(gc: GarbageCan_In_Contra[A]): Unit = {}

    /**
      * 编译成功，
      * @param gc
      * @tparam B
      * @return
      */
    def add[B <: A](gc: GarbageCan_In_Contra[B]): GarbageCan_In_Contra[B] = gc

/**
  * 编译失败，
  * */
//    def add[B >: A](gc: GarbageCan_In_Contra[B]): GarbageCan_In_Contra[B] = gc

    /**
      * 编译成功，因为GarbageCan_In_Contra是逆变的，所以GarbageCan_In_Contra[A_super]
      * 是GarbageCan_In_Contra[A]的子类型，同时对于函数的可变性，需要子类的test方法参数
      * 接受更广的范围，因为GarbageCan_In_Contra[A_super]的test方法的参数A_super
      * 应该是A的父类型。所以是根据里氏替换原则，这个是可以编译成功的。
      * @param gc
      */
    def test1(gc: A): Unit = {}
  }

  val r1:GarbageCan_In_Contra[PlasticBottle]  = new GarbageCan_In_Contra[PlasticItem]
  val result1 = r1.add(new GarbageCan_In_Contra[PlasticBottle])
  val result2 = r1.add(new GarbageCan_In_Contra[PlasticItem])
  val result3 = r1.add(new GarbageCan_In_Contra[Item])


  class GarbageCan_In_Co[+A] {
    /**
      * 编译失败，因为GarbageCan_In_Contra是逆变的，所以GarbageCan_In_Contra[A_super]
      * 是GarbageCan_In_Contra[A]的子类型，同时对于函数的可变性，需要子类的test方法参数
      * 接受更广的范围，因为GarbageCan_In_Contra[A_super]的test方法的参数GarbageCan_In_Contra[A_super]
      * 应该是GarbageCan_In_Contra[A]的父类型，也就是此时的A应该是协变的，而这个是与之前的
      * 假设冲突。所以编译失败。
      */
    //    def test(gc: GarbageCan_In_Contra[A]): Unit = {}

    /**
      * 编译成功，
      * @param gc
      * @tparam B
      * @return
      */
    def add[B >: A](gc: GarbageCan_In_Co[B]): GarbageCan_In_Co[B] = gc

    /**
      * 编译失败，
      * */
    //    def add[B <: A](gc: GarbageCan_In_Contra[B]): GarbageCan_In_Contra[B] = gc

    /**
      * 编译失败
      */
//    def test1(gc: A): Unit = {}
  }

  val r2:GarbageCan_In_Co[PlasticItem]  = new GarbageCan_In_Co[PlasticBottle]
  val result21 = r2.add(new GarbageCan_In_Co[PlasticBottle])
  val result22 = r2.add(new GarbageCan_In_Co[PlasticItem])
  val result23 = r2.add(new GarbageCan_In_Co[Item])
  println("done")
}



