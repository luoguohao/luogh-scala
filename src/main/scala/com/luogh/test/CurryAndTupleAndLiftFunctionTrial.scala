package com.luogh.test

/**
  * @author luogh
  *
  *  curry Function and tuple Function and lift function
  */
object CurryAndTupleAndLiftFunctionTrial {


  //////////////// 柯里化函数与 非柯里化函数的转换 /////////////////////////////
  def cat3(a: String, b: String): String = a + b

  val curried_cat3: String => (String => String) = (cat3 _).curried // 将普通函数转为柯里化函数
  val partial_curried_cat3 : String => String = curried_cat3("a")  // 部分应用函数

  val uncurried_cat3: (String,String) => String = Function.uncurried(curried_cat3) // 将柯里化函数转为非柯里化函数


  ////////////// tuple 参数列表与 普通参数列表的转化 //////////////////////////////
  // 当一个函数参数是一个Tuple的时候
  def cat4(a: (Int,Int)): Int = a._1


  val tupled_cat3: ((String,String)) => String = (cat3 _).tupled  // 将普通函数转为元组类型的参数的函数
  val result: String = tupled_cat3(("a","b")) // 传递元组
  val untupled_cat3: (String,String) => String = Function.untupled(tupled_cat3)
  tupled_cat3("a","b")

  ///////////////////////// 偏函数 与 偏函数的转化 //////////////////////////////

  val par_func: PartialFunction[Int,String] =  {
    case num: Int if num > 0 => "a"
  }

  par_func(12)
//  par_func(-1) // scala.MatchError Exception,因为par_func是偏函数，并且只处理num > 0的情况，当num<=0,匹配失败。

  // 将偏函数 PartialFunction[Int,String] 转为 Function[Int,Option[String]]
  val option_par_func: Int => Option[String] = par_func.lift
  val para_funct: PartialFunction[Int, String] = Function.unlift(option_par_func)
  println(option_par_func(-1))

  def main(args: Array[String]): Unit = {

  }

}
