package com.luogh.scala.test.scalaimpatient

/**
 * @author Kaola
 */
class GenericTypeExcecise {

}

/**
 * 1.定义一个不可变类Pair[T,S]，带一个swap方法。返回组件交换过位置的新对偶
 */
class Pair08[T,S](val t:(T,S)) {
  def swap()=new Pair07(t._2,t._1)
}

/**
 * 2.定义一个可变类Pair[T]，带一个swap方法，交换对偶中组件的位置
 */
class Pair09[T](var t:(T,T)){
  def swap()=(t._2,t._1)
}

/**
 * 3.给定类Pair[T,S]，编写泛型方法swap,接受对偶作为参数并返回组件交换过位置的新对偶
 */
class Pair10[T,S]{
  def swap[T,S](t:(T,S))=(t._2,t._1)
}

/**
 * 4.给定可变类Pair[S,T]，使用类型约束定义一个swap方法，当类型参数相同的时候可以被调用
 */

class Pair11[S,T](var a:S,var b:T) {
  def swap(implicit env: S =:= T,env2:T=:=S)={
    var tmp = a 
    a = b
    b = tmp
  }
  
  override def toString=a+","+b
}

object mainClass18 extends App {
  val p = new Pair09(1 -> 2)
  println(p.swap)
  println(p.t)
  
  val p1 = new Pair10
  p1.swap((1,2))
  println(middle("World"))
  
  val c = new Pair11(1,12)
  c.swap
  println(c)
  
  val e = new Pair11(1,"f")
  //val f = e.swap 无法编译
  
  /**
    * 4.编写一个泛型方法middle，返回任何Iterable[T]的中间元素，比如middle("World")应得到'r'
    */
  def middle[T](t:Iterable[T])=t.toList((t.size-1)/2)
}