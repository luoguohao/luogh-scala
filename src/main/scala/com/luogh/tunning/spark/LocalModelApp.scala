package com.luogh.tunning.spark

import org.apache.spark.scheduler.{SparkListener}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author luogh 
  * @date 2016/9/15
  */
object LocalModelApp {

  def main(args:Array[String]):Unit = {
    submitSparkJob
  }

  def submitSparkJob():Unit = {

    val sparkConf = new SparkConf
    sparkConf.set("spark.logConf","true")
             .setAppName("first test")
             .set("spark.eventLog.enabled","true")
             .setMaster("local[*]")
             .set("spark.eventLog.dir","D:\\tmp\\spark_eventLog")
    val sc = new SparkContext(sparkConf)
    sc.addSparkListener(new SparkListener(){

    })
    val rdd = sc.textFile("file:///data/test_data.gz")
    val finalRdd = rdd.filter(_.length > 10).flatMap(_.split(" "))
    val resultList = finalRdd.take(100)
    resultList.foreach(println _)

    // using dagScheduler info to get App execution status is not a infeasible way in scala, because of the private[package] access control,
    // but, this is a feasible way in java,because private[package] access control is not limited in java.
    // zeppelin spark interpreter is using this way with java language.

    //    val dagScheduler = sc.getClass.getMethod("dagScheduler").invoke(sc).asInstanceOf[DAGScheduler.class]
  }
}
