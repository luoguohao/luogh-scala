package com.luogh.scala.test.scalaimpatient

/**
 * @author Kaola
 * 操作符
 */
class OperationSympolTest(var n:Int,var d:Int) {
  def this() {this(0,0)}
  def test = {println("n++++=:"+n+" and d :"+d)}
  
  def *(other:OperationSympolTest) = {new OperationSympolTest(n*other.n,d*other.d)}
  
  def unary_~ = {new OperationSympolTest(-n,-d)}
  
  def update(n:Int,multi:Int){
    this.n = n*multi
  }
}


object OperationSympolTest {
  def apply(n:Int,d:Int)={
    new OperationSympolTest(n,d)
  }
  
  //提取器
  def unapply(input:OperationSympolTest):Option[(Int,Int)] = {
    if(input.n == 1) None else Some((input.n,input.d))
  }
}

//String的提取器
object Name {
  def unapply(input:String)={
    val pos = input.indexOf(" ")
    if(pos == -1) None
    else Some((input.substring(0,pos),(input.substring(pos+1))))
  }
}

//带单个参数或无参数的提取器,如果unapply方法要提取单值，他应该返回一个目标类型的Option
object Number {
  def unapply(input:String):Option[Int]={
    try {
    	Some(Integer.parseInt(input.trim))
    } catch {
      case ex : NumberFormatException => None
    }
  }
}

object IsCompound {
  def unapply(input:String) = input.contains(" ")
}

//unapplySeq方法，提取任意长度值的序列，返回一个Option[[Seq[A]]]
object Name01 {
  def unapplySeq(input:String):Option[Seq[String]] = {
    if(input.trim() == "") None else Some(input.trim.split("\\s+"))
  }  
}

//样例类，自动具备apply和unapply方法
case class Currency(value:Double,unit:String)


object mainClass11 extends App {
  
  //使用反引号来包含任何的字符序列
  val `val` = 43 ; Thread.`yield`()
  
  val obj01 = new OperationSympolTest(1,2).test _  // 这里添加的下划线，此时的obj01不是test方法的返回值，而是代表该函数本身
  obj01()
  
  //中置操作符(infix)  ： a 标识符 b   --> 标识符代表一个带有两个参数的方法（一个隐式参数和显示参数）
  val obj02 = 1 to 3 
  val obj03 = 1 -> 3  //元组
  println("tuple_1: "+obj03._1 + "  tuple_2: "+obj03._2)
  
  //自定义操作符
  (new OperationSympolTest(1,2)*new OperationSympolTest(3,4)).test  //(3,8)
  
  //一元操作符 (a 标识符   此时为后置标识符（postfix） ; 
  //标识符 a  此时为前置标识符(prefix),  +，-，!,~可作为前置操作符，被转换为unary_操作符的方法调用)
  
  (~ new OperationSympolTest(1,2)).test  //等同于new OperationSympolTest(1,2).unary_~() ,输出 print n:-1 and d :-2 
  
  
  //apply方法和update方法
  OperationSympolTest(2,3).test  //调用伴生对象的apply方法。
  val obj04 = OperationSympolTest(4,5)
  obj04(12) = 2 //该方法相当于调用OperationSympolTest中的update(n,multi)方法。即f(arg1,arg2,....) = value 等同于调用f.update(arg1,arg2,...,value) 
  obj04.test  //打印 24,5
  
  //提取器
  var obj05 = OperationSympolTest(4,5) * OperationSympolTest(0,4)
  obj05 match {
    case OperationSympolTest(0,20) => println("======================match")
    case _ => println("======================not match")
  }
 
  
  //String的提取器
  val Name(first,last) = "luo guohao" // 调用Name.unapply("luo guohao")
  
  println(first,last) 
  
  //数字提取
  //val Number(b) = "1s2"
  //println("b = "+b)
  //提取是否包含空格
  val author = "luo guohaolindaer"
  author match {
    case Name(first,last @ IsCompound()) => println("match!!" + (first->last))  //last 包含空格，才能匹配
    case Name(first,last) => println("match2!!")
  }
  
  val author02 = "luo van der test "
  author02 match {
    case Name01(first,last) => println(first,last)
    case Name01(first,second,last) => println(first,second,last)
    case Name01(first,"van","der",last) => println("match!!!!!!!!!!!!" + first + " ==> "+ last)
    case _ => println("not match")
  }
}

