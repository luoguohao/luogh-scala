package com.luogh.scala.test.scalabook

/**
  * 模式匹配与匿名函数
  * @author luogh 
  */
object PatternMatchingAnonymousFunction {

  ////////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////////

  /**
    * 假设有一个由二元组组成的序列，每个元组包含一个单词，以及对应的词频,
    * 我们的目标就是去除词频太高或者太低的单词，只保留中间地带的。 需要写出这样一个函数：
    * wordsWithoutOutliers(wordFrequencies: Seq[(String, Int)]): Seq[String]
    * 一个很直观的解决方案是使用 filter 和 map 函数：
    */
  val wordFrequencies = ("habitual", 6) :: ("and", 56) :: ("consuetudinary", 2) ::
    ("additionally", 27) :: ("homely", 5) :: ("society", 13) :: Nil

  def wordsWithoutOutliers(wordFrequencies: Seq[(String, Int)]): Seq[String] =
    wordFrequencies.filter(wf => wf._2 > 3 && wf._2 < 25).map(_._1)

  wordsWithoutOutliers(wordFrequencies) // List("habitual", "homely", "society")

  /**
    * 这个解法有几个问题。 首先，访问元组字段的代码不好看，如果我们可以直接解构出字段，
    * 那代码可能更加美观和可读。幸好，Scala 提供了另外一种写匿名函数的方式：模式匹配
    * 形式的匿名函数， 它是由一系列模式匹配样例组成的，正如模式匹配表达式那样，不过没有 match
    *
    *  在以下匿名函数中，我们只使用了一个匹配案例，因为我们知道这个样例总是会匹配成功，
    *  要解构的数据类型在编译器就已经确定了，所有没有出错的可能。这是模式匹配型匿名函数的
    *  一个非常常见的用法。
    */
  def wordsWithoutOutliers1(wordFrequencies:Seq[(String,Int)]):Seq[String] =
    wordFrequencies.filter { case (_,freq) => freq > 3 && freq < 25}
      .map { case (word,_) => word}

  /**
    * 如果把这些匿名函数赋值，可以看到他们有着正确的类型
    *
    * 需要注意的是，必须显示的声明值的类型，因为scala编译器无法从匿名函数中推到出其类型
    * 必须确保对于每一可能的输入，都会有一个样例能够匹配成功，不然，运行时抛出MatchError.
    */
  val prediction:(String,Int)=>Boolean = { case (_,freq) => freq > 3 && freq < 25 }
  val transformFn:(String,Int)=>String = {
    case (name,age) if age > 10 => "older:"+name
    case (name,_) => name
  }
  val transformFn1:(String,Int)=>String = {
    (name,_) => name
  }
  println(transformFn("t",11))
  println(transformFn1("t",11))


  ////////////////////////////////////////////////////////////////////////////////////
  ///////////////////////////////// 偏函数 ///////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////

  /**
    * 有时候可能会定义一个只处理特定输入的函数，这样的这样的一种函数能帮我们解决
    * wordsWithoutOutliers 中的另外一个问题： 在 wordsWithoutOutliers 中，我们首先过滤
    * 给定的序列，然后对剩下的元素进行映射， 这种处理方式需要遍历序列两次。 如果存在一种解法
    * 只需要遍历一次，那不仅可以节省一些 CPU，还会使得代码更简洁，更具有可读性
    *
    * Scala 集合的 API 有一个叫做 collect 的方法，对于 Seq[A] ，它有如下方法签名：
    *      def collect[B](pf: PartialFunction[A, B]): Seq[B]
    * 这个方法将给定的 偏函数(partial function) 应用到序列的每一个元素上，
    * 最后返回一个新的序列 - 偏函数做了 filter 和 map 要做的事情
    *
    * 那偏函数到底是什么呢？ 概括来说，偏函数是一个一元函数，它只在部分输入上有定义，
    * 并且允许使用者去检查其在一个给定的输入上是否有定义。 为此，特质 PartialFunction
    * 提供了一个 isDefinedAt 方法。 事实上，类型 PartialFunction[-A, +B] 扩展了类型
    * (A) ＝> B （一元函数，也可以写成 Function1[A, B] ）。 模式匹配型的匿名函数的类型
    * 就是 PartialFunction 。依据继承关系，将一个模式匹配型的匿名函数传递给接受一元函数
    * 的方法（如：map、filter）是没有问题的， 只要这个匿名函数对于所有可能的输入都有定义。
    * 不过 collect 方法接受的函数只能是 PartialFunction[A, B] 类型的。 对于序列中的每一
    * 个元素，首先检查偏函数在其上面是否有定义， 如果没有定义，那这个元素就直接被忽略掉,
    * 否则，就将偏函数应用到这个元素上，返回的结果加入结果集。
    * 现在，我们来重构 wordsWithoutOutliers ，首先定义需要的偏函数：
    */
  val pf:PartialFunction[(String,Int),String] = {
    case (name,age) if age > 10 && age < 30 => name  // 加入守卫语句
  }

  // 除了使用上面的这种方式，还可以显示的扩展PartialFunction特质:
  val pf1 = new PartialFunction[(String,Int),String] {
    override def isDefinedAt(x: (String, Int)): Boolean = x match {
      case (word,freq) if freq > 3 && freq < 25 => true
      case _ => false
    }

    override def apply(v1: (String, Int)): String = v1 match {
      case (name,freq) if freq > 3 && freq < 25 => name
    }
  }
  def wordsWithoutOutliers2(wordFrequencies:Seq[(String,Int)]):Seq[String] =
  // 把定义好的pf传递给map函数，能够通过编译期，但是运行时会抛出MatchError
  // 因为我们的偏函数并不是在所有的输入值上都有定义
  // wordFrequencies.map(pf) // will throw a MatchError
  // wordFrequencies.collect(pf) //List("habitual", "homely", "society")
     wordFrequencies.collect {
       case (name,freq) if freq > 4 && freq < 30 => name
     }

  wordsWithoutOutliers2(wordFrequencies)

  ////////////////////////////////////////////////////////////////////////////////////
  ///////////////////////////////// 链接偏函数 ///////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////

  /**
    * 匿名函数那一章提到过，偏函数可以被用来创建责任链： PartialFunction 上的 orElse
    * 方法允许链接任意个偏函数，从而组合出一个新的偏函数。 不过，只有在一个偏函数没有
    * 为给定输入定义的时候，才会把责任传递给下一个偏函数。 从而可以做下面这样的事情：
    *  val handler = fooHandler orElse barHandler orElse bazHandler
    */
  def main(args:Array[String]):Unit = {
    PatternMatchingAnonymousFunction
  }
}
