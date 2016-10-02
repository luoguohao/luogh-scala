package com.luogh.scala.test.scalaimpatient

import java.awt.Color

import com.luogh.scala.test.SimpleTrial.IT

/**
 * @author Kaola
 */
class patternMatcherAndCaseClassTest {
  val charT = 't'
  
  def switchTest(ch:Char,color:Color): Unit={
    var sign = ch match {
      case '+' =>  1
      case '-' => -1
      case  _  =>  0
    }
    
    var col = color match {
      case Color.RED => "RED"
      case Color.BLUE => "blue"
      case Color.YELLOW => "yellow"
      case _ => ""
    } 
    //添加守卫
    var digit = ch match {
      case '+' => "asd"
      case '-' => -1
      case _ if Character.isDigit(ch) => Character.digit(ch, 10)
      case _ => 0
    }
    
    //模式中的变量,变量必须以小写字母开头，为了将常量表达式比如:scala.math.Pi区分
    //如果有一个以小写字母开头的常量，需要将他放置在反引号中
    val   str = "thisisatestcanyousee"
    import scala.math.Pi
    import java.io.File._
    var digit2 = str.map( _ match {
      case Pi => 2  //表示的是scala.math.Pi
      case `charT`=> 4
      case `pathSeparatorChar` => 3
      case '+' => 1
      case '-' => -1
      case c if Character.isDigit(c) => Character.digit(c, 10)
      case _ => 0
    })
    
    
    /**
     * 类型模式，对表达式的类型进行匹配
     */
    
    val matchResult = digit match {
      case x:String => x
      case x:Int => x +1
      case _:BigInt => Int.MaxValue //匹配任何类型为BigInt的对象
      case BigInt => -1 //此处的BigInt表示 常量:scala.math.BigInt伴生对象
      case y:Class[_] => -1 //匹配类型为Class对象
      case _ => 0
    }
    
    /**
     * 特殊匹配测试
     */
    val clz = classOf[BigInt]
    import scala.reflect.runtime.universe._
    val typz = typeOf[BigInt] //返回的是 type BigInt = scala.math.BigInt
    val seq = Seq(clz,typz,BigInt,BigInt(100))  
  
    seq.foreach { 
      _ match {
        case BigInt => println("is BigInt object")//BigInt匹配该选项
        case x:BigInt => println("is BigInt instance") //BigInt(100)匹配该选项
        case x:Class[_] => println(x.getName)  // clz匹配该选项
        case x:Array[Int] => println("") //可以匹配
        //case x:Map[String,Int] => //不允许的
        case x:Map[_,_] => println("") //ok
        case _ => println("not match") // typz匹配该选项
      }
    }
    
    //匹配发生在运行期，Java虚拟机中泛型的类型信息是被擦掉的。因此不能使用类型来匹配特定的Map类型
    //case m:Map[String,Int] => ... 不允许的
    //可以匹配一个通用的映射:case m:Map[_,_] => ... OK
    //但是，对于数组而言，元素的类型信息是完好的，可以匹配到Array[Int]
  }
  
   /**
     * 匹配数组
     */
  def matchArrayTest()={
    val arr = Array(0)
    arr match {
      case Array(0) => println("Array(0)") //只有一个元素0的数组
      case Array(x,y) => println("x="+x+" y="+y) //只有两个元素的数组
      case Array(1,_*) => println("1 ...") //第一个元素为1的数组
      case _ => println("Somethingelse")
    }
  }
  
  /**
   * 匹配列表
   */
  def matchListTest()={
    val lst = List(2,2,4,5)
    lst match {
      case 0::Nil => println("List(0)") //只有一个元素0的列表
      case x::y::Nil => println("x="+x+" y="+y) //只有两个元素的列表
      case 1::t => println("1 ...") //第一个元素为1的列表
      case h::t => println("head is :"+h+" tail is :"+ t)
      case _ => println("Somethingelse")
    }
  }
  
  /**
   * 匹配元组
   */
  def  matchTupleTest()={
    val tuple = (4,0)
    tuple match {
      //case x:(Int,Int) => println(x)
      case (0,_) => println("0 ...")
      case (y,0) => println("y is :"+y)
      case _ => println("something else")
    }
  }
  
  /**
   * 提取器，模式匹配是通过提取器机制(extractor)--带有从对象中提取值的unapply或unapplySeq方法的对象。
   * unapply方法用于提取固定数量的对象，而uapplySeq提取的是一个序列，可长可短
   * Array.unapplySeq(Array(1,2,3,45))  //sequence wrapped in a scala.Some, if x is a Seq, otherwise None
   */
  
  /**
   * 正则表达式也可以以适用于使用提取器的场景，如果正则表达式有分组，可以使用提取器来匹配每个分组：
   */
  val pattern = "([0-9]+) ([a-z]+)".r
  "99 botterns" match {
    case pattern(num,item) => println(num,item)  // pattern.unapplySeq("99 botterns")产生一系列匹配分组的字符串，这些值被赋给了变量num和item
    //在这里提取器并非是一个伴生对象，而是一个正则表达式对象
    case _ => println("not match")
  }
  
  /**
   * 变量声明中的模式
   */
  def patternInVar(){
    val (x,y) = (1,2) //同时把x定义为1，把y定义为2
    val (q,r) = BigInt(10) /% 3 //求商和余数
    println(x,q)
    val Array(first,second,_*) = Array(1,2,34,23)
    println(first,second)
  }
  
  /**
   * for表达式中的模式
   */
  def patternInFor(){
     import scala.collection.JavaConversions.propertiesAsScalaMap
    for((k,v) <- System.getProperties) println(k,v)
    for((k,"") <- System.getProperties) println(k) //失败的匹配会安静的忽略，这里只匹配value值为空的
    for((k,v) <- System.getProperties if v == "") println(k) //使用守卫
  
  }
  
  /**
   * 样例类测试
   */
  def caseClassTest(amt:Amount_01)= {
   val result =  amt match {
                        case Dollar_01(v) => "$"+v
                        case Currency_01(v,u)=> "v:"+v+" and u:"+u
                        case RMB_01(v,u,s) => "v:"+v+" and u:"+u+" and s:"+s
                        case RMB_01(v) => "v:"+v
                        case Euro_01(v) => "v"
                        case Nothing => ""
                    }
    
    println(result)
  }
  
  /**
   * 样例类中的copy方法和带名参数
   */
  def copyMethodAndNamedVarient()= {
    val amt = Currency_01(11,"$")
    val price = amt.copy() //该方法本身不是很有用，因为Currency_01对象 是不可变的，我们完全可以共享这个对象引用。
    //但是可以使用带名参数来修改某些属性
    val price_01 = amt.copy(value=12) //Cucrency_01(12,"$")
  }
  
  /**
   * case语句中的中置表示法
   * 
   * 
   * 1.如果unapply方法产生一个对偶，则可以在case语句中使用中置表示法。
   * 尤其是对有两个参数的样例类
   * 
   * amt match { case a Currency_01 u => ... } //等同于case Currency_01(a,u)
   * 
   * 2.主要用在：对于List对象，要么是Nil，要么是样例类 :: ,定义如下：
   *  case class ::[E](head:E,tail:List[E]) exntends List[E]
   *  因此可以写成：
   *  lst match {case h :: t => ... } //等同于case ::(h,t) => ... ,将调用 ::.unapply(lst)
   *  
   *  或：
   *  
   *  result match { case p ~ q => ...} //等同于调用 case ~(p,q) => .. ,将调用 ~.unapply(lst)
   *  当把多个中置表达式放在一起的时候，他会更易读。
   *  result match { case p ~ q ~ r => ...} // 等同于调用 ~(~(p,q),r)
   *  这样的写法好过与  ~(~(p,q),r) => ...
   *  
   *  3.如果操作符以冒号结果，则他说从右到左结合的。比如
   *  case first :: second :: rest
   *  表示 ： case ::(first ::(second,rest))
   *  
   *  4.中置表示法可用于任何返回对偶的unapply方法。
   *  case object +: {
   *    def unapply[T](input:List[T]) = {
   *      if (input.isEmpty) None else Some(input.head,input.tail)
   *    }
   *  }
   *  
   *  这样可以用+:来析构列表了：
   *  
   *  1 +: 2 +: 3 +:4 +: Nil match {
   *    case first +: second +: rest => first +second +rest.length
   *  }
   */
  
  case object !: {
      def unapply[T](input:List[T]) = {
         if (input.isEmpty) None else Some(input.head,input.tail)
       }
  }
  
  /**
   * 中置表达法
   */
  def interExpress()= {
      (1 :: 2 :: 3 :: Nil) match {
        case first !: second !: rest => println(first,second,rest.length)
        case _ => println("not match")
      } 
  }
  
  /**
   * 样例类匹配嵌套结构
   */
  def caseClassWithNestingStructure()= {
    val items = Bundle("father`s day special",20.0,
            Article("scala for Impatient",32.23),
            Bundle("Another distillery sampler",10.0,
              Article("Old potrero straight rye whisky",33.32),
              Article("JuniperGino",323.2)
            )             
        )
     
     items match {
        case Bundle(_,_,Article(desc,_),_*) if desc == "" => println(desc)
        case Bundle(_,_,Article(desc,_),_*) if desc == "scala" => println("the first Article desc :"+desc)
        //可以使用@表示法将嵌套的值绑定到变量。art是第一个Article,rest是剩余Item的序列
        case Bundle(_,_,art @ Article(desc,_), rest @ _*) => println("the first Article`s price is  :"+art.price +" and rest Article number is :"+rest.length)
        case _ => println("not match Bundle")
      }
    
    //作为该特性，的实际应用，可以计算某个Item价格的函数
    
    def price(it:Item):Double = it match {
      case Article(_,p) => p
      case Bundle(_,disc,its @ _*) => its.map { price _ }.sum -disc
      case Multiple(p,its @ _*) => its.map { price _ }.sum + p
      case Noth => 0
    }
    
  }
  
  def matchColor(color : TrafficLightColor) =
    color match {
      case Red => "stop"
      case Yellow => "hurry up"
      case Green => "go"
    }
  
  /**
 * 偏函数，指被包含在花括号内的一组case语句是一个偏函数 ---一个并非对所有输入值都有定义的函数。
 * 他是PartialFunction[A,B]类的一个实例。（A是参数，B是返回值）
 * 该类有两个方法，apply方法从匹配到的模式计算函数值，而isDefineAt方法在输入至少匹配其中一个模式
 * 时返回true.
 * 
 * GenTraversable特质的collect方法将一个偏函数应用到所有在该偏函数有定义的元素，并返回
 * 包含这些结果的序列
 * "-3+4".collection{case '-' => 1 ; case '+' => -1 } //Vector(1,-1)
 */
  
  def partialFunctionTest()= {
    val f:PartialFunction[Char,Int] = {case '+' => 1 ;case '-' => 2}
    println(f('-')) //调用f.apply('-'),返回2
    println(f.isDefinedAt('-')) //true
    println(f.isDefinedAt('@')) //true
    println(f('@')) //抛出MatchError
  }
}

sealed abstract class Item
case class Article(description:String,price:Double) extends Item
case class Bundle(description:String,discount:Double,items:Item*) extends Item
case class Multiple(price:Double,items:Item*) extends Item
case object Noth extends Item
/**
 * 样例类，他们经过优化以被用于模式匹配,他们适用于那种被标记不会改变的结构。因为如果经常改变，那么match语句经常需要重写。
 * 此时，使用样例类不合适。比如
 * Scala的List是使用样例类实现的。即列表本质上就是：
 * abstract class List
 * case object Nil extends List
 * case class :: (head:Any,tail:List) extends List
 * 在声明样例类的时候，有如下几件事会自动发生：
 *  1.构造器中的每个参数都称为val--除非他们显示声明为var
 *  2.在伴生对象中提供apply方法让你不用new关键字就能构造出相应的对象。
 *  3.提供unapply、unapplySeq方法让模式匹配可以工作
 *  4.将生成toString、equals/hashCode、copy方法 --除非显式给出这些方法的定义
 *  
 *  除上述之外，样例类和其他类完全一样。可以添加方法和字段，扩展他们
 *  
 *****************************************************
 * 
 * 当使用样例类做模式匹配的时候，希望编译器能够帮你确保你已经列出了所有可能的选项。
 * 要达到此效果，需要将样例类的超类申明为sealed:即称为 密封类
 */

sealed abstract class Amount_01
case class Dollar_01(value:Double) extends Amount_01
case class Currency_01(value:Double,unit:String) extends Amount_01
case class Euro_01(value:Double) extends Amount_01
/**
 * 可以针对单例的样例对象
 */
case object Nothing extends Amount_01

/**
 * 自定义apply方法和unapplySeq方法
 */
class RMB_01(val value:Double,val unit:String) extends Amount_01 {
  private var src:String = _
  
  def this(value:Double,unit:String,src:String){
    this(value,unit)
    this.src = src
  }
}

object RMB_01 {
    def apply(value:Double,unit:String,src:String)={
      new RMB_01(value,unit,src)
    }
    def apply(value:Double,unit:String)={
      new RMB_01(value,unit,null)
    }
    
      
    def unapplySeq(x: RMB_01):Option[Seq[_]] = {
       if(x==null) None else Some(Seq(x.value,x.unit,x.src))
    }
}


/**
 * 使用样例类模拟枚举类型,见matchColor方法
 */
sealed abstract class TrafficLightColor
case object Red extends TrafficLightColor
case object Yellow extends TrafficLightColor
case object Green extends TrafficLightColor


/**
 * 标准库中的Option类型使用样例类表示那种可能存在、也可能不存在的值。
 * 样例子类Some包装了某个值，Some("Fred")，而样例对象None表示没有值
 * Map的get方法返回一个Option
 * Map().get("a") match {
 *    case Some(score) => println(score)
 *    case None => println("nothing")
 * }
 */




object mainClass15 extends App {
  val obj01 = new patternMatcherAndCaseClassTest
  obj01.matchArrayTest()
  obj01.matchListTest()
  obj01.matchTupleTest()
  obj01.caseClassTest(new Dollar_01(10))
  obj01.caseClassTest(new Currency_01(10,"#"))
  obj01.caseClassTest(RMB_01(10,"#","$"))
  obj01.caseClassTest(Nothing)
  obj01.interExpress()
  obj01.caseClassWithNestingStructure()
  //obj01.partialFunctionTest()
  val list = List(Teacher("teacher"),IT("LUOGUO"),"ASASD",11)
  list.collect(partialFunctionTest("tes")).foreach(println _)
  println(partialFunctionTest("test").isDefinedAt(11)) // false
  println(partialFunctionTest("test").isDefinedAt(IT("luogh"))) //true
  println(partialFunctionTest("test").applyOrElse(11,(x:Int)=>12)) //12
  
  val p = Per("name")
  matching(p)
  def matching(p:P){
    p match {
      case Per(a) => println(a)
      case Per(a,b) => println(a+","+b)
      case p:Per => println(p.name+":"+p.value+":"+p.src)
    }
  }
  abstract class P
  class Per(val name:String,val value:String) extends P {
     var src:String=_
  }
  
  object Per {
     def apply(name:String,value:String)={
      new Per(name,value)
    }
    def apply(name:String,value:String,src:String)={
      val p = new Per(name,value)
      p.src = src
      p
    }
    def apply(name:String)={
      new Per(name,null)
    }
     
    def unapplySeq(x: Per):Option[Seq[String]]= {
      if(x==null) None else Some(Seq(x.name,x.value,x.src))
    }
  }

  def partialFunctionTest(key:Any):PartialFunction[Any,Any] = {
    //scala模式匹配允许将提取器匹配成功的实例绑定到一个变量上，这个变量有着和
    //提取器所接受的对象相同的类型，通过@操作符实现。
    case e @ IT(name) => println(s"IT NAME:${name}") ; e
    case e: Teacher => e
    case e:String => e
  }

  sealed abstract class Career(name:String)
  case class IT(name:String) extends Career(name)
  case class Teacher(name:String) extends Career(name)
  case class Farmer(name:String) extends Career(name)
}