package com.luogh.test

import java.util.concurrent.TimeUnit

import scala.concurrent.duration._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits._

/**
  * Dependent Method Types
  *
  * Magnet Pattern
  *
  * @author luogh
  */
object MagnetPatternTrial {
  def main(args: Array[String]): Unit = {
    val localComputation = LocalComputation(Future{
      println("local")
      LocalResponse(200)
    })
    val remoteComputation = RemoteComputation(Future{
      println("remote")
      RemoteResponse("remote")
    })
    val result: LocalResponse = Service.handle(localComputation)
    val result1: RemoteResponse = Service.handle(remoteComputation)

    println(result.productPrefix)

    TimeUnit.SECONDS.sleep(2)

  }
}


case class LocalResponse(statusCode: Int)
case class RemoteResponse(message: String)

sealed trait Computation {
  type Response
  val work: Future[Response]
}

case class LocalComputation(work: Future[LocalResponse]) extends Computation {
  type Response = LocalResponse
}

case class RemoteComputation(work: Future[RemoteResponse]) extends Computation {
  type Response = RemoteResponse
}


object Service {
  def handle(computation: Computation): computation.Response = {
    val duration = Duration(2, SECONDS)
    Await.result(computation.work, duration)
  }
}