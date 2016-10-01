package com.luogh.scala.test.scalaimpatient

/**************************************************************************
 * 类型投影
 * 
 * 嵌套类从属于包含他的外部对象
 *************************************************************************/
import scala.collection.mutable.ArrayBuffer
import java.awt.Rectangle
import java.awt.geom.Area
import java.awt.Point
import javax.swing.JComponent
import javax.swing.JFrame
import java.awt.event.ActionEvent

/**
 * @author Kaola
 * 高级类型
 *    1.单例类型可用于方法串接和带对象参数的方法
 *    2.类型投影对所有外部类的对象都包含了期内部类的实例
 *    3.类型别名给类型指定一个短小的名称
 *    4.结构类型等效于"鸭子类型"
 *    5.存在类型为泛型类型的通配参数提供了统一形式
 *    6.使用自身类型来表明某特质对混入他的类或对象的类型要求
 *    7."蛋糕模式"用自身类型来实现依赖注入
 *    8.抽象类型必须在子类中被具体化
 *    9.高等类型带有本身为参数化类型的类型参数
 */
class AdvancedTypeTest {
  
  /************************************************************************
   * 单例类型
   *    给定任何引用v,可以得到类型v.type,他可能有两个可能的值：v和null.
   *    
   * 作用一: 对于使用返回this的方法，可以通过这种方式将方法调用串联起来。如下：
   *    document.setTitle("scala impatient").setAuthor("luogh")  
   *    但是，当存在子类的时候，出现如下问题：
   * 
   *    book.setTitle("scala impatient").setAuthor("luogh").addChapter("advanced type") 
   *    
   *    无法编译通过，value addChapter is not a member of scalaimpatient.mainClass19.obj01.Document
   *    因为setTitle,setAuthor方法返回的是this,Scala将返回的类型推断为Document,但是Document没有addChapter方法。所以不能编译通过
   *    
   *    解决办法：将方法的返回类型声明为:this.type,这样返回的就是book.type,而由于book有addChapter方法，方法就可以串在一起了 。
   *  
   ************************************************************************/    
   class Document {
     def setTitle(title:String):this.type = {println("title:"+title);this}
     def setAuthor(author:String):this.type = {println("author:"+author); this}
   }
   
   
   class Book extends Document {
     def addChapter(chapter:String):this.type={println("chapter:"+chapter);this}
     def addPrice(p:Price){}  //接受Price类型,不接受object实例，object实例的类型是：Price.type
   /**************************************************************************
    * 作用二:接受单例对象而不是某个类型。方式如下:Price.type表示单例类型
    *************************************************************************/
     def addSinglePrice(p:Price.type):this.type={this}  //不接受Price类型,接受object实例，object实例的类型是：Price.type,返回的Book类型
   }
   
}
object Price
class Price
class Network {
  class Member(val name:String){
    val contacts = new ArrayBuffer[Member]
  }
  private val members = new ArrayBuffer[Member]
  
  def join(name:String) = {
    val m = new Member(name)
    members += m
    m
  }
  def fun(v:Network.this.Member){}
}

object mainClass20 extends App {
  //每个网络实例都有自己的Member类
  val chatter = new Network
  val t:chatter.type = ???
  val myFace = new Network
  //现在chatter.Member 和 myFace.Member是两个不同的类型。
  //不能将其中一个网络的成员添加到另一个网络：
  val fred = chatter.join("Fred")
  val barney = myFace.join("barney")
  //fred.contacts += barney 编译不通过。fred.contacts接收的类型是chatter.Member,而barney的类型是myFace.Member
  
  chatter.fun(fred)
  //chatter.fun(barney)  编译不通过，类型不匹配
  
  def fun(v:chatter.Member){}
  //def fun1(v:Network.Member){}  //编译不通过。
  def fun1(v:Network#Member){}  //编译通过。
  
  fun(fred)
  //fun(barney) 编译不通过
  fun1(fred)
  fun1(barney)
  
  
  /***************************************************************************
   * 解决方法一：将Member类放到Network外面，可以放在Network的伴生对象中。
   * 如果就是希望这样，但是只是偶尔希望这样，那么可以用"类型投影" Network#Member,意思是任何Network的Member类型。因为：chatter.Member <:< Network#Member == true。即为子类型的关系
   * 
   * 此时：
   * 
   * class Network{
   *    class Member(val name:String){
   *      val contacts = new ArrayBuffer[Network#Member]
   *    }
   *    ...
   * }
   **************************************************************************/
}

class Network1 {
  private val members = new ArrayBuffer[Network1.Member]
  def join(name:String) = {
    val m = new Network1.Member(name)
    members += m
    m
  }
}
object Network1 {
  class Member(val name:String){
        val contacts = new ArrayBuffer[Member]
       }
}

/****************************************************************************
 * 路径
 * 
 * 考虑如下类型：com.horstman.impatient.chatter.Member （chatter是Network实例）
 * 或者将Member嵌套在伴生对象中：com.horstman.impatient.Network.Member 这个表达式称为路径
 * 在最后的类型之前，路径的所有组成部分必须是"稳定的"，也就是它必须指定到单个、有穷的范围。组成部分必须是以下的一种:
 *    包
 *    object对象
 *    val
 *    this、super、super[S]、C.this、C.super或C.super[S]
 *    如:def fun(v:Network.this.Member){}
 * 路径组成部分不能是类，因为，嵌套的内部类并不是单个类型。而是给每个实例都流出了各自独立的一套类型。
 * 因此，对于类似Network#Member这样的类型投影并不会被当做"路径"，你也无法引用他。
 * 同时，类型也不能是var。如：
 *  var chatter = new Network
 *  ...
 *  val fred = new chatter.Member  //错误，chatter不稳定，因为你可能将不同的值赋给chatter，编译器无法对类型chatter.Member做出明确的判断
 *  
 *  
 *  在内部，编译器将所有嵌套的类型表达式a.b.c.T都翻译成类型投影 a.b.c.type#T。比如，chatter.Member就成为chatter.type#Member --任何位于chatter.type单例中的Member.
 *  
 ***************************************************************************/

object mainClass21 extends App {
   val chatter = new Network1
  val myFace = new Network1
  val fred = chatter.join("Fred")
  val barney = myFace.join("barney")
  fred.contacts += barney  //编译通过
  
  var luo = new Network
  //var l = new luo.Member()  //编译不通过 ，stable identifier required, but mainClass21.this.luo found.
  
}


/*****************************************************************************
 * 类型别名 (type关键字)
 * 
 * 对于复杂类型可以使用type关键字创建简单的别名
 * 这样一来就可以用Booking.Index而不是类型scala.collection.mutable.HashMap[String,(Int,Int)]来表示
 * 类型别名必须嵌套在类或对象中。不能出现在scala文件的顶层。
 * 
 * type关键字同样被用于哪些在子类中被具体化的抽象类型
 ****************************************************************************/


class Booking {
  import scala.collection.mutable._
  type Index = HashMap[String,(Int,Int)]   //类型别名
  def fun(v:Index){
    
  }
}

 abstract class Reader{
  type Contents  //抽象类型，子类实现具体化
  def read(fileName:String):Contents
}
 class SubReader extends Reader{
  type Contents = String
   def read(fileName:String):Contents={
     "2"
   }
 }

 
 /*******************************************************************************
  * 结构类型,也称鸭子类型
  * 
  * 结构类型 指的是一组关于抽象方法、字段和类型的规格说明。这些抽象方法、字段、类型是满足该规格的类型
  * 必须具备的。
  * 比如以下的appendLines方法，可以对任意具备append方法的类调用appendLines方法。这比定义一个
  * Appendable特质更为灵活，因为你可能并不总是能将该特质添加到使用的类上。
  * 在幕后，scala使用反射来调用target.append方法，结构类型可以让你安全的做这样的反射调用
  ******************************************************************************/

 object ConstructType {
   def appendLines(target:{def append(str:String):Any},lines:Iterable[String]) {
     for(l <- lines) {
       target.append(l)
       target.append("\n")
     }
   }
 }
class Appender {
  def append(str:String):Any = {
    println(str)
  }
}
class Appender1 {
  def append(str:String,str2:Int):Any = {
    println(str)
  }
}



/*********************************************************************************
 * 复合类型
 * 
 * 复合类型的定义形式如下：
 *  T1 with T2 with T3 ...
 *  要想成为该复合类型的实例，某个值必须满足每一个类型的要求才行。这样的类型称作交集类型。
 *  
 *  可以用复合类型来操作哪些必须提供多个特质的值：
 *    val img = new ArrayBuffer[java.awt.Shape with java.io.Serializable]  //表示只能添加既是形状也可被序列化的对象。
 *    
 *  
 *  可以把结构类型的生命添加到简单类型或是复合类型。
 *  
 *  def add1(target:java.awt.Shape with java.io.Serializable {def contains(p:Point):Boolean}) {}
 *  表示该函数接受的类型必须是Shape子类型同时也是Serializable子类型，并且包含contains方法
 *  
 *  从技术上讲，如下结构类型:
 *  {def append(str:String):Any}
 *  是如下的简写：
 *  AnyRef {def append(str:String):Any}
 *  而复合类型：Shape with Serilizable
 *  是如下的简写：
 *  Shape with Serializable {}
 ********************************************************************************/

object ComplicatedType {
  def add(){
    val image = new ArrayBuffer[java.awt.Shape with java.io.Serializable]
    val rect = new Rectangle(1,2,2,3)
    image += rect
    //image += new Area(rect)  编译错误，Area是Shape但是不是Serializable
  }
  
  def add1(target:java.awt.Shape with java.io.Serializable {def contains(p:Point):Boolean}) {
  }
}


/***********************************************************************************
 * 中置类型
 *    中置类型是一个带有两个类型参数的类型，以中置的语法表示，类型名称写在两个类型参数之间。比如：
 *    可以将Map[String,Int] 写成 String Map Int 。这种写法在数学中用的较多.在scala中可以这样定义：
 *    type x[A,B] = (A,B),在此之后可以写成String x Int 而不是(String,Int)
 **********************************************************************************/
object interType {
  type x[A,B] = (A,B)  //中置类型
  def fun[A,B](e:A x B) {println(e._1); println(e._2)}
  def fun1() {
    val x = (1,2)
    fun(x)
  }
}

/*************************************************************************************
 * 存在类型
 * 
 * 存在类型主要是为了与java的类型通配符兼容。
 * 存在类型的定义方式是在类型表达式之后跟上forSome{...},花括号中包含了type和val的声明。比如：
 * Array[T] forSome { type T <: JComponent} 等同于 Array[_ <: JComponent]
 * 
 * scala的类型通配符只不过是存在类型的"语法糖":
 * Array[_] 等同于 Array[T] forSome { type T} 
 * Map[_,_] 等同于 Map[U,V] forSome { type U; type V}
 * 
 * forSome表示法可以使用更复杂的关系：
 * Map[T,U] forSome {type T; type U <:T}
 * 
 * 可以在forSome代码中使用val声明，因为val可以有自己的嵌套类型:
 * n.Member forSome{val n:NetWork}  <:<  Network#Member  为 true
 * 表示 某个类型为Network的对象的嵌套类型Member，而Network#Member表示任意Network对象的嵌套类型Member类型。 因此 是 Network#Member的子类型.
 * 
 ************************************************************************************/

object ExistType {
  def fun[T,V <:T](t:T,v:V){
     val array:Array[T] = ???
     val array1:Array[ _ <: JComponent] = ???
     val array2:Array[F] forSome { type F <: JComponent} = ???
     val b:n.Member forSome{val n:Network} = ???
  }
  
  /**
   * 该方法接受相同网络的成员，但拒绝来自不同网络的成员
   */
 def process[M <: n.Member forSome {val n:Network}](m1:M,m2:M) = (m1,m2)
 def process1[M <: Network#Member](m1:M,m2:M) = (m1,m2)
 
 type NetworkMember = n.Member forSome {val n:Network}
 def process2(m1:NetworkMember,m2:NetworkMember) = (m1,m2)
 
 def testFun() {
   val chatter = new Network
   val myFace = new Network
   val fred = chatter.join("Fred")
   val wilma = chatter.join("Wilma")
   val barney = myFace.join("Barney")
   process(fred,wilma)
   //process(fred,barney) //编译错误，fred与barney的类型不一样，分别为chatter.Member,myFace.Member,因此取他们的共同的父类型:scalaimpatient.Network#Member;
                          //inferred type arguments [scalaimpatient.Network#Member] do not conform to method process's type parameter bounds [M <: n.Member forSome { val n: scalaimpatient.Network }]
                          // 类型：n.Member forSome { val n: scalaimpatient.Network } 是 Network#Member 的子类型
                          // 因为：typeOf[a.B forSome {val a:A }] <:< typeOf[A#B]  结果返回true
   process1(fred,barney) //编译成功
   process2(fred,barney) //编译成功
 }
}



/****************************************************************************************
 * 自身类型(self type)
 * 
 * 在特质中，可以要求混入它的类必须要扩展自另一个类型。定义如下：
 * this:类型 =>
 * 这样的特质只能被混入给定类型的子类中去。
 * 如果想要多个类型的要求，可以用复合类型:
 * this: T with U with ... =>
 * 
 * 
 ***************************************************************************************/
trait Logged_1 {
  def log(msg:String){}
}

trait LoggedException_ extends Logged_1 {
  this:Exception =>
    def log() {
      log(getMessage())  //可以调用getMessage方法,因为this是一个Exception
    }
}
class myException extends Exception {
  
}

//可以将自身类型的语法结合用于包含this的别名的语法共同使用：
trait Group {
  outer:Network =>
    class Member{}
}

//自身类型不会自动继承。需要重复自身类型的声明：
trait ManagedException extends LoggedException_ {
  this:Exception =>
}

object SelfType {
  def fun(){
    //val f = new JFrame with LoggedException 编译不通过，JFrame的自身类型不是Exception的子类型
	  val f = new myException with LoggedException_ 
  }
}


/******************************************************************************************
 * 抽象类型(abstract type)
 * 
 * 类或特质可以定义一个在子类中被具体化的抽象类型
 * 
 * 抽象类型可以有类型界定，就和类型参数一样。
 *****************************************************************************************/
trait Listener_ {
  type Event <: java.util.EventObject
}

//子类必须提供一个兼容的类型
trait ActionListener_ extends Listener_{
  type Event = java.awt.event.ActionEvent //ok，这是一个子类型
}




/****************************************************************************************
 * 家族多态
 * 
 * 设计一个管理监听器的通用机制，首先使用泛型类型，之后切换到抽象类型
 * 在java中，每个监听器接口有各自不同的方法名称对应事件的发生：actionPerformed,stateChanged,itemStateChanged等。
 * 我们可以将这些方法统一起来
 ***************************************************************************************/
trait Listener[E]{
  def occurred(e:E):Unit
}

//事件源需要一个监听器的集合，和一个触发这些监听器的方法
trait Source[E,L<:Listener[E]] {
  private val listeners = new ArrayBuffer[L]
  def add(l:L){ listeners += l}
  def remove(l:L){ listeners -= l}
  def fire(e:E){
    for(l<-listeners) l.occurred(e)
  }
}

//考虑按钮触发动作事件
trait ActionListener extends Listener[ActionEvent] 

//Button类混入Source特质：
class Button extends Source[ActionEvent,ActionListener]{
  def click() {
    fire(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"click"))
  }
}

//ActionEvent将事件源设置为this，但是事件源的类型为Obejct,我们可以用自身类型让他可以使类型安全的：
trait Event[S] {
  var source:S = _
}

trait Listener_v2[S,E <: Event[S]]{
  def occurred(e:E):Unit
}

trait Source_v2[S,E <: Event[S],L <: Listener_v2[S,E]] {
  this:S =>
    private val listeners = new ArrayBuffer[L]
    def add(l:L) { listeners += l}
    def remove(l:L) {listeners -= l}
    def fire(e:E) {
      e.source = this //这里需要自身类型,用来将事件源设为this,否则this只能是某种Source,而并不一定是Event[S]所需要的类型
      for(l <- listeners) l.occurred(e)
    }
}

class ButtonEvent extends Event[Button_v2]

trait ButtonListener_v2 extends Listener_v2[Button_v2,ButtonEvent]

class Button_v2 extends Source_v2 [Button_v2,ButtonEvent,ButtonListener_v2] {
  def click() {
    fire(new ButtonEvent)
  }
}

//可以看到类型参数扩张的很厉害。如果用抽象类型，会好些：
trait ListenerSupport {
  type S<: Source
  type E <: Event
  type L <: Listener
  
  trait Event {
    var source:S =_
  }
  trait Listener {
    def occurred(e:E):Unit
  }
  trait Source {
    this:S =>
      private val listeners = new ArrayBuffer[L]
      def add(l:L) { listeners += l}
      def remove(l:L) { listeners -=l }
      def fire(e:E) {
        e.source = this
        for(l <- listeners) l.occurred(e)
      }
  }  
}

//但这些有代价，就是你不能拥有顶级的类型声明，这就是所有代码包含哎ListenerSupport特质里的原因
//接下来，当想定义一个带有按钮事件和按钮监听器的按钮时，可以将定义包含在一个扩展该特质的模块中：
object ButtonModule extends ListenerSupport {
  type S = Button
  type E = ButtonEvent
  type L = ButtonListener
  
  class ButtonEvent extends Event
  trait ButtonListener extends Listener
  class Button extends Source {
    def click() {fire(new ButtonEvent)}
  }
}

//如果要用这个按钮，必须引入该模块
object Main {
  import ButtonModule._
  def main(args:Array[String]){
    val b = new ButtonModule.Button
    b.add(new ButtonListener{
        override def occurred(e:ButtonModule.ButtonEvent){ println(e)}
    })
    b.click
  }
}


/****************************************************************************************
 * 高等类型
 * 
 * 表示依赖于依赖其他类型的类型的类型
 ***************************************************************************************/


/**************************************************************************************
 * 综合测试
 *************************************************************************************/
object mainClass19 extends App {
  
  val obj01 = new AdvancedTypeTest
  
  var document = new obj01.Document
  document.setTitle("scala impatient").setAuthor("luogh")  
  
  var book = new obj01.Book
  var c = book.setTitle("scala impatient")  //this.type此时返回的类型是:obj01.Book
  c.setAuthor("luogh").addChapter("advanced type").addPrice(new Price()) //OK
  //c.setAuthor("luogh").addChapter("advanced type").addPrice(Price) //类型不匹配， found : scalaimpatient.Price.type required: scalaimpatient.Price
  c.setAuthor("luogh").addChapter("advanced type").addSinglePrice(Price)
  
  val book1 = new Booking
  val booking = new book1.Index
  book1.fun(booking)
  
  ConstructType.appendLines(new Appender, List("ADSAD"))
}