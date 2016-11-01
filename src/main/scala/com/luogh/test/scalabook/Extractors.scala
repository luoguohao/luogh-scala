package com.luogh.test.scalabook

/**
  * 提取器
  * ref: http://udn.yyuap.com/doc/guides-to-scala-book/chp1-extractors.html
  *
  * 提取器用于模式匹配，可以解构各种数据解构，包括列表、流以及样例类。
  * 提取器使用最为广泛的使用有着与构造器相反的效果：构造器从给定的参数列表中创建一个对象，
  * 而提取器确实从传递给他的对象中提取出构造该对象的参数。
  *
  * 样例类比较特殊，scala会自动为其创建一个伴生对象:一个包含了apply和unapply方法的单例对象。
  * apply方法用来创建样例类的实例，而unapply方法需要被伴生对象实现，使其成为提取器。
  * @author luogh 
  */
object Extractors {
  //////////////////////////////////////////////////////////////////////////////////
  /////////////////////////// 提取器提取单个元素  ///////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////

  trait User {
    def name:String
  }
  class FreeUser(val name:String) extends User
  class PremiumUser(val name:String) extends User

  object FreeUser {
    def unapply(user:FreeUser):Option[String] = Some(user.name)
  }
  object PremiumUser {
   def unapply(user:PremiumUser):Option[String] = Some(user.name)
  }

  FreeUser.unapply(new FreeUser("test"))
  // 一般不直接调用unapply()方法，因为用于提取器模式时，scala会隐式的调用提取器的unapply方法。
  val user:User = new PremiumUser("test")
  user match {
    case FreeUser(name) => println("Hello"+name)
    case PremiumUser(name) => println("Welcome back,"+name)
  }

  ////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////// 提取器提取多个值 /////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////
  trait User2 {
    def name:String
    def score:Int
  }
  class FreeUser2(val name:String,val score:Int,val upgradeProbability:Double) extends User2
  class PremiumUser2(val name:String,val score:Int) extends User2

  object FreeUser2 {
    def unapply(u:FreeUser2):Option[(String,Int,Double)] = Some((u.name,u.score,u.upgradeProbability))
  }
  object PremiumUser2 {
    def unapply(u:PremiumUser2):Option[(String,Int)] = Some((u.name,u.score))
  }

  val user2:User2 = new FreeUser2("test",3000,3.0d)
  val result = user2 match {
    case FreeUser2(name,_,p) => if(p > 0.75) s"$name,what can we do for you today ?" else s"Hello,$name"
    case PremiumUser2(name,_) => s"Welcom back,$name"
  }
  println("result:"+result)

  ////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////// 布尔提取器 //////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////

  /**
    * 有些时候，进行模式匹配并不是为了提取参数，而是为了检查其是否匹配。可以使用另外一种unapply
    * 方法签名,该方法接受 S 类型的对象，返回一个布尔值: def unapply(object:S):Boolean
    * 使用的时候，如果这个提取器返回true,模式匹配成功，否则，scala会尝试拿object匹配下一个模式。
    */
  object premiumCandicate {
    def unapply(user:FreeUser2):Boolean = user.upgradeProbability > 0.75
  }
  object premiumCandicate_2 {
    def unapply(user:FreeUser2):Boolean = user.score > 200
  }

  /**
    * 提取器不一定非要在这个类的伴生对象中定义。
    * 使用的时候，只需要把一个空的参数列表传递给提取器,因为它并不真的需要提取数据，自然也没必要
    * 绑定变量。
    *
    * Scala的模式匹配也允许将提取器匹配成功的实例绑定到一个变量上，这个变量有着与提取器所接受的
    * 对象相同的类型。这通过 @ 操作符实现。 premiumCandicate 接受 FreeUser对象，因此变量 freeUser
    * 的类型也就是FreeUser.
    *
    * 布尔提取器使用的并没有那么频繁
    */
  val user3:User2 = new FreeUser2("TEST",2500,0.68d)
  user3 match {
    case freeUser @ premiumCandicate() => println("freeUser:"+freeUser)
    case premiumCandicate_2() => println("premium:"+user3)
    case _ => println("other:"+user3)
  }


  /////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////// 中缀表达方式 ///////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////////

  /**
    * 解构列表、流的方法与创建他们的方法类似，都使用cons操作符: ::、#::
    * Scala允许以中缀方式来使用提取器。所以，可以写成e(p1,p2),也可以写成 p1 e p2,
    * 其中e 是提取器，p1,p2是要提取的参数。
    * 同样，中缀操作方式的 head #:: tail 可以被写成 #::(head,tail),提取器PremiumUser 可以这样
    * 使用 name PremiumUser score.
    */

  val xs = 58 #:: 43 #:: 93 #:: Stream.empty
  xs match {
    case first #:: second #:: _ => first - second  // 等价于case #::(first,#::(second,_)) => first - second
  }

  //////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////// 序列提取器 //////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////////

  /**
    * scala 提供了提取任意多个参数的模式匹配方法，可以匹配只有两个、或者只有多个，也可以使用通配符
    * _* 匹配长度不确定的列表，提取器可以接受某一类型的对象，将其解构为列表，且这个列表的长度在
    * 编译器是不确定的。 def unapplySeq(object:S):Option[Seq[T]]
    */
  object GivenNames {
    def unapplySeq(name:String):Option[Seq[String]] = {
      val names = name.trim.split(" ")
      if(names.forall(_.isEmpty)) None
      else Some(names)
    }
  }

  def greetWithFirstName(name:String) = name match {
    case GivenNames(firstName,_*) => println(s"Good moring ,$firstName")
    case _ => println("welcome ,please make sure to fill in your name")
  }

  greetWithFirstName("Danial")

  /**
    * 固定和可变的参数提取
    * 有时候，需要提取出至少多个值，这样在编译期，就必须知道要提取出几个值出来，再外加一个可选的
    * 序列，用来保存不确定的那部分。
    * def unapplySeq(object:S):Option[(T1,...,Tn-1,Seq[T])]
    * unapplySeq返回的依然是Option[Tuple[N]],不过最后一个元素是一个Seq[T]
    */
  object Names {
    def unapplySeq(name:String):Option[(String,String,Seq[String])] = {
      val names = name.split(" ")
      if(names.size<2) None
      else Some((names.last,names.head,names.drop(1).dropRight(1).toSeq))
    }
  }

  def greet(fullName:String) = fullName match {
    case Names(lastName,firstName,other @ _ *) => println(s"$lastName $firstName with ${other}")
    case _ => println("welcome ! please make sure to fill in your name")
  }

  greet("ahs asd  asdfsdfasdfasdf adsf")

  def main(args:Array[String]):Unit = {
    Extractors
  }
}
