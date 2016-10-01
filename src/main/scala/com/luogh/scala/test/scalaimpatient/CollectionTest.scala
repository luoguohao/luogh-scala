package com.luogh.scala.test.scalaimpatient

import java.awt.Color
import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import java.util.Collection

/**
 * @author Kaola
 * scala集合
 */
class CollectionTest {
  
}

object mainClass13 extends App {
  val obj01 = ArrayBuffer(1,2,3,4,5,6,6)
  val iter = obj01.iterator
  while(iter.hasNext) println(iter.next)
  
  val obj02 = Iterable(1,2,3)
  
//  val claz = classOf[String]
//  val d = 100
//  println(d.isInstanceOf[Int])
//  println( d.asInstanceOf[Int])
  
  Set(Color.BLACK)
  Map(Color.BLACK -> 100)
  
  val mp = Map(Color.BLACK -> 100)  //scala.collection.Map以及Predef.Map和scala.collection.imumtable.Map都是不可变映射
  import scala.collection.mutable.Map
  println(mp.isInstanceOf[Map[Any,Any]])  //false.即scala优先使用不可变集合。
  
  def digit(n:Int):Set[Int]={
    if(n<0) digit(-n)
    else if(n<10) Set(n)
    else digit(n/10)+n%10
  }
  
  val digits = List(4,2,4)
  println(digits.head + " and tail is :"+digits.tail+" and tail `s head is :"+digits.tail.head)
  
  // 1 :: List(2, 3) = List(2, 3).::(1) = List(1, 2, 3)  因为:: 操作符是右结合的，因此是List(2,3).::(1)，而不是1.::(List(2,3))，所以以:结尾的操作符都是右结合的。
  val listA = 1::List(2,3)
  val listB = 1::List("3","4")
  println(listA + " and listB :"+listB)
  
  
  //疑问一 ： >: 与 java泛型中的super下界有什么区别
  class Person[A]{
    def put[B <: A](p:B){  //B >:A 表示 B的下界是A,表示B至少是一个A类型； B <: A 表示B的上界是A表示B只能是A类型的子类型或A类型自身,不同于java中的extends 与 super
      println(p.getClass())
    }
    
    def put02[B >: A](p:B){  //B >:A 表示 B的下界是A,表示B至少是一个A类型； B <: A 表示B的上界是A表示B只能是A类型的子类型或A类型自身,不同于java中的extends 与 super
      println(p.getClass())
    }
  }
  
  class Animal {
    def print = println("Animal")
  }
  class Bird extends Animal{
     override def print = println("Bird")
  }
  class Magin extends Bird {
    override def print = println("Magin")
  }
  
  /**
   * 在java中，如下代码是编译不成功的：
   *  List<? super Bird> list = new ArrayLit<>();
   *  list.add(new Bird()); //编译成功
   *  list.add(new Magin()); //编译成功
   *  list.add(new Animal()); //编译不成功
   *  list.add(new String()); //编译不成功
   */
  
  val person = new Person[Bird]
  person.put(new Bird())
  person.put(new Magin())
  //person.put(new Animal())  //编译不通过//put方法接收的参数必须是Bird的子类型或Bird类型自身，因为B <: A，类似于java中的? extends A
  //person.put(new String())  //编译不通过 ,同上
  //person.put(1)   //编译不通过 ，同上
  
  person.put02(new Bird())
  person.put02(new Magin())
  person.put02(new Animal())  //编译通过//put方法接收的参数B类型，他的下界是A类型，此时B可以是任意类型，因为B >: A,不同于java中的? super A
  person.put02(new String())  //编译通过 ,同上
  person.put02(1)   //编译通过 ，同上
  
  
  
  
  var lista:List[Bird] =  new Bird() :: new Magin()  :: Nil
  
  (lista).foreach(x => println(x.getClass().getName))
  
  //在scala中使用递归来计算列表中的所有元素之和
  def sum(lst:List[Int]):Int = {
    if(lst.length==0) 0 
    else lst.head + sum(lst.tail)
  }
  
  //使用模式匹配也可以
  def sum02(lst:List[Int]):Int = {
    lst match {
      case Nil => 0
      case h::t => h + sum(t)
    }
  }
  
  //可变列表
  val lst =scala.collection.mutable.LinkedList(1,-2,7,0)
  var cu = lst
  while(cu != Nil){
    if(cu.elem < 0) cu.elem = -cu.elem
    cu = cu.next
  }
  
  println("------------------")
  //如下循环去除每两个元素的一个：
  var cur = lst 
  while(cur != Nil && cur.next != Nil){
    cur.next = cur.next.next
    println(cur.next)
    cur = cur.next
  }
  
  //把列表中的某个节点变成列表中的最后一个节点，不能将next引用设置为Nil,
  //而应该将他设置为LinkedList.empty,也不要将它设为null，不然会在
  //遍历链表是遇到空指针错误。
  val lst01 =scala.collection.mutable.LinkedList(1,-2,7,0)
  var cur01 = lst01
  while(cur01 != Nil){
    //设置-2为最后一个节点
    if(cur01.elem == -2) cur01.next = scala.collection.mutable.LinkedList.empty
    cur01 = cur01.next
  }
  println(lst01)
  
  println("--------Set----------")
  val set = Set(1,2,3,4,6)
  set.foreach { println _}
  println (Set(1,2) subsetOf set)
  
  println("--------添加或去除列表中元素的操作符----------")
  val array = Array(1,2,3,4)
  println((array :+ 1).mkString(",")) //将元素添加至尾部 //仅适用于Seq类型，比如Array,Quene,List
  println((1 +: array).mkString(",")) //将元素添加至头部
  
  val array1 = ArrayBuffer(1,2,3,4)
  println((array1 :+ 1).mkString(",")) //将元素添加至尾部 //仅适用于Seq类型，比如Array,Quene,List
  println((1 +: array1).mkString(",")) //将元素添加至头部
  
  val map = Map(1->"a",2->"b",3->"c")
  println((map+(4->"d",5->"e")).mkString(",")) //添加多个元素,仅适用于Set与Map类型。
  
  val set1 = Set(1,2,3,4)
  println((set1+(4,5,6)).mkString(",")) //添加多个元素
  
  println(array1 - (1,2)) //移除多个元素，仅适用于Set,Map，ArrayBuffer，不适用于Array,List
  println(set1 - (1,2)) //移除多个元素，仅适用于Set,Map，ArrayBuffer，不适用于Array,List
  println(map - (1,2)) //移除多个元素，仅适用于Set,Map，ArrayBuffer，不适用于Array,List
  
  println(array1 ++ ArrayBuffer(5,6)) //添加某个集合所有元素，适用于Iterable   ArrayBuffer(1, 2, 3, 4, 5, 6)
  println(set1 ++ ArrayBuffer(5,6))//添加某个集合所有元素，适用于Iterable      Set(5, 1, 6, 2, 3, 4)
  println(map ++ ArrayBuffer(5,6))//添加某个集合所有元素，适用于Iterable       ArrayBuffer((2,b), (1,a), (3,c), 5, 6)
  
  println("<<<<")
  println(array1 ++: ArrayBuffer(5,6)) //添加某个集合所有元素，适用于Iterable   ArrayBuffer(1, 2, 3, 4, 5, 6)
  println(set1 ++: ArrayBuffer(5,6))//添加某个集合所有元素，适用于Iterable      ArrayBuffer(1, 2, 3, 4, 5, 6)
  println(map ++: ArrayBuffer(5,6))//添加某个集合所有元素，适用于Iterable       ArrayBuffer((2,b), (1,a), (3,c), 5, 6)

  println("<<<<")
  
  println(array1 -- ArrayBuffer(1,2)) //移除某个集合所有元素，等价于set,map,arrayBuffer的diff方法  ArrayBuffer(3, 4)
  println(set1 -- ArrayBuffer(1,2))//移除某个集合所有元素，等价于set,map,arrayBuffer的diff方法       Set(3, 4)
  println(map -- ArrayBuffer(1,2))//移除某个集合所有元素，等价于set,map,arrayBuffer的diff方法      Map(3 -> c)
  
  println("<<<<")
  val list = List(1,2,3,4,5)
  println(1 :: list) //向前添加单个元素。 只适用于List
  println(List(1,2,3) ::: list) //向前添加多个元素 只适用于List
  println(List(1,2,3) ++: list) //向前添加多个元素
  
  
  println("<<<<")
  println(Set(5,9) union set) //联合并集  仅适用于Set
  println(Set(5,9) | set) //联合 并集  仅适用于Set
  println(Set(5,9) ++ set) //并集  仅适用于Set
    
  println(set  diff Set(1,2)) //差异，差集   仅适用于Set
  println(set  &~ Set(1,2)) //差异，差集   仅适用于Set
  println(set -- Set(1,2)) //差集 仅适用于Set
   
  println(set  intersect Set(1,2)) //交集  仅适用于Set
  println(set  & Set(1,2)) //交集  仅适用于Set
 
  println("<<<<")
  var list1 = ArrayBuffer(1,2,3,4,5)
  println(list1 += 1) //向后追加一个元素，通过添加或移除给定元素来修改集合 只适用于可变集合 ArrayBuffer(1, 2, 3, 4, 5, 1)
  println(list1 += (1,3,4,5)) //通过添加或移除给定元素来修改集合 只适用于可变集合  ArrayBuffer(1, 2, 3, 4, 5, 1, 1, 3, 4, 5)
  println(list1 ++= Array(1,3,4,5)) //通过添加或移除给定元素来修改集合 只适用于可变集合
  
  println(list1 -= 1) //移除首次出现1的位置，通过添加或移除给定元素来修改集合 只适用于可变集合
  println(list1 -= (1,3,4,5)) //通过添加或移除给定元素来修改集合 只适用于可变集合
  println(list1 --= Array(1,3,4,5)) //通过添加或移除给定元素来修改集合 只适用于可变集合
  
  println("<<<<")
  println(1 +=: list1) //通过向前追加给定元素或集合到某个集合中来修改该集合  ,只适用于ArrayBuffer  输出结果：ArrayBuffer(1, 2, 1, 3, 4, 5)
  println(Array(1,2,3,4) ++=: list1) //通过向前追加给定元素或集合到某个集合中来修改该集合  ,只适用于ArrayBuffer  输出结果：ArrayBuffer(1, 2, 3, 4, 1, 2, 1, 3, 4, 5)
  
  /**
   * 总结：一般而言，+用于将元素添加到无先后次序的集合中，而+:和:+则是将元素添加到右先后顺序的集合的头后者尾部。
   */
  
  println("<<<<")
  var number = List(1,2,3)
  number +:= 4;  //对于不可变集合，可以在var上使用+=或者:+=
  number :+= 4;  //对于不可变集合，可以在var上使用+=或者:+=
  println(number) //List(4, 1, 2, 3, 4)
  var number2 = Set(1,2,3)
  number2 += 4;  //对于不可变集合，可以在var上使用+=或者:+=
  println(number2)  
  var number3 = Vector(1,2,3)
  number3 :+= 1 //这里无法使用+=,因为向量和List没有+操作符
  number3 +:= 4 //这里无法使用+=,因为向量没有+操作符
  println(number3) //Vector(4, 1, 2, 3, 1)
  
  
  val names = List("Peter","Paul","Mary")
  names.map(_.toUpperCase()) //List("PETER","PAUL","MARY")
  //等价于 for(e <- names) yield e.toUpperCase
  
   println("<<<<")
  //如果函数产生的是一个集合而不是一个单值，可以使用flatMap将所有的
  //的值串联起来。
  def ulcase(elem:String)=Vector(elem.toLowerCase(),elem.toUpperCase())
  names.map {ulcase} //List(Vector("peter","PETER"),Vector("paul","PAUL"),Vector("MARY","mary"))
  names.flatMap {ulcase} //List("peter","PETER","paul","PAUL","MARY","mary")
  
  //collect方法用于偏函数(partial function)
  println("-3+4".collect{case '-' => 1; case '4'=>2})
  
  
  //二元函数组合集合中的元素
  val list01 = List(1,2,3,4,5)
  println(list01.reduceLeft(_ - _)) //((((1-2)-3)-4)-5)=-13
  /**
   * 结构如下：
   *              -
   *            /   \
   *           -     5
   *         /   \
   *        -     4         
   *      /   \
   *      -    3
   *    /   \
   *   1     2
   */
  println(list01.reduceRight(_ - _)) //(1-(2-(3-(4-5)))) = 3
  
  //以不同于集合首元素的初始元素开始计算，可以使用fold函数
  
  println(list01.foldLeft(0)(_ - _)) //(((((0-1)-2)-3)-4)-5) = -15
    /**
   * 结构如下：
   *              -
   *            /   \
   *           -     5
   *         /   \
   *        -     4         
   *      /   \
   *      -    3
   *    /   \
   *   -     2
   *  / \
   * 0   1
   */
  
  
  println(list01.foldRight(0)(_ - _)) //(1-(2-(3-(4-(5-0))))) = 3
  
  //foldLeft、foldRight的初始值和操作符是两个分开定义的"柯里化"参数，这样scala就可以使用
  //初始值的类型来推断出操作符的类型定义。比方说，List(1,2,3,4).foldLeft("a")(_ + _)，
  //初始值是一个字符串，因此操作符必定是一个类型定义为(String,Int)=>String的函数
  
  //可以使用/: 操作符表示foldLeft操作。使用:\表示foldRight操作。
  List(1,3,4).foldLeft(0)(_-_) //等价于(0 /: List(1,3,4))(_ - _)
  
  //假定我们需要计算某个字符串中字符出现的频率，使用for循环实现如下
  println("<<<<<<")
  val freq = Map[Char,Int]();
  for(c <- "misssage") freq(c) = freq.getOrElse(c, 0) + 1
  println(freq)
  //使用foldLeft实现如上操作
  
  "missage".foldLeft(scala.collection.mutable.Map[Char,Int]())((m,c) => m + (c -> (m.getOrElse(c, 0)+1))).foreach(println)
  
  //使用scanLeft，scanRight得到的是foldLeft和foldRight的所有中间结果
  (1 to 10).scanLeft(0)(_ + _).foreach(println)
  
  
  println("<<<<<<")
  /**
   * 流(Stream) : 作用类似于迭代器是相对于容器的一个"懒"的替代品，即只有在需要的时候才去取元素，
   * 如果不需要更多的元素，则不会付出计算剩余元素的代价，但是迭代器在每次调用next或其他方法方法的时候都会改变
   * 迭代器指向，这样就不是很灵活，因此，流提供的是一个而不可变的替代品，流是一个尾部被懒计算的不可变列表---
   * 即只有当你需要的时候他才会被计算
   */
  def numsFrom(n:BigInt):Stream[BigInt]={n #:: numsFrom(n+1)}
  //#:: 操作符构造一个流
  
  val tenOrMore = numsFrom(10) //Stream(10,?) ,其尾部未被求值
  println(tenOrMore)
  //流的方法是懒执行的，
  val squares = numsFrom(1).map(x=> x*x)
  println(squares) //Stream(1, ?)
  //调用squares.tail强制对下一个元素求值
  println(squares.tail) // Stream(4, ?)
  //获取更多内容
  squares(3) //获取第四个元素
  println(squares.take(5).force) //Stream(1, 4, 9, 16, 25)
  
  //squares.force 强制对所有值求值，可能引发OutOfMemoryError
  
  //可以使用迭代器构造一个流。Source.getLines方法返回Iterator[String]
  //用这个方法，对于每个行你只能访问一次，而流将缓存访问过的行，允许重新访问他
  val words = Source.fromFile("F://test.txt").getLines.toStream
  println(words) //Stream(A,?) //只会对第一行求值
  println(words(5)) //获取第6行元素
  println(words) // 此时前6行的数据已经被流缓存下来
  
   
  /**
   * 懒视图，如同流，流的方法是懒执行的，仅当结果需要的时候才被计算。可以在其他集合上
   * 应用view方法来得到相同的效果。该方法产出一个其方法总是被懒执行的集合
   */
  
  val powers = (0 until 1000).view.map { scala.math.pow(10,_)} //将产出一个未被求值的集合(不像流，这里联第一个元素都未被求值)
  //当调用powers(100),pow(10,100)被计算，但是其他的值未被计算。并且和流不同，这些视图并不缓存任何值，即如果再次调用powers(100),pow(10,100)将被
  //重新计算，和流一样，用force方法可以对懒视图强制求值
  
  
  /**
   * 懒视图适合于处理那种需要进行多种变换的大型集合，因为这可以避免构建出大型中间集合的需要。
   * 比如: (0 to 1000).map(pow(10,_)).map(1/_)
   * 和 (0 to 1000).view.map(pow(10,_)).map(1/_)).force
   * 
   * 前一个会计算出10的n次方的集合，然后再对每一个得到的值取倒数，而后一个产出的是记住了两个map操作的视图，
   * 当求值动作被强制执行，对于每个元素，这两个操作被同时执行，不需要构建额外的中间集合
   */
  
  /**
   * 与Java集合的互操作
   */
  //java的map转为scala的map
  import scala.collection.JavaConversions.propertiesAsScalaMap
  val props : scala.collection.mutable.Map[String,String] = System.getProperties
  
  //scala的List转为Java的List
  import scala.collection.JavaConversions.seqAsJavaList
   java.util.Collections.synchronizedList(List[String]())
   
   /**
    * 线程安全的集合
    * scala.collection.concurrent.Map <=> java.util.concurrent.ConcurrentMap
    */
  
   val scores = new scala.collection.mutable.HashMap[String,Int] with scala.collection.mutable.SynchronizedMap[String,Int]
  
   
   /**
    * 并行集合
    */
  (0 to 1000).par.sum
  for(i <- (0 to 100).par) print(i+" ")  //数字是按照作用于该任务的线程产生出的顺序输出的。
 
  println(for(i <- (0 to 100).par) yield i+" ") //在for/yield循环中，结果是依次组装的。返回的结果是:0,1,2,3...100
  
  //par方法返回的并行集合的类型为扩展子ParSeq等特质的类型，这些特质都是ParIterable的子类型，不是Iterable的子类型不能将并行集合传递给
  //Iterable/Seq,set,Map的方法，可以使用seq方法将并行集合转换回串行版本
  val parSeq = (0 to 100).par.seq
  
  (0 to 10).foldLeft("A")(_.length + _ +"") //fold[A1 >: A](z: A1)(op: (A1, A1) => A1): A1 = foldLeft(z)(op) 即fold方法的A1类型的下界是A
  
  
  
 
}