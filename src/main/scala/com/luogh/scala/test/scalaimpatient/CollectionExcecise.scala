package com.luogh.scala.test.scalaimpatient

/**
 * @author Kaola
 */
class CollectionExcecise {
  /**
   * 编写一个函数，给定字符串，产出一个包含所有字符串的下标的映射。
   * 比如：indexes("Mississippi")应返回映射，'M'对应集{0}，
   * 'i'对应集{1,4,7,10},使用字符到可变集的映射，另外如何保证集
   * 是经过排序的。
   */
  
  def indexes(str:String)= {
    val map = scala.collection.mutable.Map[Char,Seq[Int]]()
    str.zipWithIndex.foldLeft(map){
      (m,c) => {
        m += (c._1 -> (m.getOrElse(c._1, Seq[Int]()) :+ c._2 ))
        m
        }
      }
    //此时的map中的值是有值的，因为使用+=来添加元素到map中
  }
  
  /**
   * 重复以上方法，这次使用字符到列表的不可变映射
   */
   def indexes_02(str:String)= {
    val map = scala.collection.Map[Char,Seq[Int]]()
    str.zipWithIndex.foldLeft(map){
      (m,c) => m + (c._1 -> (m.getOrElse(c._1, Seq[Int]()) :+ c._2 ))
      }
    //此时的map仍然为空,不可变映射不支持+=操作
    }
   
   /**
    * 编译一个函数,从一个整型链表中去除所有零值
    */
   def removeZero(list:List[Int])={list.filterNot{_==0}}
   
   /**
    * 编写一个函数，接受一个字符串的集合，以及一个从字符串到整数值的映射。返回整型的集合，
    * 其值为能和集合中某个字符串相对应的映射的值。比如 ：Array("Tom","Fred","Harry")
    * 和Map("Tom"->3,"Dick"->4,"Harry"->5)返回Array(3,5).提示使用flatMap将
    * get返回的Option值组合起来
    */
   def getDataFromMap(arr:Array[String],map:Map[String,Seq[Int]])= {
     arr.flatMap(x=>if(map.contains(x)) map.get(x).get else None)
   }
   
   /**
    * 实现一个方法，作用和mkString一样，使用reduceLeft完成
    */
   def mkStringBySelf[T](ite:Iterable[T],sep:String)={
          ite.map { _.toString}.reduceLeft(_.toString + sep + _.toString)  
   }
   
   /**
    * 表达式(price zip quanlities) map { p => p._1 * p._2}有些不够优雅，
    * 我们不能用(price zip quanlities) map {_ * _}因为_*_是一个带两个
    * 参数的函数，而我们需要的是带单个类型为元祖的参数的函数，Function对象的
    * tupled方法，可以将带两个参数的函数改为以元祖为参数的函数，将tupled应用于
    * 乘法函数，以便我们可以使用它来映射由对偶组成的列表
    */
    def usingTuple(list1:List[Int],list2:List[Int])={
      (list1 zip list2) map {Function.tupled(_ * _) }
    }   
    
    /**
     * 编写一个函数，将Double数组转换成二维数组。传入列数作为参数。比如
     * Array(1,2,3,4,5,6)和三列，返回Array(Array(1,2,3),Array(4,5,6))
     * 用grouped方法
     */
    def dimOfArraySelf(arr:Array[Int],dim:Int)= {
      arr.grouped(dim).toArray
    }
    
    /**
     * 如果想对字符串的不同的部分用并行集合来并发的更新字母出现的频率映射。
     * 它使用了如下代码：
     * val frequencies = new scala.collection.mutable.HashMap[Char,Int]
     * for(c <- str.par) frequencies(c) = frequencies.getOrElse(c,0) +1
     * 为什么说这个想法很糟糕？要真正并行化计算，应该怎么做？(使用aggregate)
     */
    
    
    def parExecute(str:String)= {
      str.par.aggregate(scala.collection.mutable.Map[Char,Int]())(seqop(_,_),combop(_,_))
      }
    
    def seqop(m:scala.collection.mutable.Map[Char,Int],c:Char)= {
      m(c) =m.getOrElse(c, 0)+1;m
    }
    
    def combop(m1:scala.collection.mutable.Map[Char,Int],m2:scala.collection.mutable.Map[Char,Int])= {
      (m1.keySet ++ m2.keySet).foldLeft(scala.collection.mutable.Map[Char,Int]()) {
          (m3,k) => { m3(k) = m1.getOrElse(k,0) + m2.getOrElse(k,0); m3} 
      }
    }
}

  


object mainClass14 extends App {
  val obj01 = new CollectionExcecise()
  println( obj01.indexes("Mississippi") )  //Map(M -> List(0), s -> List(2, 3, 5, 6), p -> List(8, 9), i -> List(1, 4, 7, 10))
  println( obj01.indexes_02("Mississippi") )  //Map(M -> List(0), s -> List(2, 3, 5, 6), p -> List(8, 9), i -> List(1, 4, 7, 10))
  val list = List(1,2,3,4,0,0,534,0,0,2342)
  println( obj01.removeZero(list)) //List(1, 2, 3, 4, 534, 2342)
  println( list ) //List(1, 2, 3, 4, 0, 0, 534, 0, 0, 2342)
  
  println(obj01.getDataFromMap(Array("Tom","Fred","Harry"),Map("Tom"->Seq(3,2),"Dick"->Seq(4,4),"Harry"->Seq(5,3,5))).mkString(","))
  println(obj01.mkStringBySelf(List("this","is","tst"),">"))
  
  val list1 = List(1,2,3,4)
  println((list1:\List[Int]())(_::_))//List(1,2,3,4)
  println((List[Int]()/:list1)(_:+_))//List(1,2,3,4)
  println((List[Int]()/:list1)((li,in)=> in+:li))//List(4, 3, 2, 1)
  
  val list2 = Array(1,2,3,4,5,6)
  println(obj01.dimOfArraySelf(list2,3).map(_.mkString(":")).mkString(","))
  
  println(">>>")
  println(obj01.parExecute("thisia test you can see")) //Map(n -> 1, e -> 3, h -> 1,   -> 4, t -> 3, s -> 3, y -> 1, a -> 2, i -> 2, c -> 1, u -> 1, o -> 1)
}