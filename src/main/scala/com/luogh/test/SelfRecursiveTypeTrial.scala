package com.luogh.test

/**
  * Self-Recursive Types: F-Bounded Polymorphic
  * 类似于Java中的Enum类型: Enum<E extends Enum<E>>
  *
  * @author luogh
  */
object SelfRecursiveTypeTrial {
  def main(args: Array[String]): Unit = {
    val c1 = Child("c1")
    val c2 = Child2("c2")
    val c11 = c1.make
    val c21 = c2.make

    val p1:Parent[Child] = c1
    val p2: Parent[Child2] = c2
    p1.make
    p2.make
  }
}

trait Parent[T <: Parent[T]] {
  def make: T
}

case class Child(s: String) extends Parent[Child] {
  override def make: Child = Child(s)
}

case class Child2(s: String) extends Parent[Child2] {
  override def make: Child2 = Child2(s)
}
