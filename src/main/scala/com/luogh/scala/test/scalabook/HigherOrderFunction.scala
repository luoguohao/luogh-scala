package com.luogh.scala.test.scalabook

/**
  * 高阶函数与 DRY(dont repeat yourself) 与 函数组合
  *
  *  Scala容器类型的可组合性特征,Scala中的一等公民——函数也具有这一性质。
  *  组合性产生可重用性,虽然后者是经由面向对象编程而为人熟知,但它也绝对是纯函数的固有性质.
  *  (纯函数是指那些没有副作用且是引用透明的函数）
  *  一个明显的例子是调用已知函数实现一个新的函数,当然,还有其他的方式来重用已知函数,这一章
  *  会讨论函数式编程的一些基本原理.你将会学到如何使用高阶函数,以及重用已有代码时,遵守 DRY 原则。
  *
  *  代码重复：提升现有的函数功能、或者将函数进行组合
  * @author luogh 
  */
object HigherOrderFunction {

  /////////////////////////////////////////////////////////////////////////////////////
  ///////////////////////////////  高阶函数  //////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////

  /**
    * 高阶函数三种形式:
    *   1.一个或多个参数是函数，并返回一个值,如map，filter,flatmap函数。
    *   2.返回一个函数，但没有参数是函数
    *   3.上述两者叠加:一个或多个参数是函数，并返回一个函数
    */

  /////////////////////////////////////////////////////////////////////////////////////
  ///////////////////////////////  函数生成  //////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////

  case class Email(
                    subject: String,
                    text: String,
                    sender: String,
                    recipient: String
                  )

  type EmailFilter = Email => Boolean

  def newEmailsForUser(mails:Seq[Email],f:EmailFilter) = mails.filter(f)

  // 产生EmailFilter的工厂方法:
  // 这四个 vals 都是可以返回 EmailFilter 的函数,
  // 前两个接受代表发送者的 Set[String] 作为输入，后两个接受代表邮件内容长度的 Int 作为输入。
  val sendByOneOf:Set[String]=>EmailFilter = {
    senders =>
      email => senders.contains(email.sender)
  }

  val notSentByAnyOf: Set[String] => EmailFilter = {
    senders =>
      email => !senders.contains(email.sender)
  }

  val minimumSize: Int => EmailFilter = {
    n =>
      email => email.text.size >= n
  }

  val maximumSize: Int => EmailFilter = {
    n =>
      email => email.text.size <= n
  }
  val emailFilter : EmailFilter = notSentByAnyOf(Set("jonedoe@example.com"))
  val mails = Email(
    subject = "It's me again, your stalker friend!",
    text = "Hello my friend! How are you?",
    sender = "johndoe@example.com",
    recipient = "me@example.com") :: Nil

  newEmailsForUser(mails, emailFilter) // returns an empty list


  /////////////////////////////////////////////////////////////////////////////////////
  ///////////////////////////////  重用已有函数   /////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////

  /**
    * 上述工厂方法中有重复代码.上文提到过,函数的组合特征可以很轻易的保持DRY原则
    * 对于minimumSize和maximumSize,我们引入一个叫做 sizeConstraint 的函数。
    * 这个函数接受一个谓词函数，该谓词函数检查函数内容长度是否OK，邮件长度会通过参数传递给它
    */
  type SizeChecker = Int => Boolean
  val sizeConstraint:SizeChecker=>EmailFilter = {
    f =>
      email => f(email.text.size)
  }

  // 我们就可以用 sizeConstraint 来表示 minimumSize 和 maximumSize 了
  val minimumSize1 : Int =>EmailFilter = {
    n =>
      sizeConstraint(_ >= n)
  }
  val maxmumSize1 : Int => EmailFilter = {
    n =>
      sizeConstraint(_ <= n)
  }

  def minimumSize2(n:Int):EmailFilter = {
      sizeConstraint(_ >= n)
  }


  /////////////////////////////////////////////////////////////////////////////////////
  ///////////////////////////////  函数组合   /////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////

  /**
    * 为另外两个谓词（sentByOneOf、 notSentByAnyOf）介绍一个通用的高阶函数，通过它,可以用
    * 一个函数去表达另外一个函数。这个高阶函数就是complement,给定一个类型为 A => Boolean
    * 的谓词，它返回一个新函数， 这个新函数总是得出和谓词相对立的结果：
    *  def complement[A](predicate: A => Boolean) = (a: A) => !predicate(a)
    *
    * 现在,对于一个已有的谓词p,调用complement(p)可以得到它的补.然而,sentByAnyOf并不是一个
    * 谓词函数,它返回类型为EmailFilter的谓词.Scala函数的可组合能力现在就用的上了:给定两个函数
    * f、g, f.compose(g)返回一个新函数,调用这个新函数时,会首先调用g,然后应用 f 到 g 的返回结果上
    * 类似的, f.andThen(g)返回的新函数会应用 g 到 f 的返回结果上。
    */
  def complement[A](predicate: A => Boolean): A=>Boolean = (a: A) => !predicate(a)
  val notSendByAnyOf = sendByOneOf andThen(g => complement(g))

  /**
    * 面的代码创建了一个新的函数,这个函数首先应用sentByOneOf到参数Set[String]上，
    * 产生一个 EmailFilter 谓词, 然后，
    * 应用 complement 到这个谓词上。 使用 Scala 的下划线语法，这短代码还能更精简
    */
  val notSentByAnyOf1 = sendByOneOf andThen (complement(_))


  /////////////////////////////////////////////////////////////////////////////////////
  ///////////////////////////////  谓词组合   /////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////

  /**
    * 邮件过滤器的第二个问题是，当前只能传递一个 EmailFilter 给 newMailsForUser 函数，
    * 而用户必然想设置多个标准。 所以需要可以一种可以创建组合谓词的方法，这个组合谓词可
    * 以在任意一个标准满足的情况下返回 true ，或者在都不满足时返回 false 。
    */

  def any[A](predicates:(A=>Boolean) *): A=>Boolean = {
    a => predicates.exists(pred => pred(a))
  }
  def none[A](predicates:(A=>Boolean) *):A=>Boolean = {
    complement(any(predicates:_*))
  }
  def every[A](predicates:(A=>Boolean) *):A=>Boolean = {
    none(predicates.view.map(complement(_)): _*)
  }

  /**
    * any 函数返回的新函数会检查是否有一个谓词对于输入 a 成真。
    * none 返回的是 any 返回函数的补，只要存在一个成真的谓词， none 的条件就无法满足。
    * 最后， every 利用 none 和 any 来判定是否每个谓词的补对于输入 a 都不成真。
    *
    * 可以使用它们来创建代表用户设置的组合 EmialFilter
    */

  val filter: EmailFilter = every(
    notSentByAnyOf(Set("johndoe@example.com")),
    minimumSize(100),
    maximumSize(10000)
  )

  /////////////////////////////////////////////////////////////////////////////////////
  ///////////////////////////////  流水线组合 /////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////

  /**
    * 再举一个函数组合的例子。回顾下上面的场景， 邮件提供者不仅想让用户可以配置邮件过滤器，
    * 还想对用户发送的邮件做一些处理。 这是一些简单的 Emial => Email 函数
    */

  val addMissingSubject = (email:Email) =>
    if(email.subject.isEmpty) email.copy(subject = "No subject")
    else email
  val checkSpelling = (email: Email) =>
    email.copy(text = email.text.replaceAll("your", "you're"))
  val removeInappropriateLanguage = (email: Email) =>
    email.copy(text = email.text.replaceAll("dynamic typing", "**CENSORED**"))
  val addAdvertismentToFooter = (email: Email) =>
    email.copy(text = email.text + "\nThis mail sent via Super Awesome Free Mail")

  /**
    * 现在，根据老板的心情，可以按需配置邮件处理的流水线。 通过 andThen 调用实现，
    * 或者使用 Function 伴生对象上的 chain 方法
    */
  val pipeline = Function.chain(Seq(
    addMissingSubject,
    checkSpelling,
    removeInappropriateLanguage,
    addAdvertismentToFooter))

  def main(args:Array[String]):Unit = {
    HigherOrderFunction
  }
}
