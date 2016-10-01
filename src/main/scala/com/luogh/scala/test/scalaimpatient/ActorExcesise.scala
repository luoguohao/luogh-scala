package com.luogh.scala.test.scalaimpatient

import scala.math._
import scala.actors.Actor
import scala.actors.Actor._
import scala.collection.mutable.ArrayBuffer
import java.io.File
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
/**
 * @author Kaola
 */
class ActorExcesise {
  
  /**
   * 编写一个程序，生成由n个随机数组成的数组(其中n是一个很大的值，比如1 000 000)，
   * 然后通过将工作分发给多个actor的方式，计算这些数的平均值。每个actor计算子区间内
   * 的值之和，将结果发送给一个能组合结果的actor.
   * 如果在双核或四核处理器上运行这个程序，和单线程的解决方案想比，会快多少
   */
  def exe1(){
    val seq = ( 1 to 1000 ).toArray
    //get available cpu processes 
    var processNum = Runtime.getRuntime().availableProcessors()
    if(processNum <=1) processNum = 3
    processNum = 100
    println("cpu processNum:"+processNum)
    var index = new ArrayBuffer[(Int,Int)]
    var par = (seq.length / processNum)
    
    (0 until processNum).foreach{
      x => 
        x match {
          case x if x == (processNum-1) => index += ((x*par,seq.length-1))
          case _                        => index += ((x*par,(x+1)*par-1))
        }
    }
    
   println("index:"+index.mkString(":"))
   
   @volatile var total:BigInt = 0L
   @volatile var finished = false
   val sumActor = actor{
     var index = processNum
         loopWhile(index >0){
           receive {
            case x:BigInt => {
                total += x
                index -= 1
                println("actor -> total:"+total)
            } 
            case _ =>  println("anything else");Nothing
          }
        } andThen{
          finished = true
          println("execute ended!")
        }
     
    }

    index.foreach{
      x=> println("for:"+x)
         val at = actor{
           receive {
             case (x:Int,y:Int) => {
               val sum:BigInt = seq.slice(x,y+1).sum
               println("sum:"+sum)
               sumActor ! sum
             }
             case _ => Nothing
           }
         }
         at ! x
    }
    
   //Thread.sleep(3000)
    println("processNum:"+processNum)
    while(!finished) {
      Thread.sleep(100)
      println("waiting result ...")
    }
    println("total:"+total)
  }
  
  /**
   *编写一个程序，读取一个大型图片到BufferedImage对象中，用javax.imageio.ImageIO.read方法。
   * 使用多个actor,每个actor对图像的某一个条带区域进行反色处理，当所有条带都被反色后，输出结果
   */
  def readImage(){
    val opActor = actor{
      
    }
    val buffer = ImageIO.read(new File("G://images.jpg"))
    val width = buffer.getWidth
    val height = buffer.getHeight
    var processNum = Runtime.getRuntime.availableProcessors()
    if(processNum<10) processNum = 10 else processNum = 20
    val array = Array.ofDim[Int](width,height)
    for(i <- 0 until width) {
      for(j <- 0 until height){
        array(i)(j) = ~ buffer.getRGB(i,j) //取反
      }
    }
    
    saveImage(array)
   
  }
  
  def saveImage(array:Array[Array[Int]]) {
    val bi = new BufferedImage(array.length,array(0).length,BufferedImage.TYPE_INT_RGB);
    for(i <- 0 until array.length){
      for(j <- 0 until array(0).length) {
        bi.setRGB(i,j, array(i)(j))
      }
    }
    ImageIO.write(bi, "JPEG", new File("G://images_01.jpg"))
  }
  
  
  /**
   * 编写一个程序，构造100个actor，这些actor使用while(true)/receive循环，当接收到hello的消息时，调用println(Thread.currentThread),同时
   * 构造另外100个actor，做同样的事，不过采用loop/react。将他们全部启动，给他们全部都发送一个消息，第一种actor占用了多少个线程，第二种actor占用了几个线程
   * 
   * 在每个actor里，调用receive()的时候实际上会要求有一个单独的线程。这个线程会一直持有，直到这个actor结束。也就是说，即便是在等待消息到达，程序也会持有这些线程，
   * 每个actor一个，这绝对是一种资源浪费。Scala不得不持有这些线程的原因在于，控制流的执行过程中有一些具体状态。如果在调用序列里没有需要保持和返回的状态，Scala几乎就可以从线程池里获取任意线程执行消息处理——这恰恰就是使用react()所做的事情。react()不同于receive()，它并不返回任何结果。实际上，它并不从调用中返回。
   * 如果处理了react()的当前消息后，还要处理更多的消息，就要在消息处理的末尾调用其他方法。Scala会把这个调用执行交给线程池里的任意线程
   */
  def test() {
    
    
    for(num <- 1 to 10){
      val actor_01 = actor{
      while(true) {
        receive{
          case "hello" => println("actor_01 =>"+Thread.currentThread().getName)
        }
      }
    }
    
    val actor_02 = actor{
      loop {
        react{
          case "hello" => println("actor_02 =>"+Thread.currentThread().getName)
        }
      }
    }
    
    actor_01 ! "hello"
    actor_02 ! "hello"
    }
    
  }
  
}


 
object mainClass25 extends App {
  val object01 = new ActorExcesise()
  //object01.exe1
  //object01.readImage
  object01.test
}