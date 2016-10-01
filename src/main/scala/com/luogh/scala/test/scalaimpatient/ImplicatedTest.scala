package com.luogh.scala.test.scalaimpatient

import java.io.File
import java.awt.Point



/******************************************************************************
 * 隐式转换（implicit conversion）和隐式参数
 * 
 *  1.隐式转换用于在类型之间做转换
 *  2.必须引入隐式转换，并确保他们可以以单个标识符的形式出现在当前作用域中。
 *  3.隐式参数列表会要求制定类型的对象。他们可以从当前作用域中以单个标识符定义的
 *    隐式对象获取，或者从目标类型的伴生对象获取。
 *  4.如果隐式参数是一个单参数的函数，那么它同时也会被作为隐式转换使用
 *  5.类型参数的上下文界定要求存在一个指定类型的隐式对象
 *  6.如果有可能定位到一个隐式对象，这一点可以作为证据证明某个类型转换是合法的。
 *****************************************************************************/

 class Fraction(val n:Int,val b:Int){
   def  * (c:Fraction)={
    new Fraction(n*c.n,b*c.b)
  }
}
  object Fraction{
    def apply(n:Int,b:Int)={
      new Fraction(n,b)
    }
   /******************************************************************************
    * 隐式转换函数使用source2Target的命名方式
    *****************************************************************************/
    implicit def int2Fraction(n:Int) = Fraction(n,1)
    
    implicit def frac2Orderd(frac:Fraction): Ordered[Fraction]= new Ordered[Fraction]{
       def compare(that: Fraction): Int = frac.n.compareTo(that.n)
    }
  }

  
 /******************************************************************************
  * 利用隐式转换丰富现有类库的功能
  *****************************************************************************/
 class RichFile(val from:File) {
   import scala.io.Source
   def read = Source.fromFile(from.getPath).mkString
 }
 object RichFile {
   implicit def file2RichFile(from:File)=new RichFile(from)
 }
 
 /******************************************************************************
  * 引入隐式转换
  *   scala会考虑如下的隐式转换函数：
  *       1.位于源或目标类型的伴生对象中的隐式转换函数
  *       2.位于当前作用域可以以单个标识符指代的隐式函数  
  *       
  *  在REPL中，键入:implicits以查看所有除Predef外被引入的隐式成员或者键入:implicit -v以查看全部
  *****************************************************************************/
  
 
 /******************************************************************************
  * 隐式转换规则
  *   隐式转换在以下三种不同的情况下会被考虑：
  *     1.当表达式的类型和预期的类型不同时：
  *         sqrt(Fraction(1,4)) //将调用fraction2Double ,因为sqrt预期是一个Double
  *     2.当对象访问一个不存在的成员时：
  *         new File("").read //将调用file2RichFile,因为File没有read方法
  *     3.当对象调用某个方法时，而该方法的参数声明和传入参数不匹配时：
  *         3 * Fraction(4,5) //将调用int2Fraction,因为Int的*方法不能接受Fraction作为参数
  *   有三种情况编译器不会尝试使用隐式转换：
  *     1.如果代码能在不使用隐式转换的前提下通过编译，则不会使用隐式转换。比如：
  *       如果a*b能够编译，那么编译器不会尝试a*convert(b)或者convert(a)*b
  *     2.编译器不会尝试同时执行多个转换，比如convert1(convert2(a))*b
  *     3.存在二义性的转换是个错误。比如:convert1(a)*b 和convert2(a)*b都是合法，编译器会报错
  *     上述二义性规则只适用于被尝试转换的对象：比如：
  *       Fraction(3,4) * 5
  *       虽然如下两个都是合法的：
  *         Fraction(3,4) * int2Fraction(5)
  *       和
  *         fraction2Double(Fraction(3,4))*5
  *       但是这里并不存在二义性。第一个转化将会胜出，因为它不需要改变一个呗应用*方法的那个对象
  *****************************************************************************/
 
 
 /******************************************************************************
  * 隐式参数
  *   函数或方法可以带有一个标记为implicit的参数列表。这种情况下，编译器将会查找缺省值，提供给该函数或方法。
  *****************************************************************************/
 
 case class Delimiters(left:String,right :String)
 
 object FrenchPunctuation {
   //隐式参数
   implicit val quteDelimiters = Delimiters("<<",">>")
   
 }
 
 
 
 /******************************************************************************
  * 利用隐式参数进行隐式转换
  * 
  * 隐式的函数参数也可以被用做隐式转换。比如：
  * def smaller[T](a:T,b:T) = if(a<b) a else b //不太对劲
  * 这个不行。编译器不能接受这个函数，因为它并不知道a和b属于一个带有<操作符的类型
  * 可以提供一个转换函数达到目的：
  *****************************************************************************/
 
 class SmallerTest {
   //def smaller[T](a:T,b:T) = if(a<b) a else b
   
   //注意order是一个带有单个参数的函数，被打上了implicit标签，并且有一个
   //以单个标识符出现的名称。因此他不仅是一个隐式参数，他还是一个隐式转换。正因为
   //如此，可以在函数体中略去order的显示调用：
   def smaller[T](a:T,b:T)(implicit order: T=>Ordered[T])
        = if(a < b) a else b   //将调用order(a) < b ,如果a 没有带 < 操作符的话，此时order函数的作用既是隐式参数，也是隐式转换函数
   
   def test(){
          smaller(2,3)
          
          
          import Fraction._
          smaller(Fraction(12,2),Fraction(3,2))
        }
 }
 
class ImplicatedTest {
 //import RichFile._
   implicit def file2RichFile(from:File)=new RichFile(from)
  val result = 3 * Fraction(3,4)
  val str = new File("").read
  
  val convert:Fraction = 3
  
  /******************************************************************************
   * 排除某个隐式转换函数
   * 引入除double2Fraction外的所有成员
   *****************************************************************************/
  
  import FractionConversions.{double2Fraction=>_,_}
   
  def quote(what:String)(implicit delims:Delimiters)=delims.left + what + delims.right
  
  //可以使用一个显示的Delimiters对象来调用quote方法，
  quote("Bonjour le monde")(Delimiters("<<",">>")) //返回<<Bonjour le monde>>
  
  
  /*****************************************************************************
   * 也可以略去隐式参数列表：
   * 在这种情况下，编译器将会查找一个类型为Delimiters的隐式值。这必须是一个被声明为implicit的值。
   * 编译器将会在两个地方查找这样的对象：
   *    1) 在当前作用域所有可以用单个标识符指代的满足类型要求的val和def
   *    2) 与所要求类型相关联的类型的伴生对象中。相关联的类型包括所要求类型本身，以及
   *    他的类型参数(如果他是一个参数化类型的话)
   *    
   *  对于给定的数据类型，只能有一个隐式值。与隐式值的名称关系。因此，使用常用类型的隐式参数不是一个好主意
   *  例如：
   *  def quote(what:String)(implicit left:String,right:String) //别这样做
   *  上述代码行不通，因为调用者没法提供两个不同的字符串
   *  
   *****************************************************************************/
  import FrenchPunctuation._
  quote("Bonjour le monde")
}



/******************************************************************************
 * 上下文界定
 * 
 * 类型参数可以有一个形式为T:M的上下文界定。其中M是另一个泛型类型。它要求作用域中
 * 存在一个类型为M[T]的隐式值
 * 
 * 例如： class Pair[T:Ordering]  
 * 要求存在一个类型为Odering[T]的隐式值，该隐式值可以被用在该类的方法中。
 * 
 * class Pair[T:Ordering](val first:T,val second:T) {
 *    def smaller(implicit ord:Ordering[T])=
 *        if(ord.compare(first,second) < 0) first else second
 * }
 * 
 * 如果new一个Pair(40,2)，编译器推断出需要一个Pair[Int]，由于Predef作用域中存在
 * 一个类型为Ordering[Int]的隐式值，所以Int满足上下文界定。这个Ordering[Int]就成为该类的
 * 一个字段，被存入需要该值的方法中
 * 
 * 可以使用Predef类中的implicitly方法获取该值
 *****************************************************************************/

 class APair[T:Ordering](val first:T,val second:T) {
    def smaller(implicit ord:Ordering[T])=
         if(ord.compare(first,second) < 0) first else second
    
    def smaller2 = if(implicitly[Ordering[T]].compare(first, second) < 0) first else second
  }
 
  class APair_01[T](val first:T,val second:T) {
    def smaller(implicit ord:T => Ordered[T])=
         if(first < second) first else second
  }
  
  
  /*******************************************************************************
   * 可以利用Ordered特质中定义的从Ordering 到 Ordered的隐式转换。一旦引入了这个转换，就可以使用关系操作符
   * 
   * 这些只是细微的变化，重要的是可以随时实例化APair_03[T],只要满足存在 类型为Ordering[T]d的隐式值的
   * 条件即可。比如，需要使用一个APair_03[Point]，则可以组织一个隐式的Odering[Point]值：
   ******************************************************************************/
  
  class APair_03[T:Ordering](val first:T,val second:T) {
     def smaller = {
       import Ordered._
       if(first < second) first else second
     }
  }
  object APair_03 {
    
    //隐式值
    implicit object PointOrdering extends Ordering[Point] {
       def compare(x: Point, y: Point): Int = {
         x.x.compare(y.x)
       }
     }
  }
 
  
  /*********************************************************************************
   * 类型证明
   * T =:= U
   * T <:< U
   * T <%< U
   * 分别表示 T类型是否等于U类型。T是否是U类型的子类型。T类型是否可以被视图(隐式)
   * 转换为U类型。要使用这样的类型约束，需要提供一个隐式参数,如下
   * 
   * =:= 、<:< 、<%< 是带有隐式值的类，定义在Predef对象中,<:<从本质上是：
   * abstract class <:<[-From,+To] extends Function1[From,To]
   * 
   * object <:< {
   *    implicit def conforms[A] = new (A <:< A){ def apply(x:A) = x}
   * }
   ********************************************************************************/
  
  class TypeDefine {
    def firstLast[A,C](it:C)(implicit ev : C <:< Iterable[A])= (it.head,it.last)
  }
  
  
  
object mainClass26 extends App {
  import APair_03._
  val a = new APair_03[Point](new Point(1,2),new Point(1,2))
  val b = new TypeDefine()
  b.firstLast(List("as"))
}