package com.luogh

import java.io.{FileOutputStream, PrintWriter}

import com.luogh.DDCStat.TaskStatus.TaskStatus
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.io.Source

/**
  * @author luogh 
  */
object DDCStat {

  case class RowData(jobId: Int, email: String, serial_no: Int, startTime: String, taskType: String, params: String, resultCount: Int, status: TaskStatus)

  case object TaskStatus extends Enumeration {
    type TaskStatus = Value
    val CREATED = Value(0, "已创建")
    val QUEUEING = Value(1, "排队中")
    val SCHEDULED = Value(2, "已调度")
    val SUBMITTED = Value(3, "已提交")
    val RUNNING = Value(4, "运行中")
    val SUCCEEDED = Value(5, "已成功")
    val FAILED = Value(6, "已失败")

    def status(statusType: Int): TaskStatus = {
      // throw Exception expression return type is Nothing. Nothing in scala is the subtype of all other types in scala.
      values.find(_.id == statusType).getOrElse(throw new IllegalStateException("未知状态"))
    }
  }

  def stat(fileName: String = "C:\\Users\\luogh\\Desktop\\ddc_stat_20161015"): Iterator[DDCStat.RowData] = {
    val file = Source.fromFile(fileName)
    // file schema: job_id  email	serial_no	start_time	params	result_count	status
    val rowDetailList = file.getLines().drop(1).map {
      line =>
        val arr = line.split("\t")
        RowData(arr(0).toInt, arr(1), arr(2).toInt, arr(3), getTaskTypeByJson4s(arr(4)), arr(4), arr(5).toInt, TaskStatus.status(arr(6).toInt))
    }.map {
      case _@rowData if rowData.taskType == "中间结果合并筛选" =>
        val JString(req) = parse(rowData.params) \ "requestUrl"
        if (req.contains("excludeCheatedIds=1")) {
          Some(rowData.copy(taskType = "过滤刷量设备", resultCount = 0))
        } else {
          None
        }
      case _@rowData => Some(rowData)
    }.filter(_.isDefined).map(_.get)
    rowDetailList
  }

  def outputDetail(list: Iterator[DDCStat.RowData], fileName: String = "ddc_stat_detail"): Unit = {
    val printer = new PrintWriter(new FileOutputStream(fileName))
    printer.println("作业ID\t邮箱\t任务序列号\t任务开始时间\t任务类型\t任务结果数\t任务状态")
    list.foreach {
      data =>
        printer.println(s"${data.jobId}\t${data.email}\t${data.serial_no}\t${data.startTime}\t${data.taskType}\t${data.resultCount}\t${data.status}")
    }
    printer.flush()
    printer.close()
  }

  def outputStatistic(list: Iterator[DDCStat.RowData], fileName: String = "ddc_stat_20161015_statistic"): Unit = {
    val printer = new PrintWriter(new FileOutputStream(fileName))
    printer.println("邮箱\t任务类型\t执行次数")
    val set = list.filter(_.status == TaskStatus.SUCCEEDED).map {
      data =>
        (data.email, data.taskType)
    }.foldLeft(new collection.mutable.HashMap[(String, String), Int]()) {
      (statSet, next) =>
        if (statSet.contains(next)) {
          statSet.put(next, statSet.get(next).get + 1)
        } else {
          statSet.put(next, 1)
        }
        statSet
    }
    set.foreach {
      case ((email, taskType), taskCount) =>
        printer.println(s"$email\t$taskType\t$taskCount")
      case _ =>
    }
    printer.flush()
    printer.close()
  }

  def outputStatisticByTaskType(list: Iterator[DDCStat.RowData], fileName: String = "ddc_stat_statistic_byTaskType"): Unit = {
    val printer = new PrintWriter(new FileOutputStream(fileName))
    printer.println("任务类型\t执行次数")
    val set = list.filter(_.status == TaskStatus.SUCCEEDED).map {
      data => data.taskType
    }.foldLeft(new collection.mutable.HashMap[String, Int]()) {
      (statSet, next) =>
        if (statSet.contains(next)) {
          statSet.put(next, statSet.get(next).get + 1)
        } else {
          statSet.put(next, 1)
        }
        statSet
    }
    set.foreach {
      case (taskType, taskCount) =>
        printer.println(s"${taskType}\t${taskCount}")
      case _ =>
    }
    printer.flush()
    printer.close()
  }

  def outputStatisticByEmail(list: Iterator[DDCStat.RowData], fileName: String = "ddc_stat_statistic_byEmail"): Unit = {
    val printer = new PrintWriter(new FileOutputStream(fileName))
    printer.println("邮箱\t执行次数")
    val set = list.map {
      data => data.email
    }.foldLeft(new collection.mutable.HashMap[String, Int]()) {
      (statSet, next) =>
        if (statSet.contains(next)) {
          statSet.put(next, statSet.get(next).get + 1)
        } else {
          statSet.put(next, 1)
        }
        statSet
    }
    set.foreach {
      case (taskType, taskCount) =>
        printer.println(s"${taskType}\t${taskCount}")
      case _ =>
    }
    printer.flush()
    printer.close()
  }


  def getTaskTypeByJson4s(json: String): String = {
    val JString(taskDesc) = parse(json) \ "taskDesc"
    taskDesc
  }

  def main(args: Array[String]): Unit = {
    val iter = stat("C:\\Users\\luogh\\Desktop\\ddc_stat_detail")
    val (iter1, iter2) = iter.duplicate
//    outputDetail(iter1)
//    outputStatistic(iter2)
    outputStatisticByTaskType(iter2)
  }
}
