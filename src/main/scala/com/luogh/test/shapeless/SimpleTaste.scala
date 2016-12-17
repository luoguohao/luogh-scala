package com.luogh.test.shapeless

import shapeless._

object SimpleTaste {
  def main(args: Array[String]): Unit = {
    val genericEmployee = Generic[Employee].to(Employee("Dave", 123, false))
    val genericIceCream = Generic[IceCream].to(IceCream("Sundae", 1, false))

    println(genericCsv(genericEmployee))
    println(genericCsv(genericIceCream))
  }

  case class Employee(name: String, number: Int, manager: Boolean)
  case class IceCream(name: String, numCherries: Int, inCone: Boolean)

  def genericCsv(gen: String :: Int :: Boolean :: HNil): List[String] =
    List(gen(0), gen(1).toString, gen(2).toString)
}