package com.luogh.test


/**
  * @author luogh
  *
  *         使用Phantom type(既没有任何instance的type,这种类型只是用于类型标识)来实现工作流。
  *         使用Pipeline Style 编写优雅的工作流代码
  *
  *         实例代码以税收业务说明，税收包含税前、税后流程。因此，这是一个流程约束，顺序不能调整。
  */
object WorkFlowTrial {

  sealed trait PreTaxDeductions   // phantomType ,使用sealed,说明没有不需要用户提供任何的实现，只是用于流程控制

  sealed trait PostTaxDeductions  // phantomType ,使用sealed,说明没有不需要用户提供任何的实现，只是用于流程控制

  sealed trait Final              // phantomType ,使用sealed,说明没有不需要用户提供任何的实现，只是用于流程控制

  case class Employee(
                       name: String,
                       annualSalary: Float,
                       taxRate: Float,
                       insurancePremiumsPerPayPeriod: Float,
                       _401kDeductionRate: Float,
                       postTaxDeductions: Float
                     )

  case class Pay[Step](employee: Employee, netPay: Float)

  object PayRoll {
    def start(employee: Employee): Pay[PreTaxDeductions] = Pay[PreTaxDeductions](employee,employee.annualSalary / 26.0F)

    def minusInsurance(pay: Pay[PreTaxDeductions]): Pay[PreTaxDeductions] = {
      val newNet = pay.netPay - pay.employee.insurancePremiumsPerPayPeriod
      pay copy (netPay = newNet)
    }

    def minus401k(pay: Pay[PreTaxDeductions]): Pay[PreTaxDeductions] = {
      val newNet = pay.netPay - (pay.employee._401kDeductionRate * pay.netPay)
      pay copy (netPay = newNet)
    }

    def minusTax(pay: Pay[PreTaxDeductions]): Pay[PostTaxDeductions] = {
      val newNet = pay.netPay - (pay.employee.taxRate * pay.netPay)
      pay copy (netPay = newNet)
    }

    def minusFinalDeductions(pay: Pay[PostTaxDeductions]): Pay[Final] = {
      val newNet = pay.netPay - pay.employee.postTaxDeductions
      pay copy (netPay = newNet)
    }
  }

  //使用隐式类定义PipeLine
  object PipeLine {
      implicit class toPipe[V](v: V) {
        def >|[M](f: V => M): M = f(v)
      }
  }
}
object CalculatePayroll {
  def main(args: Array[String]): Unit = {
    beautifyWorkFlowUsingPipeLine()
  }


  def unbeautifyWorkFlow(): Unit = {
    import WorkFlowTrial._
    val e = Employee("Buck Trends", 10000.0F, 0.25F, 200F, 0.10F, 0.05F)
    val pay1 = PayRoll start e         // Pay[WorkFlowTrial.PreTaxDeductions]
    val pay2 = PayRoll minus401k pay1  // Pay[WorkFlowTrial.PreTaxDeductions]
    val pay3 = PayRoll minusInsurance pay2  // Pay[WorkFlowTrial.PreTaxDeductions]
    val pay4 = PayRoll minusTax pay3   // Pay[WorkFlowTrial.PostTaxDeductions]
    val pay5 = PayRoll minusFinalDeductions pay4  // Pay[WorkFlowTrial.Final]

    val twoWeekGross = e.annualSalary / 26.0F
    val twoWeekNet = pay5.netPay
    val percent = (twoWeekNet / twoWeekGross) * 100
    println(s"For ${e.name}, the gross vs. net pay every 2 weeks is :")
    println(f" $$${twoWeekGross}%.2f vs. $$${twoWeekNet}%.2f or ${percent}%.1f%%")
  }

  /**
    * 使用PipeLine风格的代码来建立优雅的代码
    */
  def beautifyWorkFlowUsingPipeLine(): Unit = {
    import WorkFlowTrial._
    import PayRoll._
    import PipeLine._
    val e = Employee("Buck Trends", 10000.0F, 0.25F, 200F, 0.10F, 0.05F)
    val pay1 = start(e)      >|        // Pay[WorkFlowTrial.Final]
              minus401k      >|
              minusInsurance >|
              minusTax       >|
              minusFinalDeductions

    val twoWeekGross = e.annualSalary / 26.0F
    val twoWeekNet = pay1.netPay
    val percent = (twoWeekNet / twoWeekGross) * 100
    println(s"For ${e.name}, the gross vs. net pay every 2 weeks is :")
    println(f" $$${twoWeekGross}%.2f vs. $$${twoWeekNet}%.2f or ${percent}%.1f%%")
  }
}

