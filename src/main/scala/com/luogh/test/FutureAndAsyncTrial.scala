package com.luogh.test

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits._
import scala.util.{Failure, Success, Try}
import scala.async.Async._
/**
  * @author luogh
  */
object FutureAndAsyncTrial {
  def main(args: Array[String]): Unit = {
    futureTest()
    futureWithCallBack()
    asyncTest()

    Thread.sleep(5000)
  }

  def futureTest(): Unit = {
    val futures = 1 to 9 map {
      i => Future {
        val s = i.toString
        println(s)
        s
      }
    }
    val result = Future.reduce(futures)(_ + _)
    val r = Await.result(result, Duration.Inf)
    println(s"result:$r")
  }

  def futureWithCallBack(): Unit = {
    case class ThatsOdd(i: Int) extends RuntimeException (s"odd $i received")

    val doCompleted: PartialFunction[Try[String],Unit] = {
      case s @ Success(_) => println(s)
      case f @ Failure(_) => println(f)
    }

    val futures = (1 to 9) map {
      case i if i % 2 == 0 => Future { i.toString }
      case i  => Future[Nothing] { throw ThatsOdd(i) }
    }

    futures map (_.onComplete(doCompleted))

    val futures_1 = (1 to 9) map {
      case i if i % 2 == 0 => Future.successful(i.toString)
      case i  => Future.failed(ThatsOdd(i))
    }

    futures_1 map (_.onComplete(doCompleted))
  }


  /**
    * 使用Async将 Future串起来，这样 不需要Future 通过 onComplete() 回调方法来触发
    */
  def asyncTest(): Unit = {
    def recordExists(id: Long): Boolean = {
      println(s"record exist $id ...")
      Thread.sleep(1000)
      id > 0
    }

    def getRecord(id: Long): (Long, String) = {
      println(s"getRecord $id ...")
      Thread.sleep(1000)
      (id, s"Record: $id")
    }

    def asyncGetRecord(id: Long): Future[(Long, String)] = async {
      val exits = async {
        val b = recordExists(id)
        println(b)
        b
      }

      if (await(exits)) {
          val re = await ( async {
            val record = getRecord(id)
            println(record)
            record
          })
        re
      } else (id, s"id $id not found.")
    }

    (-1 to 1) foreach { id =>
      val fut = asyncGetRecord(id)
      println(Await.result(fut, Duration.Inf))
    }
  }
}
