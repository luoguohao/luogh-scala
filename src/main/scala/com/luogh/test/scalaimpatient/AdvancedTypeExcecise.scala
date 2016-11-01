package com.luogh.test.scalaimpatient

/**
 * @author Kaola
 */
class AdvancedTypeExcecise {
  /**
   * 实现一个Bug类，对沿着水平线爬行的虫子建模，move方法向当前方向移动，
   * turn方法让虫子转身，show方法打印出当前的位置，让这些方法可以被串接调用
   */
  trait Moveable[T] {
    this:Bug =>
    val currPosition:Position[T]
    def move(step:T):this.type
    def turn():this.type
    def show():this.type
    
    trait Position[T]{
      var x:T
      var y:T
      def move(step:T):Unit 
      def turn():Unit
    }
  
  }
  
  class Bug extends Moveable[Int]{
    
    val currPosition:Position[Int] = new Position_(0,0)
    def move(step:Int):this.type = {
      currPosition.move(step)
      this
    }
    def turn():this.type = {
      currPosition.turn
      this
    }
    def show():this.type = {
      println("current position is: x= "+currPosition.x+" y="+currPosition.y)
      this
    }
    
    class Position_(var x:Int,var y:Int) extends Position[Int] {
       def move(step:Int):Unit = {
         this.x += step
         this.y += step
       }
       def turn():Unit = {
         this.x = -x
         this.y = -y
       }
    }
  }
}


/**
 * 2.实现被嵌套在Network类中的Member类的equals方法。两个成员要相等，必须属于同一个网络
 */
class N {
  class M {
    var t:String = _
    override def equals(other: Any) = {
      other match {
      case that: N.this.M => true
        case that: N#M => false
        case _ => false
      }
    }
  }
}


object Exc {
  /**
   *   val succ = (x: Int) => x + 1
       val anonfun1 = new Function1[Int, Int] {
          def apply(x: Int): Int = x + 1
       }
       assert(succ(0) == anonfun1(0))
   */
  def printValues(target:{def apply(i:Int):Int},from:Int,toS:Int) {
    (from to toS).map(target(_)).foreach(println)
  }
}


abstract class Dim[T](val value:Double,val name:String) {
  this:Dim[T] with T =>  //等价于this:T  自身类型结合复合类型
  protected def create(v:Double):T
  def +(other:Dim[T]) = {create(value + other.value)}
  override def toString() = value +" "+name
}

class Seconds(v:Double) extends Dim[Seconds](v,"s") {
  def create(v: Double): Seconds = {
    new Seconds(v)
  }
}

class Meters(v:Double) extends Dim[Meters](v,"m") { 
  def create(v: Double): Meters = {
    new Meters(v)
  }
}


trait A {
  println("A")
  def sing() = "from a"
}
trait C {
  this:A=>
println("C")
    val w = sing +"from c"
    println("w:"+w)
}
class B {
  this:C =>
println("B")
    val k = w
}
object mainClass22 extends App{
  val obj = new AdvancedTypeExcecise
  val bug = new obj.Bug
  bug.move(1).move(2).turn.show
  
  val n = new N
  val n1 = new N
  val m = new n.M
  val m1 = new n1.M
  println(m equals m1)
  
  Exc.printValues((x:Int)=>x*x, 3, 6) //9,36
  Exc.printValues(Array(1,2,3,4,5,6,7,8,9), 3, 6) //4,5,6,7
  val b = new B with C with A
  println(b.k)
}