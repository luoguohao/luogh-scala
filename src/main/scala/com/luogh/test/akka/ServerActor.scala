package com.luogh.test.akka


import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, OneForOneStrategy, Props, SupervisorStrategy}
import akka.util.Timeout
import akka.pattern.ask

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

/**
  * @author luogh
  */
class ServerActor extends Actor with ActorLogging {
  import Messages._
  implicit val timeout = Timeout(1.seconds)

  override val supervisorStrategy: SupervisorStrategy = {
    val decider: SupervisorStrategy.Decider = {
      case WorkerActor.CrashException => SupervisorStrategy.restart
      case NonFatal(ex) => SupervisorStrategy.resume
    }
    OneForOneStrategy()(decider orElse super.supervisorStrategy.decider)
  }

  var workers = Vector.empty[ActorRef]

  override def receive: Receive = initial

  val initial: Receive = {
    case Start(numberOfWorkers) =>
      workers = ((1 to numberOfWorkers) map makeWorker ).toVector
      context become processRequests
  }

  val processRequests: Receive = {
    case c @ Crash(n) => workers(n % workers.size) ! c
    case DumpAll =>
      Future.fold(workers map ( _ ? DumpAll))(Vector.empty[Any])(_ :+ _)
              .onComplete(askHandler("State of workers"))
    case Dump(n) =>
      (workers(n % workers.size) ? DumpAll).map(Vector(_)).onComplete(askHandler(s"State of worker $n"))
    case request: Request =>
      val key = request.key.toInt
      val index = key % workers.size
      workers(index) ! request
    case Response(Success(message)) => printResult(message)
    case Response(Failure(ex)) => printResult(s"ERROR! $ex")
  }

  def askHandler(prefix: String): PartialFunction[Try[Any], Unit] = {
    case Success(suc) => suc match {
      case vect: Vector[_] =>
        printResult(s"$prefix: \n")
        vect foreach {
          case Response(Success(message)) =>
            printResult(s"$message")
          case Response(Failure(ex)) =>
            printResult("sERROR! Success received wrapping $ex")
        }
      case _ => printResult(s"Bug! Expected a vector, got $suc")
    }
    case Failure(ex) => printResult(s"ERROR! $ex")
  }

  protected def printResult(message: String): Unit = {
    println(s"<< $message")
  }

  protected def makeWorker(i: Int) = {
    context.actorOf(Props[WorkerActor], s"worker-$i")
  }
}

object ServerActor {
  def make(system: ActorSystem): ActorRef = system.actorOf(Props[ServerActor], "server")
}
