package com.luogh.tunning.spark

import org.apache.spark.storage.StorageLevel
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author luogh 
  * @date 2016/9/15
  */
object YarnModelApp {

  buildSparkEnv()

  val sc:SparkContext = new SparkContext(new SparkConf)

  def main(args:Array[String]):Unit = {
  }

  def newApp(): Unit ={
    val fileRDD = sc.textFile("/data/huge_data")
    val resultRDD = fileRDD.filter(_.split("\t").length>=2).flatMap(_.split("\t")).map((_,1))
    resultRDD.persist(StorageLevel.MEMORY_AND_DISK_SER)
    println("debug info:"+resultRDD.toDebugString)
    val finalRDD = resultRDD.reduceByKey{(a,b)=>a+b}

    val count = finalRDD.count
    println(s"first count :${count}")
    val secCount = finalRDD.count
    println(s"second count:${secCount}")
  }

  def buildSparkEnv():Unit = {}
}
