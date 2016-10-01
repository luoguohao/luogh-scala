package com.luogh.scala.test

import com.luogh.bean.Person
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag
import scala.util.control.NonFatal

/**
  * @author luogh 
  * @date 2016/9/10
  */
object SimpleTrial {

  def main(args:Array[String]):Unit = {
//    packagePrivilegeTest()
//    stackTraceTest()
//    stripText()
//    dropWhileTest()
    getRuntimeClass(Array(Male("luogh",2)))
//    tryLogNonFatalError(throw new RuntimeException("test")) // 传值调用,异常立即抛出，而不会执行tryLogNonFatalError()方法体。
    tryLogNonFatalError(()=>throw new RuntimeException("test"))// 传值调用
    tryLogNonFatalError_1(throw new RuntimeException("test")) // 传名调用

    val list = List(Teacher("teacher"),IT("LUOGUO"),"ASASD",11)
    list.collect(partialFunctionTest("tes")).foreach(println _)
    println(partialFunctionTest("test").isDefinedAt(11)) // false
    println(partialFunctionTest("test").isDefinedAt(IT("luogh"))) //true
    println(partialFunctionTest("test").applyOrElse(11,(x:Int)=>12)) //12
  }

case class Male(name:String,age:Int);

  def packagePrivilegeTest(): Unit ={
    val person = new Person
    // field
    val name = person.getClass.getMethod("name").invoke(person)
    // val id = person.getClass.getMethod("id").invoke(person) //cant find id method,because its private.
    println(s"age:${person.age}")
    println(s"name:${name}")
    person.getClass.getMethod("name_$eq",classOf[String]).invoke(person,"new name")
    val newName = person.getClass.getMethod("name").invoke(person)
    println(s"newName:${newName}")
    println(s"sex:${person.sex}")

    // method
    person.getClass.getMethod("makeTest").invoke(person)
    // person.getClass.getMethod("makeTest2").invoke(person) // cant find method makeTest2,because its private.
  }

  def stackTraceTest():Unit = {
    val array = new ArrayBuffer[String]
    Thread.currentThread().getStackTrace.foreach { ste:StackTraceElement =>
      if(ste.getMethodName ne "getStackTrace") {
        val steStr = s"${ste.getFileName}:${ste.getClassName}.${ste.getMethodName}[${ste.getLineNumber}]"
        array += steStr
      }
    }
    array.foreach(println _)
  }

  def stripText():Unit = {
    val str = s"""Cannot call methods on a stopped SparkContext.
        |This stopped SparkContext was created at:
        |
        |test
        |
        |   The currently active SparkContext was created at:
        |
        |""".stripMargin
    println(str)
  }

  def dropWhileTest():Unit = {
    Thread.currentThread().getStackTrace.foreach(println)
    println
    val stace = Thread.currentThread().getStackTrace.dropWhile{ st:StackTraceElement =>
      println(s"${st.getClassName}=>${this.getClass.getSimpleName}")
      !st.getClassName.contains(this.getClass.getSimpleName)
    }
    stace.foreach(println)
    println
    val drop_1 = stace.drop(1)
    drop_1.foreach(println)
  }

  def getRuntimeClass[K:ClassTag](arr:Array[K]) = {
    val className = reflect.classTag[K].runtimeClass.getName
    println(s"className:${className}")
  }


  def tryLogNonFatalError(block: ()=>Any) : Unit = {
    try {
      block()
    } catch {
      case NonFatal(e) => println("Exception:"+e)
      case e:Throwable => throw e
      case e @ _ =>
    }
  }

  /**
    * block : =>Unit 传名调用，即函数不会立即调用，而是在引用block时，才会调用。scala默认是传值调用。
    * tryLogNonFatalError_1(throw new RuntimeException("test")) 此时异常不会立即抛出，而是在tryLogNonFatalError_1
    * 方法中，当引用block对象时，异常才会真正抛出，这个既是延迟计算。
    *
    * 而对于方法tryLogNonFatalError(block :()=>Any)因为他是传值调用，所以，当调用tryLogNonFatalError(throw new RutimeException("test"))
    * 的时候，异常会立即抛出，而不会等到执行tryLogNonFatalError()方法体才抛出。可以写成以下方式:
    * tryLogNonFatalError(()=> throw new RuntimeException("test")) 将一个匿名函数传入，这样，异常只有在方法体中真正调用该匿名
    * 函数的时候才会抛出该异常。
    * @param block
    */
  def tryLogNonFatalError_1(block: =>Unit) : Unit = {
    try {
      block
    } catch {
      case NonFatal(e) => println("Exception:"+e)
      case e:Throwable => throw e
    }
  }

  def partialFunctionTest(key:Any):PartialFunction[Any,Any] = {
    //scala模式匹配允许将提取器匹配成功的实例绑定到一个变量上，这个变量有着和
    //提取器所接受的对象相同的类型，通过@操作符实现。
    case e @ IT(name) => println(s"IT NAME:${name}") ; e
    case e: Teacher => e
    case e:String => e
  }

  sealed abstract class Career(name:String)
  case class IT(name:String) extends Career(name)
  case class Teacher(name:String) extends Career(name)
  case class Farmer(name:String) extends Career(name)
}
