package com.luogh

import scala.io.Source

/**
  * @author luogh 
  */
object FormatFile {
  case class PHONE_FILE_1(phone: String, line: String)
  case class TDID_2_PHONE(tdid: String, phone: String)
  case class PHONE_MD5_FILE_3(phoneMD5: String, line: String)
  case class TDID_2_PHONE_MD5(tdid: String, phoneMD5: String)
  case class PHONE_PHONEMD5(phone:String ,phoneMD5: String)

  def main(args: Array[String]): Unit = {
    val file_1 =  Source.fromFile("C:\\Users\\luogh\\Desktop\\format\\1.txt") // 手机号对应数据
    val phones = file_1.getLines().map(line => PHONE_FILE_1(line.split("\t")(2).trim,line)).map { r => (r.phone, r.line)}.toMap

    val file_2 =  Source.fromFile("C:\\Users\\luogh\\Desktop\\format\\2.txt") // TDID对应手机号
    val phone_2_tdid = file_2.getLines().map(line => TDID_2_PHONE(line.split("\t")(1).trim, line.split("\t")(0).trim)).map {r => (r.tdid, r.phone)}.toMap

    val file_3 =  Source.fromFile("C:\\Users\\luogh\\Desktop\\format\\3.txt") // 手机号MD5对应数据
    val phoneMD5_2 = file_2.getLines().map(line => PHONE_MD5_FILE_3(line.split("\t")(1).trim, line)).map {r => (r.phoneMD5, r.line)}.toMap

    val file_4 =  Source.fromFile("C:\\Users\\luogh\\Desktop\\format\\4.txt") // TDID对应手机号号MD5
    val phoneMD5_2_tdid = file_2.getLines().map(line => TDID_2_PHONE_MD5(line.split("\t")(1).trim, line.split("\t")(0).trim)).map {r => (r.tdid, r.phoneMD5)}.toMap

    // 希望得到文件1和文件3匹配的宽表
    val (bigger,small) = if (phone_2_tdid.size >= phoneMD5_2_tdid.size) { (phone_2_tdid,phoneMD5_2_tdid) } else (phoneMD5_2_tdid,phone_2_tdid)
    bigger.foreach { entry => }

  }
}
