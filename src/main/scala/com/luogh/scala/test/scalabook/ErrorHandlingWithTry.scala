package com.luogh.scala.test.scalabook

/**
  * Try 与 错误处理 (函数式风格处理程序错误)
  * Scala 的错误处理和其他范式的编程语言有很大的不同。 Try 类型可以让你将可能会出错的计算封装
  * 在一个容器里，并优雅的去处理计算得到的值。 并且可以像操作集合和 Option 那样统一的去操作 Try。
  * @author luogh 
  * @date 2016/10/2
  */
object ErrorHandlingWithTry {

  ////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////异常的抛出和捕获///////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////////

  /**
    * 和其他语言一样，scala也可以抛出异常，并且以类似java的方式捕获，虽然是使用偏函数来中指定要
    * 处理的异常类型，此外，scala的try/catch是表达式（返回一个值），因此下面的代码会返回异常的
    * 消息
    */
  case class Customer(age: Int)
  class Cigarettes
  case class UnderAgeException(message: String) extends Exception(message)
  def buyCigarettes(customer: Customer): Cigarettes =
    if (customer.age < 16)
      throw UnderAgeException(s"Customer must be older than 16 but was ${customer.age}")
    else new Cigarettes

  val youngCustmor = Customer(15)
  val result = try {  // result 返回 异常的消息内容
    buyCigarettes(youngCustmor)
  } catch {
    case UnderAgeException(message) => message
  }
  println(result)

  /////////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////函数式的错误处理/////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////////

  /**
    *现在，如果代码中到处是上面的异常处理代码，那它很快就会变得丑陋无比，和函数式程序设计非常不搭。
    *对于高并发应用来说，这也是一个很差劲的解决方式，比如， 假设需要处理在其他线程执行的 actor 所
    * 引发的异常，显然你不能用捕获异常这种处理方式， 你可能会想到其他解决方案，例如去接收一个表示
    * 错误情况的消息。一般来说，在 Scala 中，好的做法是通过从函数里返回一个合适的值来通知人们程
    * 序出错了。 别担心，我们不会回到 C 中那种需要使用按约定进行检查的错误编码的错误处理。 相反
    * ，Scala 使用一个特定的类型来表示可能会导致异常的计算，这个类型就是 Try。
    *
    * Option[A] 是一个可能有值也可能没值的容器， Try[A] 则表示一种计算： 这种计算在成功的情况下，
    * 返回类型为 A 的值，在出错的情况下，返回 Throwable 。 这种可以容纳错误的容器可以很轻易的在
    * 并发执行的程序之间传递。
    * Try 有两个子类型：
    *     Success[A]：代表成功的计算。
    *     封装了 Throwable 的 Failure[A]：代表出了错的计算。
    * 如果知道一个计算可能导致错误，我们可以简单的使用 Try[A] 作为函数的返回类型。 这使得出错的
    * 可能性变得很明确，而且强制客户端以某种方式处理出错的可能。
    */

  /**
    * 实现一个简单的网页爬取器
    * 函数返回类型为 Try[URL]： 如果给定的 url 语法正确，这将是 Success[URL],否则,URL构造器
    * 会引发 MalformedURLException ，从而返回值变成 Failure[URL] 类型
    *
    *  Try 伴生对象里的 apply 工厂方法，这个方法接受一个类型为 A 的 传名参数， 这意味着,
    *  new URL(url) 是在 Try 的 apply 方法里执行的。
    *  apply 方法不会捕获任何非致命的异常，仅仅返回一个包含相关异常的 Failure 实例。
    */

  import java.net.URL

  import scala.util.Try
  def parseURL(url:String):Try[URL] = Try(new URL(url))

  val url = parseURL("http://danielwestheide.com") // http://danielwestheide.com
  val url_bad = parseURL("garbage") // throw Exception

  println(url.get)
//  println(url_bad.get)
  /**
    * 你可以调用 isSuccess 方法来检查一个 Try 是否成功，然后通过 get 方法获取它的值，
    * 但是，这种方式的使用并不多见，因为你可以用 getOrElse 方法给 Try 提供一个默认值：
    */
  println(url_bad.getOrElse("http://danielwestheide.com"))


  ////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////链式操作--Mapping和Flat Mapping //////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////////

  /**
    *将一个是 Success[A] 的 Try[A] 映射到 Try[B] 会得到 Success[B] 。 如果它是 Failure[A] ，
    * 就会得到 Failure[B] ，而且包含的异常和 Failure[A] 一样。
    */
  parseURL("http://danielwestheide.com").map(_.getProtocol)
  // results in Success("http")
  val result_1 = parseURL("garbage").map(_.getProtocol)
  // results in Failure(java.net.MalformedURLException: no protocol: garbage)

  /**
    *如果链接多个 map 操作，会产生嵌套的 Try 结构，这并不是我们想要的。 考虑下面这个返回输
    * 入流的方法：
    * 由于每个传递给 map 的匿名函数都返回 Try，因此返回类型就变成了 Try[Try[Try[InputStream]]]
    */
  import java.io.InputStream
  def inputStreamFromUrl(url:String):Try[Try[Try[InputStream]]] = parseURL(url).
          map{ u => Try(u.openConnection()).map(con => Try(con.getInputStream))}

  /**
    * 这时候， flatMap 就派上用场了。 Try[A] 上的 flatMap 方法接受一个映射函数，这个函数类型是
    * (A) => Try[B]。 如果我们的 Try[A] 已经是 Failure[A] 了，那么里面的异常就直接被封装成
    * Failure[B] 返回， 否则， flatMap 将 Success[A] 里面的值解包出来，并通过映射函数将其映射
    * 到 Try[B] 。
    */
  def inputStreamFromUrl2(url:String):Try[InputStream] = parseURL(url).
    flatMap{ u => Try(u.openConnection()).flatMap(con => Try(con.getInputStream))}
  val r = inputStreamFromUrl2("u.com")
  r.foreach(x=> println(x.read()))

  ////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////链式操作--过滤器和foreach ////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////////

  /**
    * 当一个 Try 已经是 Failure 了，或者传递给它的谓词函数返回假值，filter 就返回 Failure
    * （如果是谓词函数返回假值，那 Failure 里包含的异常是 NoSuchException ）， 否则的话，
    * filter 就返回原本的那个 Success ，什么都不会变：
    */
  def parseHttpURL(url: String):Try[URL] =  parseURL(url).filter(_.getProtocol == "http")
  parseHttpURL("http://apache.openmirror.de") // results in a Success[URL]
  parseHttpURL("ftp://mirror.netcologne.de/apache.org") // results in a Failure[URL]

  /**
    * 当一个 Try 是 Success 时， foreach 允许你在被包含的元素上执行副作用， 这种情况下，
    * 传递给 foreach 的函数只会执行一次，毕竟 Try 里面只有一个元素：
    * 当 Try 是 Failure 时， foreach 不会执行，返回 Unit 类型。
    */
  parseHttpURL("http://danielwestheide.com").foreach(println)


  ////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////链式操作--for语句中的Try //////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////////

  /**
    * 这个方法中，有三个可能会出错的地方，但都被 Try 给涵盖了。 第一个是我们已经实现的
    * parseURL 方法， 只有当它是一个 Success[URL] 时，我们才会尝试打开连接，从中创建一个新的
    * InputStream 。 如果这两步都成功了，我们就 yield 出网页内容，得到的结果是
    * Try[Iterator[String]]
    */

  import scala.io.Source
  def getURLContent(url:String):Try[Iterator[String]] = {
    for {
      u <- parseURL(url)
      c <- Try(u.openConnection())
      i <- Try(c.getInputStream) // InputStream未关闭，如果抛异常，使用Source.fromURL()来弥补
      source = Source.fromInputStream(i)
    } yield source.getLines()
  }

  import scala.io.Source
  def getURLContent1(url: String): Try[Iterator[String]] =
    for {
      url <- parseURL(url)
      source = Source.fromURL(url)
    } yield source.getLines()

  ////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////链式操作--模式匹配 ////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////////
  import scala.util.{Failure, Success}
  getURLContent("http://www.baidu.com") match {
    case Success(lines) => lines.foreach(println _)
    case Failure(ex) => println(s"Problem rendering URL content:${ex.getMessage}")
  }


  ////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////链式操作--从故障中恢复 ////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////////

  /**
    *如果想在失败的情况下执行某种动作，没必要去使用 getOrElse， 一个更好的选择是 recover ，
    * 它接受一个偏函数，并返回另一个 Try。 如果 recover 是在 Success 实例上调用的，那么就直
    * 接返回这个实例，否则就调用偏函数。 如果偏函数为给定的 Failure 定义了处理动作， recover
    * 会返回 Success ，里面包含偏函数运行得出的结果。
    */

  import java.io.FileNotFoundException
  import java.net.MalformedURLException
  val content = getURLContent("http://www.baidu.com").recover {
    case e: FileNotFoundException => Iterator("Reqeust page does not exist")
    case e: MalformedURLException => Iterator("please make sure to enter a valid URL")
    case _ => Iterator("An unexpected error has occurred.")
  }

  /**
    * 现在，我们可以在返回值 content 上安全的使用 get 方法了，因为它一定是一个 Success。
    * 调用 content.get.foreach(println) 会打印 Please make sure to enter a valid URL。
    */
  content.get.foreach(println)

  def main(args:Array[String]):Unit = {
    ErrorHandlingWithTry
  }
}
