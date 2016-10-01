package com.luogh.scala.test.scalaimpatient

import scala.actors.Actor._
/**
 * @author Kaola
 * 
 * 每个actor都要扩展Actor类并提供actor方法
 * 要往actor发送消息，可用 actor ! message
 * 消息发送是异步的，“发完就忘”
 * 要接受消息，actor可以调用receive 或者react,通常在循环里这样做。
 * receive、react的参数由case语句组成的代码块。（从技术角度上看是一个偏函数）
 * 不同actor之间不应该共享状态，总是使用消息来发送数据
 * 不要直接调用actor的方法，通过消息进行通信
 * 避免同步消息---也就是说将发送消息和等待响应分开
 * 不同actor可以通过react而不是receive来共享线程。前提是消息处理器的控制流转足够简单
 * 让actor挂掉是ok的，前提是有其他actor监控着actor的生死，用连接来设置监控关系
 */

case class Charge1(creditCardNumber:Long,merchart:String,amount:Double)

object mainClass24 extends App {
  val actor2 = actor {
    receive {
        case "Hi" =>println("actor2:Hello2")
        case Charge1(m,b,c) => println("m:"+m +" b:"+b +"")
      }
  
  }
  actor2.start
  actor2 ! Charge1(323232323,"Freada`sS And Tackle",12.3)
  actor2 ! Charge1(3323,"another test",12233)
  println("end")
  
}