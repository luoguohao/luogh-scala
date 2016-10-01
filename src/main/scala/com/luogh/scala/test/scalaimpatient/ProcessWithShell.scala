package com.luogh.scala.test.scalaimpatient

import java.io.File

/**
 * @author Kaola
 * 执行shell脚本
 */


object ProcessWithShell extends App {
  import sys.process._
  
  val result = "ls -al ." #| "grep bin" #| "wc -l" !!
  
  println(result)
  
  "ls -al ." #| "grep bin" #| "wc -l" !
  
  "ls -al ." #> new File("F://test1.txt") !
  
  "grep a" #< new File("F://test1.txt") !
  
  
  //直接构造ProcessBuilder，给出起始目录、环境变量和命令
  val cmd = "whoami"
  val p = Process(cmd,new File("."),("LANG","en_US")) 
  p !
}