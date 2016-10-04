package com.luogh.scala.test.scalabook


/**
  * 类型Future
  *
  * 请确保 Scala 版本不低于 2.9.3， Future 在 2.10.0 版本中引入，并向后兼容到 2.9.3，
  * 最初，它是 Akka 库的一部分（API略有不同）。
  *
  * 因为它是一个可组合、可函数式使用的容器类型，这让我们的工作变得异常舒服。
  * 调用 future 方法可以轻易将阻塞执行的代码变成并发执行，但是，代码最好原本就是非阻塞的。
  * 为了实现它，我们还需要 Promise 来完成 Future
 *
  * @author luogh
  */
object FutureType {

  ///////////////////////////////////////////////////////////////////////////////////
  //////////////////////////// 顺序代码 /////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////

  /**
    * 假设你想准备一杯卡布奇诺，你可以一个接一个的执行以下步骤：
    * 1.研磨所需的咖啡豆
    * 2.加热一些水
    * 3.用研磨好的咖啡豆和热水制做一杯咖啡
    * 4.打奶泡
    * 5.结合咖啡和奶泡做成卡布奇诺
    */

  import scala.util.Try
  // some type aliases,just for getting more meaningful method signatures:
  type CoffeeBeans = String
  type GroundCoffee = String
  case class Water(temperature:Int)
  type Milk = String
  type FrothedMilk = String
  type Espresso = String
  type Cappuccino = String

  //dummy implementations of the individual steps:
  def grind(beans:CoffeeBeans):GroundCoffee = s"ground coffee of $beans"
  def heatWater(water:Water):Water = water.copy(temperature = 85)
  def frothMilk(milk: Milk): FrothedMilk = s"frothed $milk"
  def brew(coffee: GroundCoffee, heatedWater: Water): Espresso = "espresso"
  def combine(espresso: Espresso, frothedMilk: FrothedMilk): Cappuccino = "cappuccino"

  // some exceptions for things that might go wrong in the individual steps
  // (we'll need some of them later, use the others when experimenting with the code):
  case class GrindingException(msg: String) extends Exception(msg)
  case class FrothingException(msg: String) extends Exception(msg)
  case class WaterBoilingException(msg: String) extends Exception(msg)
  case class BrewingException(msg: String) extends Exception(msg)

  // going through these steps sequentially:
  def prepareCappuccino():Try[Cappuccino] = for {
    ground <- Try(grind("arabica beans"))
    water <- Try(heatWater(Water(25)))
    espresso <- Try(brew(ground,water))
    foam <- Try(frothMilk("milk"))
  } yield combine(espresso,foam)

  /**
    * 这样做有几个优点： 可以很轻易的弄清楚事情的步骤，一目了然，而且不会混淆。
    * （毕竟没有上下文切换） 不好的一面是，大部分时间，你的大脑和身体都处于等待的状态：
    * 在等待研磨咖啡豆时，你完全不能做任何事情，只有当这一步完成后，你才能开始烧水。
    * 这显然是在浪费时间，所以你可能想一次开始多个步骤，让它们同时执行， 一旦水烧开，
    * 咖啡豆也磨好了，你可以制做咖啡了，这期间，打奶泡也可以开始了。
    *
    * Scala 的 Future 也允许回调，但它提供了更好的选择，所以你不怎么需要它。
    *     "I know Futures, and they are completely useless!"
    * 也许你知道些其他的 Future 实现，最引人注目的是 Java 提供的那个。 但是对于 Java 的 Future，
    * 你只能去查看它是否已经完成，或者阻塞线程直到其结束。 简而言之，Java 的 Future 几乎没有用，
    * 而且用起来绝对不会让人开心
    */


  ///////////////////////////////////////////////////////////////////////////////////
  //////////////////////////// Future 语义///////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////

  /**
    * scala.concurrent 包里的 Future[T] 是一个容器类型，代表一种返回值类型为 T 的计算。
    * 计算可能会出错，也可能会超时；从而，当一个 future 完成时，它可能会包含异常，而不是
    * 你期望的那个值。
    * Future 只能写一次： 当一个 future 完成后，它就不能再被改变了。 同时，Future 只提供
    * 了读取计算值的接口，写入计算值的任务交给了 Promise，这样，API 层面上会有一个清晰的界限。
    * 这篇文章里，我们主要关注前者，下一章会介绍 Promise 的使用。
    */



  ///////////////////////////////////////////////////////////////////////////////////
  //////////////////////////// 使用 Future //////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////

  // 重写上述例子，所有可以并行执行的函数，应该返回一个Future:
  import scala.concurrent.Future
  import scala.util.Random
  import scala.concurrent.ExecutionContext.Implicits.global
  def grind_1(beans:CoffeeBeans):Future[GroundCoffee] = Future{
    println("start grinding...")
    Thread.sleep(Random.nextInt(2000))
    if (beans == "baked beans") throw GrindingException("are you joking?")
    println("finished grinding...")
    s"ground coffee of $beans"
  }

  def heatWater_1(water: Water): Future[Water] = Future {
    println("heating the water now")
    Thread.sleep(Random.nextInt(2000))
    println("hot, it's hot!")
    water.copy(temperature = 85)
  }

  def frothMilk_1(milk: Milk): Future[FrothedMilk] = Future {
    println("milk frothing system engaged!")
    Thread.sleep(Random.nextInt(2000))
    println("shutting down milk frothing system")
    s"frothed $milk"
  }

  def brew_1(coffee: GroundCoffee, heatedWater: Water): Future[Espresso] = Future {
    println("happy brewing :)")
    Thread.sleep(Random.nextInt(2000))
    println("it's brewed!")
    "espresso"
  }

  /**
    * 首先是 Future 伴生对象里的 apply 方法需要两个参数：
    * object Future {
    *     def apply[T](body: => T)(implicit execctx: ExecutionContext): Future[T]
    * }
    *
    * 要异步执行的计算通过传名参数 body 传入。 第二个参数是一个隐式参数，隐式参数是说，
    * 函数调用时，如果作用域中存在一个匹配的隐式值，就无需显示指定这个参数。 ExecutionContext
    * 可以执行一个 Future，可以把它看作是一个线程池，是绝大部分 Future API 的隐式参数。
    * import scala.concurrent.ExecutionContext.Implicits.global 语句引入了一个全局的执行
    * 上下文，确保了隐式值的存在。 这时候，只需要一个单元素列表,可以用大括号来代替小括号。
    * 调用 future 方法时，经常使用这种形式，使得它看起来像是一种语言特性，而不是一个普通方法的
    * 调用。
    *
    *
    * 计算会在 Future 创建后的某个不确定时间点上由 ExecutionContext 给其分配的某个线程中执行。
    */


  ///////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////// 回调 ///////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////

  /**
    * 对于一些简单的问题，使用回调就能很好解决。 Future 的回调是偏函数，你可以把回调传递给
    * Future 的 onSuccess 方法， 如果这个 Future 成功完成，这个回调就会执行，并把 Future
    * 的返回值作为参数输入：
    *
    * 类似的，也可以在 onFailure 上注册回调，只不过它是在 Future 失败时调用，其输入是一个
    * Throwable。
    * 通常的做法是将两个回调结合在一起以更好的处理 Future：在 onComplete 方法上注册回调，
    * 回调的输入是一个 Try。
    */
  grind_1("arabica beans").onSuccess {
    case ground => println("ok,got my ground coffee")
  }
  import scala.util.Success
  import scala.util.Failure
  grind_1("arabica beans").onComplete {
    case Success(ground) =>println(s"ok,got my $ground coffee")
    case Failure(ex) => println("This grinder needs a replacement,seriously")
  }


  ///////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////// Future组合//////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////

  /**
    * 当嵌套使用 Future 时，回调就变得比较烦人。
    * 你一定已经注意到，之前讨论过的所有容器类型都可以进行 map 、 flatMap 操作，
    * 也可以用在 for 语句中。 作为一种容器类型，Future 支持这些操作也不足为奇！
    */

  ///////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////// Future组合-Map操作//////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////

  /**
    * Scala 让 “时间旅行” 成为可能！ 假设想在水加热后就去检查它的温度， 可以通过将
    * Future[Water] 映射到 Future[Boolean] 来完成这件事情
    *
    * 写传递给 map 的函数时，你就处在未来（或者说可能的未来）。 一旦 Future[Water]
    * 实例成功完成，这个函数就会执行，只不过，该函数所在的时间线可能不是你现在所处的这个。
    * 如果 Future[Water] 失败，传递给 map 的函数中的事情永远不会发生，调用 map 的结果将是
    * 一个失败的 Future[Boolean]
    */
  val temperatureOkay:Future[Boolean] = heatWater_1(Water(25)) map {
    water =>
      println("we are in future!")
      (80 to 85) contains (water.temperature)
  }

  ///////////////////////////////////////////////////////////////////////////////////
  ////////////////////////// Future组合-FlatMap操作//////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////

  /**
    * 如果一个Future 的计算依赖与另一个Future的结果，那需要求救于flatMap 以避免Future的嵌套
    *
    * 假设，测量水温的线程需要一些时间，那你可能想异步的去检查水温是否 OK。 比如，有一个函数，
    * 接受一个 Water ，并返回 Future[Boolean]
    */

  def temperatureOkay_1(water:Water):Future[Boolean] = Future {
    (80 to 85) contains (water.temperature)
  }

  // 使用flatMap(而不是map)得到一个Future[Boolean],而不是Future[Future[Boolean]]
  val nestedFuture:Future[Future[Boolean]] = heatWater_1(Water(25)) map {
    water => temperatureOkay_1(water)
  }
  //映射只会发生在 Future[Water] 成功完成情况下
  val flatFuture:Future[Boolean] = heatWater_1(Water(25)) flatMap {
    water => temperatureOkay_1(water)
  }

  ///////////////////////////////////////////////////////////////////////////////////
  ////////////////////////// Future组合-for 操作/////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////

  //除了调用flatMap,可以写成for语句，因为for语句只不过是flatMap嵌套调用的语法糖
  val acceptable : Future[Boolean] = for {
    heatWater <- heatWater_1(Water(25))  //步骤一
    okay <- temperatureOkay_1(heatWater) //步骤二: 如flatMap中，只有在步骤一执行完成，才执行步骤二
  } yield okay

  // 如果有多个并行的计算，需要在for语句外面创建好对应的Futures.
  /**
    * for语句只不过是flatMap嵌套调用的语法糖.这意味着,只有当 Future[GroundCoffee]成功完成后,
    * heatWater才会创建Future[Water]
    * 所以，该函数的四个并行步骤实际是顺序执行。
    * 只有在步骤一执行完成后才会执行步骤二 ...
    * 修改成 ： 确保在for语句之前实例化所有相互独立的Futures
    */
  def prepareCappuccinoSequentially():Future[Cappuccino] = for {
    ground <- grind_1("arabica beans") //步骤一
    water <- heatWater_1(Water(25)) // 步骤二
    foam <- frothMilk_1("milk") // 步骤三
    espresso <- brew_1(ground, water) // 步骤四
  } yield combine(espresso, foam)

  def prepareCappuccino_1(): Future[Cappuccino] = {
    val groundCoffee = grind_1("arabica beans") //步骤一
    val heatedWater = heatWater_1(Water(20)) // 步骤二
    val frothedMilk = frothMilk_1("milk") // 步骤三
    // 步骤一、步骤二、步骤三 并行计算，没有先后次序
    for {
      ground <- groundCoffee
      water <- heatedWater
      foam <- frothedMilk
      espresso <- brew_1(ground, water) // 步骤四:当步骤一、二、三全部执行完成后，执行步骤四
    } yield combine(espresso, foam)
  }



  ///////////////////////////////////////////////////////////////////////////////////
  ////////////////////////// 失败偏向的Future   /////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////

  /**
    * 你可能会发现 Future[T] 是成功偏向的，允许你使用 map、flatMap、filter 等。
    * 但是,有时候可能处理事情出错的情况.调用Future[T]上的failed方法,会得到一个失败偏向的Future,
    * 类型是Future[Throwable].之后就可以映射这个Future[Throwable],在失败的情况下执行mapping函数。
    */
  val test = Future{
    println("x")
    throw new RuntimeException("test")
  }.failed.map {
    ex => ex
  }
  def main(args:Array[String]):Unit = {
    FutureType
  }
}
