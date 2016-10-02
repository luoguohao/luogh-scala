package com.luogh.scala.test.scalabook

/**
  * 类型Option
  *
  * @author luogh 
  * @date 2016/10/2
  */
object OptionType {

  val absentGreeting:Option[String] = Option(null)  //None
  val presentGreeting:Option[String] = Option("Hello") // Some("Hello")

  ///////////////////////////////////////////////////////////////////////////////////////
  ///////////////////////////////////提供默认值//////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////////
  case class User(
                   id: Int,
                   firstName: String,
                   lastName: String,
                   age: Int,
                   gender: Option[String]
                 )

  object UserRepository {
    private val users = Map(1 -> User(1, "John", "Doe", 32, Some("male")),
      2 -> User(2, "Johanna", "Doe", 30, None))
    def findById(id: Int): Option[User] = users.get(id)
    def findAll = users.values
  }

  val user = User(2, "Johanna", "Doe", 30, None)

  /**
    * 请注意，作为getOrElse参数的默认值是一个传名参数,这意味着，只有当这个Option确实是None的
    * 时候，传名参数才会被求值。因此，没有必要担心创建默认值的代价，他只有在需要时才发生
    */
  println("Gender: " + user.gender.getOrElse("not specified")) // will print "not specified"


  ///////////////////////////////////////////////////////////////////////////////////////
  ///////////////////////////////////作为集合的Option/////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////////

  /**
    * Option是类型A的容器，更确切的说，可以把它当做某种集合。这个特殊的集合要么只包含一个元素，
    * 要么什么元素都没有。
    * 在类型层次上，Option并不是Scala的集合类型，但凡你觉得Scala集合好用的方法，Option都有，
    * 甚至可以将其转为一个集合，比如List.
    */

  ///////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////作为集合的Option -- 执行一个副作用//////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////////

  /**
    * 如果想在Option有值的时候执行一个副作用，那么可以使用foreach
    *
    * 如果这个Option是一个Some，传递给foreach的函数就会被调用一次，且只有一次，
    * 如果是None，那么就不会调用
    */
  UserRepository.findById(2).foreach(user=>println(user.firstName))


  ///////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////作为集合的Option -- 执行映射////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////////

  /**
    * 正如你可以将 List[A] 映射到 List[B] 一样，你也可以映射 Option[A] 到 Option[B]：
    * 如果 Option[A] 实例是 Some[A] 类型，那映射结果就是 Some[B] 类型；否则，就是 None 。
    * 如果将 Option 和 List 做对比 ，那 None 就相当于一个空列表： 当你映射一个空的 List[A],
    * 会得到一个空的 List[B] ， 而映射一个是 None 的 Option[A] 时，得到的 Option[B] 也是 None
    */
  val age = UserRepository.findById(1).map(_.age) // age is Some(32)


  ///////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////作为集合的Option -- flatMap ////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////////

  val gender = UserRepository.findById(1).map(_.gender) // gender is an Option[Option[String]]
  /**
    * 既然可以 flatMap 一个 List[List[A]] 到 List[B] ， 也可以 flatMap 一个 Option[Option[A]]
    * 到 Option[B] ，这没有任何问题： Option 提供了 flatMap 方法。
    */
  val gender1 = UserRepository.findById(1).flatMap(_.gender) // gender is Some("male")
  val gender2 = UserRepository.findById(2).flatMap(_.gender) // gender is None
  val gender3 = UserRepository.findById(3).flatMap(_.gender) // gender is None


  ///////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////作为集合的Option -- 过滤Option//////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////////

  UserRepository.findById(1).filter(_.age > 30) // None, because age is <= 30
  UserRepository.findById(2).filter(_.age > 30) // Some(user), because age is > 30
  UserRepository.findById(3).filter(_.age > 30) // None, because user is already None


  ///////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////作为集合的Option -- For语句/////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////////

  /**
    * 用 for 语句来处理 Option 是可读性最好的方式，尤其是当你有多个 map 、flatMap 、filter
    * 调用的时候。 如果只是一个简单的 map 调用，那 for 语句可能有点繁琐
    */
  val gender_2 = for {
    user <- UserRepository.findById(1)
    gender <- user.gender
  } yield gender // results in Some("male")

  val genders = for {
    user <- UserRepository.findAll
    gender <- user.gender
  } yield gender // result in List("male")


  ///////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////作为集合的Option -- 在生成器左侧使用////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////////

  /**
    * for 语句中生成器的左侧也是一个模式。 这意味着也可以在 for 语句中使用包含选项的模式。
    * 在生成器左侧使用 Some 模式就可以在结果集中排除掉值为 None 的元素。
    */
  val genders_2 = for(User(_,_,_,_,Some(ge)) <- UserRepository.findAll if ge.length > 3) yield gender


  ///////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////作为集合的Option -- 链接 Option/////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////////

  /**
    * Option 还可以被链接使用，这有点像偏函数的链接： 在 Option 实例上调用 orElse 方法，
    * 并将另一个 Option 实例作为传名参数传递给它。 如果一个 Option 是 None ， orElse 方法会
    * 返回传名参数的值，否则，就直接返回这个 Option。一个很好的使用案例是资源查找：对多个不同
    * 的地方按优先级进行搜索。 下面的例子中，我们首先搜索 config 文件夹，并调用 orElse 方法，
    * 以传递备用目录
    * 如果想链接多个选项，而不仅仅是两个，使用 orElse 会非常合适。 不过，如果只是想在值缺失的
    * 情况下提供一个默认值，那还是使用 getOrElse。
    */
  case class Resource(content: String)
  val resourceFromConfigDir: Option[Resource] = None
  val resourceFromClasspath: Option[Resource] = Some(Resource("I was found on the classpath"))
  val resource = resourceFromConfigDir orElse resourceFromClasspath



  def main(args:Array[String]) :Unit = {
    OptionType
  }
}
