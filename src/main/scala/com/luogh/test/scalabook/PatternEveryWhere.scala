package com.luogh.test.scalabook

/**
  * 无处不在的模式
  *
  * 模式匹配表达式(pattern matching expression):一个表达式 e，后面跟着关键字 match
  * 以及一个代码块，这个额代码块包含了一些匹配样例;而样例又包含了case关键字、模式、可选
  * 的守卫分句(guard clause),以及最右边的代码块。
  *   e match {
  *     case Pattern1 => block1
  *     case Pattern2 if-clause => block2
  *   }
  *
  * @author luogh 
  */
object PatternEveryWhere {

  //////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////// 值定义中的模式 //////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////////

  /**
    * 模式可以出现在值定义的左边
    */
  case class Player(name:String,score:Int)
  def currentPlayer():Player = Player("test",12)
  val player = currentPlayer()
  // doSomethingWithName(player.name)
  val Player(name,_) = currentPlayer()
  // doSomethingWithName(name)

  // 元组
  def gameResult():(String,Int) = ("tes",12)  //Tuple2
  val result = gameResult()
  println(result._1+" "+result._2)
  val (game,score) = gameResult()

  //////////////////////////////////////////////////////////////////////////////////////////
  /////////////////////////////// for语句中的模式 //////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////////

  def gameResults():Seq[(String,Int)] =
    ("Daniel", 3500) :: ("Melissa", 13000) :: ("John", 7000) :: Nil
  def hallofFrame() = for {
    result <- gameResults()
    (name,score) = result
    if (score > 5000)
  } yield name

  // 简化版
  def hallofFrame1() = for {
    (name,score) <- gameResults()
    if (score > 5000)
  } yield name
  println(hallofFrame().mkString("\t"))

  /**
    * 生成器左侧的模式也可以用来过滤，如果左侧的模式匹配失败，那相关的元素就会直接被过滤掉。
    * 假设有一序列的序列，我们想返回所有非空序列的元素个数。 这就需要过滤掉所有的空列表，
    * 然后再返回剩下列表的元素个数
    *
    * 下例中，左侧的模式不会匹配空列表，不会抛出MatchError,对应的空列表会被丢掉。
    */
  val lists = List(1,2,3) :: List.empty :: List(3,5) :: Nil
  val sizes = for { list @ head::_ <- lists} yield list.size
  println(sizes.mkString("\t"))

  def main(args:Array[String]):Unit = {
    PatternEveryWhere
  }
}
