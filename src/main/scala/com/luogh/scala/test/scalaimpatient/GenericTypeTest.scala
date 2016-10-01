package com.luogh.scala.test.scalaimpatient

import java.io.File
import scala.collection.mutable.ListBuffer

/**
 * @author Kaola
 * 
 * 类型界定的语法包括:
 *  T <: UpperBound 、T >: LowerBound 、T <% ViewBound、T : ContextBound
 *  可以用类型约束来约束一个方法，比如：(implicit ev:T <:< UpperBound)
 *  用 +T (协变)来表示某个泛型类的子类型关系与参数T方向一致，或用 -T (逆变)来表示方向相反：
 *    比如： List[+T] 表示 如果 T的子类型为S,那么对于协变来说，List[S] 同样是 List[T]的子类型。
 *         List[-T] 表示，如果T的子类型为S ，那么对于逆变来说，List[S] 则是 List[T]的父类型。
 *    协变和逆变表示的是泛型类的子类型与泛型参数的子类型的对应关系。
 *   
 *   同java一样。遵循PECS（producer extends consumer super）原则。
 *   协变适用于表示输出的类型参数，比如不可变集合中的元素. 此时类似于java中的 extends 作为Producer
 *   逆变适用于表示输入的类型参数，比如函数参数 。 此时类似于java中的super 作为Consumer
 */


class GenericTypeTest {
  def getMiddle[T](a:Array[T]) = a(a.length/2)
}


class Pair[T,S](val first:T,val second:S)
class Person01
class Student extends Person01
class SenorStudent extends Student


/**
 * 类型变量界定
 */
/*
 * 添加上界，T只能是Comparable[T]的子类型。这样compareTo方法才能编译
 */
class Pair01[T <: Comparable[T]](val first:T,val second:T) {
  def smaller = if(first.compareTo(second) <0) first else second
}



/**
 * 添加下界，假定我们有一个Pair[Student],我们应该允许用一个Person来替换第一个组件，
 * 当然这样做得到的结果是一个Pair[Person],通常而言，替换进来的必须是一个原类型的超类型
 */
class Pair02[T](val first:T,val second:T) {
  //如果没有上界界定，那么他将会返回Pair[Any]
  def replaceFirst[R >: T](newFirst:R) = new Pair02(newFirst,second)
}




/**
 * 视图界定
 * 上一个实例
 * class Pair[T <: Comparable[T]]
 * 当试着new一个Pair(4,2)的时候，编译器会说Int不是Comparable[Int]的子类行。和java.lang.Integer包装类型不一样。scala中的Int类型
 * 并没有实现Comparable。不过RichInt实现了Comparable[Int],同时还有一个从Int到RichInt的隐式转换。
 * 因此解决办法是使用视图界定：
 * class Pair[T <% Comparable[T]] , <% 意味着T可以别隐式转换成Comparable[T]
 */
class Pair03[T <% Comparable[T]](val first:T,val second:T) {
  def smaller = if(first.compareTo(second) <0) first else second
}



/**
 * 用Ordered特质会更好，应为他在Comparable的基础上提供了额外的关系操作符：
 * 因为java.lang.String实现了Comparable[String]，但没有实现Ordered[String].
 * 有了视图界定，字符串可以被隐式的转换成为StringOps,而StringOps是Ordered[String]的子类型。
 */
 class Pair04[T <% Ordered[T]](val first:T,val second:T) {
     def smaller = if(first < second) first else second
 } 


 /**
  * 上下文界定
  *   视图界定T <% V 要求必须存在一个从T 到 V的一个隐式转换。 上下文界定的形式为 T : M ，其中M是另一个泛型类。
  *   它要求必须存在一个类型为M[T]的隐式值
  *   例如：
  *     class Pair[T : Ordering]
  *   表示要求必须存在一个类型为Ordering[T]的隐式值，该隐式值可以用在该类的方法中。当你声明一个使用隐式值的方法时，
  *  需要添加一个"隐式参数"
  */
 class Pair05[T : Ordering](val first:T,val second:T) {
   def smaller(implicit ord:Ordering[T]) = {
     if(ord.compare(first,second) < 0) first else second
   }
 }
 
 class Pair15[T : Ordered](val first:T,val second:T) {
   def smaller(implicit ord: T=> Ordered[T]) = {
     if(first < second) first else second
   }
 }
 
 
 
 /**
  * Manifest上下文界定
  *   要实例化一个泛型的Array[T],我们需要一个Manifest[T]对象。要想让基本类型的数组正常工作，这个是必须的。如果T是Int,你会希望
  *   虚拟机中对应的是一个int[]数组。在Scala中，Array只不过是类库提供的一个类，编译器并不会对他做特殊处理。如果要编写一个泛型函数
  *   来构造泛型数组的话，需要传入这个Manifest对象来帮忙。由于它是构造器的隐式参数，可以使用上下文界定。
  *   
  *   如果调用makePair(4,9)，编译器将定位到隐式的Manifest[Int]并实际上调用makePair(4,9)(intManifest).这样一来，该方法调用的
  *   就是new Array(2)(intManifest),返回基本类型的数组int[2]
  *   
  *   在虚拟机中，泛型相关的类型信息是被抹掉的。只会有一个makePair方法，处理所有类型T
  */
 class Pair06 {
   def makePair[T:Manifest](first:T,second:T)={
     val r = new Array[T](2) //cannot find class tag for element type T
     val t = Vector[T]() //ok
     r(0) = first
     r(1) = second
     r
   }
 }
 
 
 /**
  * 多重界定
  * 
  * 类型变量可以同时有上界和下界。
  *   T >: Lower <: Upper
  * 可以要求一个类型可以实现多个特质：
  *   T <: Comparable[T] with Serializable with Cloneable
  * 可以有多个视图界定：
  *   T <% Comparable[T] <% String
  * 可以有多个上下文界定：
  *   T : Orderring : Manifest
  */
 
 
 
 /**
  * 类型约束
  *   类型约束是提供给你的另一个限定类型的方式。总共有三种关系可供使用:
  *   T =:= U
  *   T <:< U
  *   T <%< U
  *   这些约束将会测试T是否等于U,是否为U的子类型。或能否被视图(隐式)转化为U，
  *   要使用这样一个约束，你需要添加"隐式类型证明参数"：
  *     class Pair[T](val first:T,val second :T)(implicit ev: T <:< Comparable[T])
  *     
  *    类型约束让你可以在泛型类中定义只能再特定条件下使用的方法。
  *    class Pair[T](val first:T,val second:T) {
  *       def smaller(implicit ev: T <:< Ordered[T])=
  *         if (first < second) first else second
  *    }
  *    
  *    可以构造出Pair[File],尽管File并不带先后次序的，只有当你调用smaller方法时，才会报错
  *    
  *   val friends = Map("a"->"c","d"->"f")
  *   val friendOpt = friends.get("a") //Optiion[String]
  *   val friendOptOrNull = friendOpt.orNull //要么是String,要么是null,不过对于值类型，比如Int,他们并不把null看成是合法的值，因为orNull
  *                                      //的实现带有约束Null <:< A，你仍然可以实例化Option[Int]，但是当你对这些事例使用orNull就会报错。
  * 这个就是类型约束的好处之一
  * 
  * 类型约束的另一个用途是改进类型推断。
  * 
  * def firstLast[A,C <: Iterable[A]](it : C) = (it.head,it.last)
  * 
  * 当执行如下代码：
  *   firstLast(List(1,2,3))，你会得到消息，推断出的类型参数[Nothing,List[Int]]不符合[A,C <: Iterable[A]].
  *   为什么是Nothing? 类型推断器单凭List(1,2,3)无法推断出A是什么，因为他是在同一个步骤中匹配到的A和C，要解决这个问题，
  *   就必须首先匹配C，然后匹配A：
  *   def firstLast1[A,C](it:C)(implicit ev: C <:<Iterable[A]) = (it.head,it.last)
  */
 
 class Test02 {
   def firstLast[A,C <: Iterable[A]](it : C) = (it.head,it.last)
   def firstLast1[A,C](it:C)(implicit ev: C <:<Iterable[A]) = (it.head,it.last)
 }
 
 
 
/**
 * 型变
 * 
 * 有一个函数对Pair[Person]做某种处理:
 *    def makeFriends(p:Pair[Person])
 *    
 * 如果Student是Person的子类，那么默认情况下，Pair[Student]作为参数调用makeFriends是错误的。因为，尽管Student是Person的子类，但是
 * Pair[Student]与Pair[Persion]没有丝毫关系。
 * 
 * 如果想要这种关系，则必须在定义Pair类的时候表明这一点:
 *    class Pair[+T](val first:T,val second:T)
 *    1) + 意味着该Pair[T]类型是与类型T是协变的 --即Pair[T]与T 按相同的方向型变。 即，由于Student是Person的子类，那么Pair[Student]便是Pair[Person]
 *    的子类.
 *    
 * 也可以有另一个方向的型变。考虑泛型类型Friend[T]，表示希望与类型T的人称为朋友的人
 *    trait Friend[-T] {
 *        def befriend(someone:T)
 *    }
 * 
 * 假定有一个函数：
 *    def makeFriendWith(s:Student,f:Friend[Student]){f.befriend(s)}
 *    
 * 此时如果有：
 *    class Person extends Friend[Person]
 *    class Student extends Person
 *    val susan = new Student
 *    val fred = new Person
 *    
 * 此时函数调用makeFriendWith(susan,fred)此时会编译通过。因为此时Student是Person的子类,对于逆变，则Friend[Person] 是 Friend[Student]的子类
 * 所以，f:Friend[Student] = fred 成立。
 */
 
 class Pair07[+T](val first:T,val second:T)
 
 trait Friend[-T]{
   def befriend(someone:T)
 }
 
 class Person02 extends Friend[Person02] {
   def befriend(someone:Person02)= {}
 }
 class Student02 extends Person02 {
    def befriend(someone:Student02)= {}
 }
 
 object Friends {
   def makeFriendWith(s:Student02,f:Friend[Student02]){
       f.befriend(s)
   }
 }
 
 
 
 /**
  * 在一个泛型的声明中，可以同时使用这两种型变。比如：单参数函数的类型为：Function1[-A,+R]，至于为何这样声明，考虑如下函数：
  */
 object CovariantAndContravariance {
   
   def friends(students:Array[Student02],find:Function1[Student02,Person02]) {
     //第二个参数可以写成：find : Student02=>Person02
     for(s<-students) yield find(s)
   }
   
   def findStudent(p:Person02):Student02 ={
     new Student02
   }
   
   def test() {
     friends(Array(new Student02,new Student02),findStudent) 
     //因为Function1[-A，+R]:A表示函数参数类型，R表示函数返回类型。同时A逆变，R协变。
     //所以，在friends的类型为(Array[Student02],Function1[Student02,Person02])时,即使findStudent函数类型是Function1[Person02,Student02]
     //但是Function1的型变，此时Function1的参数是逆变的，因此Function1[Student02,R]可以接受子类型Function1[Person02,R],而Function1的返回值类型是协变的，
     //因此Function1[A,Person02]可以接受子类型Function[A,Student02]
   }
 }
 
 
 /**
  * 协变 和 逆变点
  * 
  * 函数在参数上是逆变的，在返回值上是协变的。通常来说，对某个对象消费的值适用逆变，而对于生产该对象的值适用协变。
  * 如果一个对象同时消费和产出某值，则该类型应该保持不变。
  * 这通常适用于可变数据结构。比如，scala的数组的不支持型变的。不能将Array[Student]转换成Array[Person],或者反过来。这是不安全的。
  * 
  * 在java中，可以将Student[]数组转换为Person[]数组，但是如果试着把非Student类的对象添加到该数组时，会抛出ArrayStoreException。在
  * Scala中，编译器会拒绝可能引发类型错误的程序通过编译
  */
 object CovarientAndControvarientPoint {
   def invarientTest() {
     val students = new Array[Student02](10)
     //val people : Array[Person02] = students  Array[]是不支持型变的，List[T]支持型变，但是是不可变的。即没有改变List里面的内容的操作，
                            //因此，能这样做。对于Array,如果可以这样做，
                          //之后可以对people(0) = new Person02("test") //此时students(0)不再是Student02类型了
     
     val people = new Array[Person02](10)
     //val student:Array[Student02] = people 
                         //同上，也是不能编译通过的，否则，people[0] = new Person02("")，此时student[0]变成了Person02类型
   }
 }
 
/**
 * 逆变点(contravarient position)
 * 
 * class Pair08[+T](var first:T,var second:T) {
 *    def func(arg:T){}
 * }
 * 
 * 如上所示，如果声明一个协变的可变类Pair08，发现不能编译通过，编译器提示：
 * 在first、second的setter方法中，协变的类型T 出现在了逆变点。
 *      ◾covariant type T occurs in contravariant position in type T of value first_=
 *      ◾covariant type T occurs in contravariant position in type T of value second_=
 *  
 *  first_= : (value:T) => Unit,参数位置是逆变点，而返回类型的位置为协变点。
 *  
 *  说明：
 *      
 *      假设以上定义成立。根据里氏替换原则，任意使用父类型的地方都可以用子类型替换。如下：
 *      val c:Pair08[Children] = new Pair08 (new Children,new Children)
 *      val p:Pair08[Parent] = new Pair08 (new Parent,new Parent)
 *      因为Pair08的类型参数是协变的,那么Pair08[Children]即为Pair08[Parent]的子类型，当用父类对象p调用函数func(Parent):
 *        p.func(arg:Parent)
 *      考虑里氏替换原则，任意使用父类型的地方都可以使用子类型来替换，那么此时使用c替换p,来调用func(arg:Parent)是不行的。因为
 *      c对象的函数func的方法签名为:(Children)=>Unit，此时c.func(arg:Parent)不能成立。因为Children是Parent的子类。c对象的
 *      func函数所能处理的范围比p对象所能处理的范围小，这是不能满足里氏替换原则的。所以编译不能通过。
 *      
 *      如果，将Pair08声明如下：
 *      class Pair08[-T](){
 *          def func(arg:T) {}
 *      }
 *      此时，是满足里氏替换原则的。即：
 *       val c:Pair08[Children] = new Pair08 (new Children,new Children)
 *       val p:Pair08[Parent] = new Pair08 (new Parent,new Parent) 
 *       因为，Pair08的类型参数是逆变，因此。Pair08[Children]变成了Pair08[Parent]的父类型。即在调用c.func(Children)的时候，可以
 *       使用子类型p替换父类型c,调用:  p.func(Children)，因为p的func的参数类型是Parent,所以在调用func(Children)时是成立的。
 *       所以，使用逆变是满足里氏替换原则
 *      
 * 
 */

 
 /**
  * 协变点(convarient position)
  * 
  * 方法的返回值是可以协变的。
  * class In[+A] {
  *     def fun():A = null.asInstanceOf[A]
  * }
  * 
  * 方法的返回值是不可以逆变的。
  * class In2[-A]{
  *     def fun1():A = null.asInstanceOf[A]
  * }
  * <console>:8: error: contravariant type A occurs in covariant position in type ()A of method fun1
  * 
  * 同样使用里氏替换原则，父类型：In[AnyRef]中的方法fun返回的类型是AnyRef，子类型In[String]的方法返回String,而任意父类型的引用都可以被子类型替换，那么
  * val parent = new In[AnyRef]
  * val children = new In[String]
  * val result:AnyRef = parent.fun() 
  * val result2:String = children.fun()
  * result = result2 //成立String是AnyRef的子类，这种引用关系是成立的
  * 
  * 如果In2的类型参数是逆变的。那么父类型In2[String]中的方法fun返回的类型是String,而子类型In2[AnyRef]的方法返回的是AnyRef，根据里氏替换原则，
  * val parent = new In2[String]
  * val children = new In2[AnyRef]
  * val result:String = parent.fun() 
  * val result2:AnyRef = children.fun()
  * result = result2 //不成立，AnyRef是String的父类型，这种将子类型引用指向父类型的引用关系是不能成立的
  */
 
 
 /**
  * 因此参数位置是逆变点，而返回类型的位置为协变点。
  * 但是在函数参数中，型变是反转过来的 -- 他的参数是协变的。比如，Iterable[+A]的foldLeft方法：
  *     foldLeft[B](z:B)(op: (B,A) => B) : B
  *                   -       + +     _    +
  *                   
  * 或者说如类It[+A]所示。函数fold的参数是函数类型(A)=>String,此时类型A是协变的但是出现了逆变点。
  *   
  */
 
 class It[+A] {
     def foldLeft[B](z:B)(op:(B,A)=>B):B = {???}
     /**
      * def fold[B](z:B)(op:A) 编译不通过,协变类型A出现了逆变点。covariant type A occurs in contravariant position in type A of value op
      * 解决办法是给这个方法加上另一个类型参数:
      */
     def fold[B,C>:A](z:B)(op:C) = {???}
     def fold(op:(A)=>String):Unit  = {}
 }
 
 
 /**
  * 对象不能是泛型 即
  * object Empty[T] extends List[T] //错误
  * 不能将参数化的类型添加到对象中。我们可以继承List[Nothing]
  * object Empty extends List[Nothing] 
  * Nothing类型是所有类型的子类型。因此对于List[Nothing]，根据协变的规则，可以被转为List[T]
  */
 
 
object mainClass17 extends App {
  val obj01 = new GenericTypeTest
  val p = new Pair(43,"String") //Pair[Int,String] ，scala会从构造参数中推断出实际类型
  val p2 = new Pair[Any,Any](43,"String") //也可以自己指定类型
  obj01.getMiddle(Array("mary","had","a","little","lamb")) //scala会在调用该方法的时候使用实际的参数来推断出类型。将会调用getMiddle[String]
  val f = obj01.getMiddle[String] _ //可以指定类型
  f(Array("String"))
  
  val obj02 = new Pair01("ss","asd")
  obj02.smaller
  
  val obj03 = new Pair01(new File(""),new File("")) // file也实现了Compaable特质
  //val obj04 = new Pair01(new Person(),new Person()) // Person未实现了Compaable特质，不能通过编译
  //val obj05 = new Pair01(1,2) 此时也无法通过编译，T = Int,界定T <: Comparable[T]无法满足，需要使用T <% ViewBound来解决
  
  val obj04 = new Pair02("ss","asd")
  val obj05 = obj04.replaceFirst(1) //此时的obj05的类型是Pair02[Any],1不是String的直接父类，他们有共同的父类Any
  val obj06 = new Pair02(new Student,new Student)
  val obj07 = obj06.replaceFirst(new Person01) //此时的obj07的类型是Pair02[Person01],Person01是Student的直接父类。
  val obj08 = obj06.replaceFirst(new SenorStudent) //此时的obj08的类型是Pair02[Student],SenorStudent是Student的子类。
  val obj09 = obj06.replaceFirst(new Student) //此时的obj09的类型是Pair02[Student]
  
  ///////////////////////////////由上可知，>: 上界约束 可以传递任意类型值 ////////////////////////////////////////////
  
  val obj10 = new Pair03(1,2)
  val obj11 = new Pair03("a","b")
  
  val obj12 = new Pair06 
  val obj13 = obj12.makePair(1,2)
  
  
  
  //////////////////////////类型约束 ////////////////////////////////////////////////////////////////////
  val obj14 = new Test02
  //obj14.firstLast(List(1,2,3)) 编译不通过
  obj14.firstLast1(List(1,2,3))
  
  
  /////////////////////////型变 ////////////////////////////////////////////////////////////////////////
  val susan = new Student02
  val fred = new Person02
  val pairs = new Pair07(susan,susan)
  
  /////////协变/////////////////
  val pairs2:Pair07[Person02] = pairs  //如果不使用协变，这个是无法成立的。即Pair07[Student02] 与 Pair07[Person02]没有任何关系。
  /////////逆变/////////////////
  Friends.makeFriendWith(susan, fred) //如果不使用逆变，makeFriendWith的签名是:(Student,Friend[Student]),而fred类型是Friend[Person],此时二者类型不能匹配，无法编译通过
  
  
  
  
  ///////////////////////////////////////函数参数类型的型变///////////////////////////////////////////////
  val parent = new It[Person02]
  val children = new It[Student02]
  val obj:It[Person02] = children
  val c:Function1[Function1[Any,String],Unit] = obj.fold _ //编译通过
  //val c:Function1[Function1[Student02,String],Unit] = obj.fold _ //此时编译不通过，说明函数参数的类型是协变的，而不是逆变
  val d = children.fold _
  c(func1 _)
  d(fun _)
  val fun01:Function1[Person02,String] = fun _
  val fun02:Function1[Student02,String] = fun _
  //val fun03:Function1[Any,String] = fun _  //此时编译不通过，说明函数本身的参数类型是逆变的。
  
  parent.fold(1)(new Student02)
  def fun(a:Person02):String={???}
  def func(a:Student02):String={???}
  def func1(a:Any):String={???}
}