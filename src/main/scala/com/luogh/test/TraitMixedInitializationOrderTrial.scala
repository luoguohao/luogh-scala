package com.luogh.test

/**
  * 多个特质混入时，每个特质的初始化顺序,即每个特质 body的初始化顺序
  * @author luogh
  */
object TraitMixedInitializationOrderTrial {
  var initialOrder: Int = 0
  def main(args: Array[String]): Unit = {
    println("Create C12 before")

    /**
      * 因为C12继承自Base12,混入T1，T2特质，所以在实例化C12的时候，首先会
      * 执行Base12中的body体内的值，然后继续从左往右依次执行T1，T2的body体
      * 中的值，最后才执行C12本身body体的值。
      */
    new C12
    println("After Create C12 ")
  }

  trait T1 {
    initialOrder += 1
    println(s"T1 initialOrder:$initialOrder")


  }

  trait T2 {
    initialOrder += 1
    println(s"T2 initialOrder:$initialOrder")
    println(s" in T2: y = $y")
    val y = "T2"
    println(s" in T2: y = $y")
  }

  class Base12 {
    initialOrder += 1
    println(s"Base12 initialOrder:$initialOrder")
    println(s" in Base12: b = $b")
    val b = "Base12"
    println(s" in Base12: b = $b")
  }

  class C12 extends Base12 with T1 with T2 {
    initialOrder += 1
    println(s"C12 initialOrder:$initialOrder")
    println(s" in C12: c = $c")
    println(s" in C12: z = $z")
    var z: Int = 4
    val c = "C12"
    println(s"in C12: z = $z")
    println(s"in C12: c = $c")
  }
}

