package com.luogh.test

/**
  * @author luogh 
  */
package grantfather {
  package father {
    class Father(val name:String ,age:Int)
    package brother {
      class Uncle(val name:String)
    }
    class Test()
  }

  package grantfather.father.children {
    import com.luogh.test.grantfather.father._
    class Child(val name:String,father: Father,uncle:brother.Uncle)
    class Child2(val name:String,test:Test)
  }
}


object PacakgeScope {
   import grantfather._
   new father.Father("father",45)
   new father.brother.Uncle("uncle")

  def test[T](t:T,f:T=>String): String = {
    f(t)
  }

  def test1[T](t:T)(f:T=>String): String = {
    f(t)
  }

  def test2[T](t:T,x:T): Unit = {
    println(s"$t,$x")
  }

  def main(args:Array[String]):Unit = {
//    println(test(12,x=>s"$x +11")) // compile failed,missing parameter type
    println(test[Int](12,x=>s"$x +11")) // compile success
    println(test1(12)(x=>s"$x +11"))
    test2(12,"a")
    val fun:Int=>String = {x:Int => "test"}
    println(test(12,fun))
  }

}
