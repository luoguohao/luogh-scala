package com.luogh.test

import java.security.AccessController
import java.util.concurrent.{ConcurrentMap, TimeUnit}
import javax.security.auth.Subject

import com.google.common.cache._
import com.google.common.collect.MapMaker
import com.google.inject.Key
import org.apache.hadoop.yarn.state.Graph
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}
import scala.collection.JavaConverters._
import scala.reflect.ClassTag

/**
  * @author luogh
  */
class SimpleTrialTestSuite extends FunSuite with BeforeAndAfterAll  with Matchers {

  def mkArray[T : ClassTag](elems:T*):Array[T] = {
//    val runtimeClass = reflect.classTag[T].runtimeClass
    val runtimeClass = implicitly[ClassTag[T]].runtimeClass
    println(runtimeClass.getName)
    Array[T](elems:_*)
  }

  test("runtime class info ") {
    mkArray(1,2,3)
    mkArray("a","b","c")
  }

  test("regex test") {
    val regex = """local\[([0-9]+|\*)\]""".r
    val master = "local[*]"
    master match {
      case regex(threadNum) => println(s"threadNum:$threadNum")
      case _ => println("not match")
    }
  }

  test("guava MapMaker api to create weak reference value HashMap ") {
    val map:ConcurrentMap[Int,String] = new MapMaker().weakValues().makeMap[Int,String]()
    map.put(1,"test")
  }

  test("guava CacheBuilder api to create cache") {
    val cache  = CacheBuilder.newBuilder().maximumSize(1000).removalListener(new RemovalListener[Integer,String] {
       def onRemoval(notification: RemovalNotification[Integer,String]):Unit = {
            println(s"notification:${notification.getKey.toString} removed. remove cause:${notification.getCause.name()}")
       }
    }).expireAfterWrite(1,TimeUnit.SECONDS).build[Integer,String]()
    cache.put(1,"aaa")
    cache.put(2,"bbb")
    cache.put(3,"ccc")
    cache.invalidate(1) // trigger removalListener
    Thread.sleep(3000)
    //Expired entries may be counted in {@link Cache#size}, but will never be visible to read or
    //write operations.Expired entries are cleaned up as part of the routine maintenance described in the class javadoc.
    cache.size should be (2)
    cache.getIfPresent("2") should be (null)
  }


  test("html define") {
    val JOBS_LEGEND =
    <div class="legend-area"><svg width="150px" height="85px">
      <rect class="succeeded-job-legend"
            x="5px" y="5px" width="20px" height="15px" rx="2px" ry="2px"></rect>
      <text x="35px" y="17px">Succeeded</text>
      <rect class="failed-job-legend"
            x="5px" y="30px" width="20px" height="15px" rx="2px" ry="2px"></rect>
      <text x="35px" y="42px">Failed</text>
      <rect class="running-job-legend"
            x="5px" y="55px" width="20px" height="15px" rx="2px" ry="2px"></rect>
      <text x="35px" y="67px">Running</text>
    </svg></div> .toString.filter(_ != '\n')

    println(JOBS_LEGEND)
  }

  test("seq ") {
    val seq: Seq[Int] = Array(1, 2, 4, 5)
    val s = Traversable(1,2,3)
  }
}
