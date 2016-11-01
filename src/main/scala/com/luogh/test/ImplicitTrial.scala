package com.luogh.test

/**
  * @author luogh
  */

/**
  * 隐式参数
  */
class ImplicitTrial {
  def printImplicit(implicit str: String): Unit = println(str)
}

object ImplicitTrial {
  implicit val strVal = ImplicitTrial.getClass.getName
//  implicit def str: String = ImplicitTrial.getClass.getName
}

object AnotherClass {
  implicit val strVal2 = this.getClass.getName
}

/**
  * 隐式转换函数
  */
object ImplicitMethodTrial {
  implicit def int2String(num: Int): String = "ImplicitMethod Trial"
}

class ImplicitMethodTrial {
  def implicitMethod(num: String): Unit = println(num)
}

trait Row {
  def getInt(key: String): Int

  def getDouble(key: String): Double

  def getString(key: String): String
}

case class InvalidColumnException(key: String) extends RuntimeException(s"invalid column $key .")

class JRow(val rep: Map[String, Any]) extends Row {

  private def get[T](key: String): T = rep.getOrElse(key, throw new InvalidColumnException(key)).asInstanceOf[T]

  override def getInt(key: String): Int = get(key)

  override def getDouble(key: String): Double = get(key)

  override def getString(key: String): String = get(key)
}

object JRow {
  def apply(pairs: (String, Any)*): JRow = new JRow(Map[String, Any](pairs: _*))
}

object Implicit {

  /**
    * 使用隐式转换类,可以给已经存在的类型动态增加新的方法，
    * 如下，此时JRow对象有了get[T]方法。并且通过隐式函数，
    * (JRow, String) => T ,可以通过定义某些T类型的隐式函数，
    * 比如 any2Int(),any2Double()函数，假如没有定义any2String
    * 隐式函数，那么就不能调用SRow.get[String]方法，编译会报错，
    * 提示，找不到(JRow, String) => String 隐式方法。
    *
    * 这样，通过隐式函数，可以约束 T 的类型。
    *
    * @param jRow
    */
  implicit class SRow(jRow: JRow) {
    def get[T](key: String)(implicit func: (JRow, String) => T): T = func(jRow,key)
  }

  implicit def any2Int(jRow: JRow, key: String): Int = jRow.getInt(key)

  implicit def any2Double(jRow: JRow, key: String): Double = jRow.getDouble(key)

  implicit def any2String(jRow: JRow, key: String): String = jRow.getString(key)

}

/**
  * 使用implicitly()方法获取指定类型的隐式参数值
  *
  * @param list
  * @tparam A
  */
class ImplicitlyMethodTrial[A](list: List[A]) {

  def myListOrder1[B](f: A => B)(implicit ordering: Ordering[B]): List[A] = list.sortBy(f)(ordering)

  /**
    * implicitly()方法结合类型Context-Bound,简化代码，获取隐式值
    *
    * @param f
    * @tparam B
    * @return
    */
  def myListOrder2[B: Ordering](f: A => B): List[A] = list.sortBy(f)(implicitly[Ordering[B]])
}


/**
  * 隐式参数查找路径显示从当前scope中查找以及目标类型的伴生对象中查找，如果有的话，则使用。如果当前scope以及目标类型的伴生
  * 对象同时存在隐式转换函数，那么使用当前scope的。
  */
case class Foo(s: String)
object Foo {
  implicit def fromString(s: String):Foo = Foo(s)
}

class O {
//  import Implicit1._  // 如果不显示导入Implicit1中隐式转换，那么将会去目标类型Foo的伴生对象中查找相应的隐式转换
  def m1(foo: Foo) = println(foo)
  def m(s: String) = m1(s)
}

object Implicit1 {
  implicit def fromString2(s: String):Foo = Foo(s"$s,this is ")
}
object App {

  /**
    * 相同类型的隐式参数在当前上下文中只能出现一个，如果同时import ImplicitTrial.strVal 和 AnotherClass.strVal2两个隐式参数，
    * 编译器无法识别，编译报错。
    * ambiguous implicit values:
    * both value strVal in object ImplicitTrial of type => String
    * and value strVal2 in object AnotherClass of type => String
    * match expected type String
    * def main(args: Array[String]): Unit = new ImplicitTrial().printImplicit
    */
  def implicitVariable(): Unit = {
    import ImplicitTrial._
    //    import AnotherClass._
    new ImplicitTrial().printImplicit
  }


  def implicitMethod(): Unit = {
    import ImplicitMethodTrial._
    new ImplicitTrial().printImplicit(2)
  }

  def implicitlyMethod(): Unit = {
    println(new ImplicitlyMethodTrial(List(1, 2, 4)).myListOrder1(_.toString))
    println(new ImplicitlyMethodTrial(List(1, 2, 4)).myListOrder2(_.toString))
  }

  def implicitClass(): Unit = {
    val row = JRow("A" -> 1, "B" -> 2.0, "C" -> "TEST")
    val a1 = row.getInt("A")
    val a2 = row.getDouble("B")
    val a3 = row.getString("C")

    println(s"$a1 -> $a2 -> $a3")
    import Implicit._
    val b1 = row.get[Int]("A")   // 指定类型参数T为Int
    val b2 = row.get[Double]("B")
    val b3 = row.get[String]("C")

    println(s"$b1 -> $b2 -> $b3")

    val b4: Int = row.get("A")   // get[T] 通过变量声明Int类型，编译器推导出类型 T 为Int类型
    val b5: Double = row.get("B")
    val b6: String = row.get("C")

    println(s"$b4 -> $b5 -> $b6")
  }

  def main(args: Array[String]): Unit = {
    implicitVariable()
    implicitMethod()
    implicitlyMethod()
    implicitClass()

    new O().m("11")
    val list = List(1,2,3)
    val conf = conforms[Int]
    val imp = implicitly[Int <:< Int]
    val imp2 = implicitly[Int <:< AnyVal]
    println(1)
  }
}


