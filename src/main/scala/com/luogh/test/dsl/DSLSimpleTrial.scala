package com.luogh.test.dsl

import com.luogh.test.dsl.common.{Deduction, Deductions, Dollars, Percentage}
import com.luogh.test.dsl.dslObj.PayrollParser
import scala.util.parsing.combinator.JavaTokenParsers

/**
  * Parser Combinator 解析组合子
  *   1) 文法定义中的二选一、拼接、选项和重复 在scala组合子解析器中对应为 |、~、opt、rep
  *   2) 对于RegexParser而言，字符串字面量和正则表达式匹配的是词法单元
  *   3) 用 ^^ 来处理解析结果
  *   4) 在提供给^^的函数中使用模式匹配来将 ~ 结果拆开
  *   5) 用 ~> 或者 <~ 来丢弃那些在匹配后不再需要的词法单元,(~侧的词法单元将被丢弃)
  *   6) repsep组合子处理那些常见的用分隔符分隔开来的条目
  *   7) 基于词法单元的解析器对于解析那种带有保留字和操作符的语言很有用。可以定义自己的词法分析器
  *   8) 解析器是消费读取器并产出解析结果: 成功、失败或者错误的函数
  *   9) Failure结果提供了用于错误报告的明细信息
  *   10) 添加failure语句到文法当中来改进错误提示的质量
  *   11) 凭借操作符符号、隐式转换和模式匹配，解析器组合子类库让任何能理解无上下文文法的人都可以很容易编写解析器
  *
  * @author luogh
  */
object DSLSimpleTrial {
    def main(args: Array[String]): Unit = {
      val input =
        """biweekly {
          | federal tax 20.0 percent,
          | state tax 3.0 percent,
          | insurance premiums 250.0 dollars,
          | retirement savings 15.0 percent
          |}""".stripMargin

      val parser = new PayrollParser
      val biweeklyDeductions = parser.parseAll(parser.biweekly, input).get
      println(biweeklyDeductions)

      val annualGross = 100000.0
      val gross = biweeklyDeductions.gross(annualGross)
      val net = biweeklyDeductions.net(annualGross)
      println(f"Biweekly pay(annual: $$${annualGross}%.2f)")
      println(f"Gross: $$${gross}%.2f, Net: $$${net}%.2f")
    }
}

object common {
  sealed trait Amount {
    def amount: Double
  }

  case class Percentage(amount: Double) extends Amount {
    override def toString = s"$amount%"
  }

  case class Dollars(amount: Double) extends Amount {
    override def toString = s"$$$amount"
  }

  implicit class Units(amount: Double) {
    def percent = Percentage(amount)
    def dollars = Dollars(amount)
  }

  case class Deduction(name: String, amount: Amount) {
    override def toString = s"$name : $amount"
  }

  case class Deductions(name: String, divisorFromAnnualPay: Double = 1.0, var deductions: Vector[Deduction] = Vector.empty) {
    def gross(annualSalary: Double): Double =
      annualSalary / divisorFromAnnualPay

    def net(annualSalary: Double): Double = {
      val g = gross(annualSalary)
      (deductions foldLeft g ) {
        case (total, Deduction(deduction, amount)) => amount match {
          case Percentage(value) => total - (g * value / 100.0)
          case Dollars(value) => total - value
        }
      }
    }

    override def toString = s"$name Deductions: "+ deductions.mkString("\n ", "\n ","")
  }
}

object dslObj {

  class PayrollParser extends JavaTokenParsers {

    /** @return  Parser[(Deductions)] **/
    def biweekly = "biweekly" ~> "{" ~> deductions <~ "}" ^^ { ds =>
      Deductions("Biweekly", 26.0, ds)
    }

    /** @return Parser[Vector(Deduction)] **/
    def deductions = repsep(deduction, ",") ^^ { ds =>
      ds.foldLeft(Vector.empty[Deduction])( _ :+ _)
    }

    /** @return Parser[Deduction] **/
    def deduction = federal_tax | state_tax | insurance | retirement

    /** @return Parser[Deduction] **/
    def federal_tax = parseDeduction("federal", "tax")
    def state_tax = parseDeduction("state", "tax")
    def insurance = parseDeduction("insurance", "premiums")
    def retirement = parseDeduction("retirement", "savings")

    private def parseDeduction(word1: String, workd2: String) =
      word1 ~> workd2 ~> amount ^^ {
        amount => Deduction(s"$word1 $workd2", amount)
      }

    /** @return Parser[Amount] **/
    def amount = dollars | percentage

    /** @return Parser[Dollar]**/
    def dollars = doubleNumber <~ "dollars" ^^ { d => Dollars(d) }

    /** @return Parser[Percent]**/
    def percentage = doubleNumber <~ "percent" ^^ { d => Percentage(d) }

    def doubleNumber = floatingPointNumber ^^ (_.toDouble)
  }
}