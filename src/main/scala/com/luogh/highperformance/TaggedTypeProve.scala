package com.luogh.highperformance

import scala.util.Random
import scalaz.{@@, Tag}

/**
  * Tagged types : an alternative to value classes
  * 标签类型，来自于ScalaZ 借鉴于shapeless
  *
  * tagged type 同值类一样，是加强了编译期的检查并且避免运行期的不必要的对象实例化
  *
  * 使用的时候主要依赖于scalaz定义的Tagged 结构体类型以及 @@ 类型别名
  *   type Tagged[A,U] = { type Tag = T; type Self = A}
  *   type @@[T, U] = Tagged[T, U]
  *  对应shapeless
  *   type Tagged[U] = { type Tag = U}
  *   type @@[T, U] = T with Tagged[U]
 *
  * @author luogh
  */
object TaggedTypeProve {

    sealed trait PriceTag
    type Price = BigDecimal @@ PriceTag // 结构体类型

    object Price {

      def newPrice(p: BigDecimal): Price = {
        Tag[BigDecimal, PriceTag](p)
      }

      def lowerThan(a: Price, b: Price): Boolean = {
        Tag.unwrap(a) >= Tag.unwrap(b)
      }

      /**
        * 此时往数组中添加Price类型，就不会存在同值类Price那样增加new Price对象的操作了
        * @param count
        * @return
        */
      def newPriceArray(count: Int): Array[Price] = {
        val a = new Array[Price](count)
        for (i <- 0 until count) {
          a(i) = Tag[BigDecimal, PriceTag](BigDecimal(Random.nextInt()))
        }
        a
      }
    }
}
