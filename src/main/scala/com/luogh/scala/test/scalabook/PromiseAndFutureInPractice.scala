package com.luogh.scala.test.scalabook

import scala.util.{Failure, Success}

/**
  * 实战中的 Promise 和 Future
  *
  * Future 只是整个谜团的一部分:它是一个只读类型,允许你使用它计算得到的值,或者处理计算中
  * 出现的错误.但是在这之前,必须得有一种方法把这个值放进去.你将会看到如何通过 Promise类型
  * 来达到这个目的。
  *
  * @author luogh 
  */
object PromiseAndFutureInPractice {

  ///////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////// 类型Promise //////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////

  // Promise 允许你在Future中放入一个值，不过只能做一次，Future一旦完成，不能改变了。
  // 一个Future总是和一个(也只能是一个)Promise实例关联起来。
  // 实际上，Future{ " HELLO WORLD" },方法内部也是通过DefaultPromise来实例化Future.
  // Future只是一个辅助函数，隐藏了具体的实现细节
  import scala.concurrent.Future
  import scala.concurrent.ExecutionContext.Implicits.global
  val f:Future[String] = Future {"Hello World"}
  f.onComplete {
    case Success(word) => println(word)
    case Failure(ex) => println(ex.toString)
  }


  ///////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////// 给出承偌 /////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////

  // 假设被推选的政客给他的投票者一个减税的承诺
  import scala.concurrent.Promise
  case class TaxCut(reduction:Int)
  // either give the type as a type parameter to the factory method:
  val taxCut = Promise[TaxCut]()
  // or give the compiler a hint by specifying the type of your val:
  val taxCut2:Promise[TaxCut] = Promise()

  // 一旦创建这个Promise,就可以在他上面调用future方法来获取承偌的未来
  val taxCutF:Future[TaxCut] = taxCut.future  //Promise 和 Future 之间一对一的关系


  ///////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////// 结束承偌 /////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////

  //成功结束一个承偌，调用他的success方法,这个方法只能调用一次，如果继续写，会产生异常
  taxCut.success(TaxCut(20))

  /**
    * 此时，和Promise关联的Future也成功完成，注册回调开始执行，或者说对这个Future进行的映射
    * 这个时候映射函数也将开始执行。
    *
    * 一般来说，Promise的完成和对返回的Future的处理发生在不同的线程，很可能你创建了Promise，
    * 并立即返回和他关联的Future给调用者，而实际上，另外一个线程还在计算他。
    *
    * 如下：减税例子。
     */
  object Government {
    def redeemCampaignPledge():Future[TaxCut] = {
      val p = Promise[TaxCut]()
      // Future伴生对象的使用，用来说明Promise的兑现并不是在调用者线程里完成的。
      Future {
        println("Starting the new legislative period.")
        Thread.sleep(200)
        p.success(TaxCut(20))
        println("we reduced the taxes,You must reelect us!!!!")
      }
      p.future
    }
  }
    // 兑现当初的竞选宣言，在Future上添加一个onComplete回调:
    val taxCutF1:Future[TaxCut] = Government.redeemCampaignPledge()
    println("Now that they're elected, let's see if they remember their promises...")
    taxCutF1.onComplete {
      case Success(TaxCut(reduction)) =>
        println(s"A miracle! They really cut our taxes by $reduction percentage points!")
      case Failure(ex) =>
        println(s"They broke their promises! Again! Because of a ${ex.getMessage}")
    }


  ///////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////// 违背承偌 /////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////

  // 调用failure方法，传递一个异常，结束Promise.
  // 这个redeemCampaignPledge 实现最终会违背承诺.一旦用failure结束这个Promise,也无法再次
  // 写入了,正如success方法一样.相关联的Future也会以Failure收场.
  case class LameExcuse(msg:String) extends Exception(msg)
  object Government1{
    def redeemCampaignPledge():Future[TaxCut] = {
      val p = Promise[TaxCut]()
      Future {
        println("Starting the new legislative period.")
        Thread.sleep(2000)
        p.failure(LameExcuse("global economy crisis"))
        println("We didn't fulfill our promises, but surely they'll understand.")
      }
      p.future
    }
  }

  // 兑现当初的竞选宣言，在Future上添加一个onComplete回调:
  val taxCutF2:Future[TaxCut] = Government1.redeemCampaignPledge()
  println("Now that they're elected, let's see if they remember their promises...")
  taxCutF2.onComplete {
    case Success(TaxCut(reduction)) =>
      println(s"A miracle! They really cut our taxes by $reduction percentage points!")
    case Failure(ex) =>
      println(s"They broke their promises! Again! Because of a ${ex.getMessage}")
  }


  /////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////显式指定ExecutionContext ////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////

  /**
    * 我们都是使用隐式可用的全局 ExecutionContext 来执行这些代码块.通常,更好的方式是创建
    * 一个专用的ExecutionContext放在数据库层里.可以从Java的ExecutorService来它,这也意味着,
    * 可以异步的调整线程池来执行数据库调用，应用的其他部分不受影响。
    */

  import java.util.concurrent.Executors
  import scala.concurrent.ExecutionContext
  val executorService = Executors.newFixedThreadPool(4)
  implicit val executionContext = ExecutionContext.fromExecutorService(executorService)
  Future{
    println("executorService")
  }

  println("e")

  def main(args:Array[String]):Unit = {
    PromiseAndFutureInPractice
    Thread.sleep(5000)
  }
}
