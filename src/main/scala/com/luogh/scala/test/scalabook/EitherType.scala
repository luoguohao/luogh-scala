package com.luogh.scala.test.scalabook

/**
  * 类型Either
  * ref:http://udn.yyuap.com/doc/guides-to-scala-book/chp7-the-either-type.html
  * Try 不能完全替代 Either，它只是 Either 用来处理异常的一个特殊用法。 Try 和 Either
  * 互相补充，各自侧重于不同的使用场景。
  *
  * Either 也是一个容器类型，但不同于 Try、Option，它需要两个类型参数： Either[A, B]
  * 要么包含一个类型为 A 的实例，要么包含一个类型为 B 的实例。 这和 Tuple2[A, B] 不一样，
  * Tuple2[A, B] 是两者都要包含。
  *
  * Either 只有两个子类型： Left、 Right， 如果 Either[A, B] 对象包含的是 A 的实例，
  * 那它就是 Left 实例，否则就是 Right 实例。
  * 在语义上，Either 并没有指定哪个子类型代表错误，哪个代表成功， 毕竟，它是一种通用的类型，
  * 适用于可能会出现两种结果的场景。 而异常处理只不过是其一种常见的使用场景而已， 不过，
  * 按照约定，处理异常时，Left 代表出错的情况，Right 代表成功的情况。
  *
  * @author luogh
  * @date 2016/10/2
  */
object EitherType {

  ///////////////////////////////////////////////////////////////////////////////////
  ///////////////////////////////// 创建Either //////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////

  import scala.util.Left
  import scala.util.Right
  import scala.util.Either
  import java.net.URL
  import scala.io.Source
  def getContent(url:URL) :Either[String,Source] =
    if(url.getHost.contains("google"))
      Left("Requested URL is blocked for the good of the people")
    else
      Right(Source.fromURL(url))

  val content = getContent(new URL("http://www.baidu.com"))

  /////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////Either 用法/////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////

  getContent(new URL("http://www.baidu.com")) match {
    case Left(msg) => println(msg)
    case Right(source) => source.getLines().foreach(println)
  }

  ///////////////////////////////////////////////////////////////////////////////
  /////////////////////////////// 立场 //////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////

  /**
    * 你不能，至少不能直接像 Option、Try 那样把 Either 当作一个集合来使用,因为
    * Either是无偏(unbiased)的。Try偏向Success:map、flatMap以及其他一些方法都假设
    * Try 对象是一个 Success 实例， 如果是 Failure，那这些方法不做任何事情，直接将
    * 这个 Failure 返回.但Either不做任何假设，这意味着首先你要选择一个立场，假设它
    * 是Left 还是 Right,然后在这个假设的前提下拿它去做你想做的事情。调用 left 或
    * right 方法，就能得到 Either 的 LeftProjection 或 RightProjection实例,这就是
    * Either 的 立场(Projection) ，它们是对 Either 的一个左偏向的或右偏向的封装。
    */

  ///////////////////////////////////////////////////////////////////////////////
  /////////////////////////////// 映射 //////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////

  /**
    * 一旦有了 Projection，就可以调用 map
    * 无论 Either[String, Source] 是 Left 还是 Right， 它都会被映射到
    * ither[String, Iterator[String]] 。 如果，它是一个 Right 值，这个值就会被
    * _.getLines()转换;如果,它是一个Left值,就直接返回这个值,什么都不会改变
    */
  val content1 : Either[String,Iterator[String]] =
    getContent(new URL("http://www.baidu.com")).right.map(_.getLines)
  // content is a Right containing the lines from the Source returned by getContent
  val moreContent: Either[String, Iterator[String]] =
    getContent(new URL("http://www.google.com")).right.map(_.getLines)
  // moreContent is a Left, as already returned by getContent

  /**
    *请注意,map 方法是定义在 Projection 上的，而不是 Either,但其返回类型是 Either，而不是
    * Projection.可以看到，Either 和其他你知道的容器类型之所以不一样，就是因为它的无偏性.
    * 接下来你会发现，在特定情况下，这会产生更多的麻烦。 而且，如果你想在一个 Either 上多次
    * 调用 map 、 flatMap 这样的方法,你总需要做 Projection，去选择一个立场。
    */

  ///////////////////////////////////////////////////////////////////////////////
  /////////////////////////////// Flat Mapping //////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////

  /**
    * Projection 也支持 flat mapping，避免了嵌套使用 map 所造成的令人费解的类型结构。
    * 假设我们想计算两篇文章的平均行数，下面的代码可以解决这个 “富有挑战性” 的问题：
    *
    * 运行上面的代码,会得到什么?会得到一个类型为 Either[String, Either[String, Int]]
    * 的玩意儿。当然,你可以调用 joinRight 方法来使得这个结果 扁平化(flatten) 。
    * 不过我们可以直接避免这种嵌套结构的产生,如果在最外层的 RightProjection 上调用
    * flatMap函数,而不是 map,得到的结果会更好看些,因为里层 Either的值被解包了：
    * 现在,content值类型变成了 Either[String, Int],处理它相对来说就很容易了
    */
  val part5 = new URL("http://www.baidu.com")
  val part6 = new URL("http://www.baidu.com")
  val content3 = getContent(part5).right.map(a =>
    getContent(part6).right.map(b =>
      (a.getLines().size + b.getLines().size) / 2))
  // => content: Product with Serializable with scala.util.Either[String,Product with Serializable with scala.util.Either[String,Int]] = Right(Right(537))

  val content2 = getContent(part5).right.flatMap(a =>
    getContent(part6).right.map(b =>
      (a.getLines().size + b.getLines().size) / 2))
  // => content: scala.util.Either[String,Int] = Right(537)
  content2 match {
    case Left(msg) => println(s"message:$msg")
    case Right(iter) => println(s"count $iter")
  }

  def main(args:Array[String]):Unit = {
    EitherType
  }
}
