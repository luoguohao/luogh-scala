package com.luogh.scala.test.scalaimpatient

import scala.actors.Actor
import scala.actors.Actor._
import scala.actors.OutputChannel
import scala.actors.Channel
import scala.actors.TIMEOUT
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
 * 
 * 
 * 1.消息的投递过程是异步的。并不知道他们会以什么顺序到达，所在在设计的时候，应该让程序不依赖
 * 任何特定的消息投递顺序
 * 2.发送到actor的消息是被存放在一个“邮箱”中。receive方法从邮箱获取下一条消息并将他传递给他的参数
 * 3.如果在receive方法被调用时并没有消息，则该调用会阻塞，直到有消息抵达。
 * 4.如果邮箱中没有任何消息可以被偏函数处理，则对receive方法的调用也会阻塞，直到一个可以匹配的消息抵达
 * 5.邮箱会串行化消息，actor运行在单个线程中。他会先接受一条消息，然后接受下一条消息，不需要再actor的代码中担心争用的情况
 * 6.actor可以安全的修改自己的数据，但是如果他修改了在不同actor之间共享的数据，那么争用状况就可能出现。
 * 因此，不要在不同的actor中使用共享的对象--除非你知道对这个对象的访问时线程安全的。理想情况下actor除了自己的状态之外不应该
 * 方根或修改其他任何状态。
 */

case class Charge(creditCardNumber:Long,merchart:String,amount:Double)
case class Deposit(amount:Double)
case class Withdraw(amount:Double)

class HiActor extends Actor {
  private var balance:Double = 0.0
  
  //重载异常处理。默认情况下异常抛出，将会导致整个程序终止，如果有特殊需要，可以通过重载exceptionHandler方法来结合actor的控制语句来实现
  override def exceptionHandler: PartialFunction[Exception, Unit] = {case e:Exception => println("found error,but ignore this error,and continue")}
 
  def act(): Unit = {
    loop { //此处需要在抛出异常后，actor仍然可以接收messge，即继续执行下一个迭代。此时只能用loop而不能使用while(true)才有作用
      receive {
        case Charge(m,b,c) => {
          println("Charge!"+Thread.currentThread().getName);
          balance -=c;
          println("current mailBox size :"+mailboxSize)
          throw new Exception("cant not process")
          }
        case Deposit(amount) => {
          println("Deposit!"+Thread.currentThread().getName);
          balance += amount
          println("current mailBox size :"+mailboxSize)
          }
        case Withdraw(amount) => {
          println("Withdraw!"+Thread.currentThread().getName);
          balance -= amount
          println("current mailBox size :"+mailboxSize)
          }
        case _ => {
          println("unkown message"+Thread.currentThread().getName);
          Thread.sleep(1000);
          println("current mailBox size :"+mailboxSize)
          }
      } andThen {
        println(" and then")
      }
    }
  }
}

/**
 * 2.向其他Actor发送消息
 *      当运算被分拆到不同的actor来并行处理问题的各个部分时，这些处理结果需要被收集到一起。actor可以将结果存入到一个线程安全的数据结构中，
 *  比如一个并发的哈希映射，但是actor模型不鼓励使用共享数据。因而，当actor运算结束后，应该向另一个actor发送消息。
 *      一个actor如何知道应该往哪里发送计算结果。
 *         a)使用全局的actor，不过当actor数量很多的时候，伸缩性不好。
 *         b)actor可以构造出带有指向一个或多个actor的引用
 *         c)actor可以接受带有指向另一个actor的引用的消息。在请求中提供一个actor引用：
 *            actor ! Compute(data,continuation)
 *            这里的continuation是另一个actor，当actor计算完结果后，应该调用该continuation actor.
 *         d)actor可以返回消息给发送方。receive方法会把sender字段设为当前消息的发送方。
 */


/**
 * 3.消息通道
 * 除了在程序中对actor共享引用的做法，还可以共享消息通道给他们。
 * 这样做的好处：
 *  1）消息通道是类型安全的----你只能发送或接受某个特定类型的消息
 *  2）你不会不小心通过消息通道调用到某个actor的方法
 *  消息通道可以使一个OutputChannel(带有!方法),也可以是一个InputChannel（带有receive或react方法），Channel类同时扩展OutputChannel和InputChannel特质。
 *  
 *  要构造一个消息通道，可以提供一个actor:
 *    val channel = new Channel[Int](someActor)
 *    如果不提供构造参数，消息通道会绑定到当前执行的这个actor上。
 *    通常你会告诉某个actor发送结果到一个输出消息通道。
 */
 case class Compute(input:Seq[Int],result:OutputChannel[Int])
 class Computer extends Actor {
   def act():Unit = {
     while(true) {
       receive {
         case Compute(input,out) => {println("Computer receive !");val answer = input.length; out ! answer }
       }
     }
   }
 }

 
/**
 * 同步消息 和 Future
 * actor可以发送一个消息并等待回复用!?操作符即可。
 */
 

/**
 * 共享线程
 * 
 *      考虑一个发送消息到另个一actor的actor,如果每个actor都在单独的线程中运行，很容易实现控制流转。
 *   作为消息发送方的actor将消息放到邮箱中，然后他的线程继续执行，而每当有条目被放入邮箱中，作为消息接收方
 *   的actor的线程就会被唤醒。
 *      有些程序包含的actor很多，至于要为每个actor创建单独的线程开销很大。因此有时希望在同一个线程中运行多个actor.
 *   假定actor的大部分时间用于等待消息，与其让每个actor在单独的线程中阻塞，不如用一个线程来执行多个actor的消息处理
 *   函数。这样做的前提是每个消息处理函数只需要做比较小规模的工作，然后继续等待下一条消息。
 *      在scala中可以使用react方法，react方法接收一个偏函数，并将它添加到邮箱，然后退出，假定有两个嵌套的react语句：
 *      react { //偏函数f1
 *        case Withdraw(amount) =>
 *        react { //偏函数f2
 *          case Comfirm() =>
 *              println("Comfirming "+ amount)
 *        }
 *      }
 *      
 *      第一个react的调用将f1与邮箱关联起来，然后退出。当Withdraw消息抵达时，f1被调用。
 *      偏函数f1也调用react，这次调用将f2与actor的邮件关联起来；然后退出，当Confirm消息
 *      抵达时，f2被调用。
 *      
 *      与第一个react关联的偏函数不会返回一个值---它执行了某些工作，然后执行下一个react,这就造成
 *      它自己的退出。退出意味着返回到协调actor的方法中。这样的一个函数的返回类型为Nothing,该类型
 *      用来表示非正常退出。
 *      
 *      由于react会退出，因此不能将它简单地放在while循环当中。
 *      
 *      def act(){
 *          while(true){
 *              react{  //偏函数f1
 *                case Withdraw(amount) => println("withdrawing :"+amount)
 *              }
 *          }
 *      }
 *      
 *      当act被调用的时候，对react的调用将f1与邮箱关联起来，然后退出。当f1被调用时，他们将会处理这条消息。不过
 *      f1没有任何办法返回到循环中---他只不过是一个小小的函数：
 *        { case Withdraw(amount) => println("withdrawing :"+amount)}
 *        解决办法之一是在消息处理器中再次调用act方法：
 *        def act(){
 *           react{  //偏函数f1
 *                case Withdraw(amount) => {
 *                    println("withdrawing :"+amount)
 *                    act()
 *                }
 *              }
 *        }
 *        
 *        这样做，意味着一个无穷递归替换掉无穷循环。这个递归不会占用很大的栈空间，因为每次对react的调用都是抛出异常，从而请栈。
 *        
 *        让每个消息处理器自己负责爆出循环继续运行下去并不很公平，有些控制流转组合子可以自动产出这些循环。
 *        
 *        loop组合子可以自动产出这些循环：
 *        def act(){
 *          loop {
 *            react{  //偏函数f1
 *                case Withdraw(amount) => {
 *                    println("withdrawing :"+amount)
 *                }
 *              }
 *          }
 *        }
 *        
 *        如果需要一个循环条件，可以使用loopWhile
 */
object mainClass23 extends App {
   
  //hiActorTest
  //test
  //computerActorTest
  //synchronizedMessage
  //receiveWithinTest
  //futureTest
  
  checkMailBox
  
  def test(){
    val a = "s"
    val b = "s"
    if( a eq b ) println("match eq")
    if( a equals b ) println("match equals")
    if( a == b ) println("match ==")
  }
  
  /**
   * 查看mailbox中message的个数
   */
  def checkMailBox() = {
    val actor_01 = actor{
      while(true){
         receive{
        case "T" => {println("current mail box size is :"+mailboxSize +" and current Thread is:"+Thread.currentThread().getName);}
        case _ => println("Nothing")
      }
      }
    }
    
    
    println("begin to send")
    actor_01 ! "T"
    Thread.sleep(2000)
    actor_01 ! "T"
    Thread.sleep(2000)
    actor_01 ! "f"
     println("end send")
  }
  /**
   * 接收一个future,将在结果可用时产出结果的对象。使用 !! 方法即可做到：
   */
  def futureTest() {
    val actor_01 = actor {
      while(true) {
        receive{
          case "F" => {println("F");Thread.sleep(3000);println(self.getClass.getName);println(sender.getClass.getName);sender ! "TEST"}
        }
      }
    }
    
    val result = actor_01 !! "F"
    
    while(!result.isSet) { //检查结果是否可用
      println("result is unavailable")
      Thread.sleep(1000)
    }
    println(result()) //如果结果可用，可以使用函数调用的表示法来获取结果。这个调用将会阻塞，知道回复被发送
   
  }
  /**
   * 在某个时间内接收
   */
  def receiveWithinTest() {
    val actor_01 = actor {
     while(true) {
      receiveWithin(2*1000) {
        case "T" => println("T" + Thread.currentThread().getName)
        case TIMEOUT => println("Timeout" + Thread.currentThread().getName)
      }
     }
    }
    val actor_02 = actor {
      actor_01 ! "T"
      receiveWithin(3*1000) {
        case "F"=> println("F" + Thread.currentThread().getName)
        case TIMEOUT => println("actor_02 timeout" + Thread.currentThread().getName)
      }
    }
    
    actor_01.start
    actor_02.start
    actor_01 ! "T"
  }
  /**
   * 同步消息和Futrue
   */
  def synchronizedMessage() {
    val actor_1 = actor{
      while(true) {
        receive {
          case Deposit(amount) => {println("replay received!"); sender ! Withdraw(12)}
        }
      }
    }
    
    val reply = actor_1 !? Deposit(12.3)
    println(".......")
    reply match {
      case Withdraw(amount) => println("received withdraw："+amount)
    }
    
    actor_1.start
  }
  
  /*** 消息通道*/
  def computerActorTest {
    val computer = actor {
    val channel = new Channel[Int]
    val computerActor = new Computer
    computerActor.start
    val input:Seq[Int] = Seq(12,3,3,4,21,4)
    computerActor ! Compute(input,channel)
    
    /**
     * 此处我们调用的是channel的recieve方法，而不是actor自己。如果想通过actor来接受响应，那么可以匹配一个!样例类的实例，就像这样：
     *  receive{
     *      case !(channel,x) => ...  //此时的case匹配的是： !(scala.actors.Channel@7c472bc,6) 样例类
     *  }
     */
    channel.receive {
      case x => println("channel receive:"+x)
      }
    }
    
    computer.start
  }
  
  def hiActorTest() {
      val hiActor = new HiActor
      hiActor.start()
      hiActor ! "Hi"
      hiActor ! Charge(323232323,"Freada`sS And Tackle",12.3)
      hiActor ! Deposit(323232323.2)
      hiActor ! Withdraw(11323.2)
      
      val sa:String= s"strat"
  }
  /**
   * 匿名Actor的构造
   */
  def anonymousActor() {
    
  val actor2 = actor {
    while(true) {
      receive {
        case "Hi" =>{println("actor2:Hello2"); Thread.sleep(6000)}
      }
    }
  }
  actor2.start
  actor2 ! "Hi"
  actor2 ! Charge(323232323,"Freada`sS And Tackle",12.3)
  }
}