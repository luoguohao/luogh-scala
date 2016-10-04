package com.luogh.scala.test.scalabook

/**
  * 柯里化和部分函数应用
  *
  * 两种函数重用的机制：函数的部分应用(Partial Application of Functions) 、 柯里化(Currying)
  * @author luogh 
  */
object CurryingAndPartiallyAppliedFunctions {

  ///////////////////////////////////////////////////////////////////////////////////////
  //////////////////////// 部分应用的函数 ////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////////

  /**
    *  调用一个函数时，不是把函数需要的所有参数都传递给它，而是仅仅传递一部分，其他参数留空;
    *  这样会生成一个新的函数，其参数列表由那些被留空的参数组成。
    */

  // 假想的免费邮件服务，能够让用户配置筛选器，以使得满足特定条件的邮件显示在收件箱里，
  // 其他的被过滤掉。
  case class Email(
                    subject: String,
                    text: String,
                    sender: String,
                    recipient: String)
  type EmailFilter = Email => Boolean

  /**
    * 过滤邮件的条件用谓词 Email => Boolean 表示， EmailFilter 是其别名。 调用适当的工厂
    * 方法可以生成这些谓词。
    *
    * 我们创建了两个这样的工厂方法，它们检查邮件内容长度是否满足给定的最大值或最小值。
    * 这一次，我们使用部分应用函数来实现这些工厂方法，做法是，修改 sizeConstraint ，
    * 固定某些参数可以创建更具体的限制条件
    */

  type IntPairPred = (Int,Int) => Boolean
  def sizeConstraint(pred:IntPairPred,n:Int,email:Email) =
    pred(email.text.size,n)

  //遵循 DRY 原则，我们先来定义常用的 IntPairPred 实例
  val gt:IntPairPred = _ > _
  val ge: IntPairPred = _ >= _
  val lt: IntPairPred = _ < _
  val le: IntPairPred = _ <= _
  val eq: IntPairPred = _ == _

  /**
    * 对所有没有传入值的参数，必须使用占位符 _ ，还需要指定这些参数的类型，
    * 这使得函数的部分应用多少有些繁琐。 Scala 编译器无法推断它们的类型，
    * 方法重载使编译器不可能知道你想使用哪个方法。
    */
  val minimumSize: (Int, Email) => Boolean = sizeConstraint(ge, _: Int, _: Email)
  val maximumSize: (Int, Email) => Boolean = sizeConstraint(le, _: Int, _: Email)

  def main(args:Array[String]):Unit = {
    CurryingAndPartiallyAppliedFunctions
  }
}
