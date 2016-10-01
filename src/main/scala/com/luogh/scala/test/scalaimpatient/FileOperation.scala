package com.luogh.scala.test.scalaimpatient

import scala.io.Source
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.PrintWriter
import scala.collection.mutable.ArrayBuffer

/**
 * @author Kaola
 * 文件操作
 */
class FileOperation {
  
  def readFromFile01={
    val source = Source.fromFile("F://test.txt","UTF-8")
    val content = source.getLines.toArray
    //for(line <- content) println(line)
    println(source.getLines.length) // 0
    println(content.length) //总个数
    print(content.mkString(","))
    source.close
  }
  
  def readFromFile02={
    val source = Source.fromFile("F://test.txt","UTF-8")
    var sum = 0
    for(c <- source if c!='\n' && c!='\r') {println("char is :"+c); sum+=1}
    println("total size:"+sum)
    source.close
  }
  //读取文件，并且想查看某个字符但是不标记该字符串为已读状态,
  def readFromFile03WithPeek={
     val source = Source.fromFile("F://test.txt","UTF-8")
     val iter = source.buffered
     var flag = false
     while(iter.hasNext && !flag) {
       println("head is :"+iter.head)  //只是获取，不标记该字符串被操作
       if(iter.head=='f') {
         println("got f")
         flag = true
       } else {
          println("still not found and current is :"+iter.next())
       }
     }
     source.close
  }
  
  def readFileWithSplitWord() {
    val source = Source.fromFile("F://test.txt","UTF-8")
    val tokens = source.mkString.split("\\s+")
    val numbers = for(t <- tokens) yield t.hashCode()
    println(numbers.mkString("========"))
    val numbers2 = tokens.map(_.hashCode)
    println(numbers2.mkString(">>>>"))
    source.close
  }
  
  def readFromConsole = {
    while(true) {
      println("please enter a number:")
      val number = scala.io.StdIn.readInt()
      println("the number is :"+number)
    }
    
  }
  
  
  def readFromURL = {
    val source = Source.fromURL("http://www.baidu.com","UTF-8")
    println(source.mkString)
    source.close
  }
  
  def readByteFile = {
    val file = new File("F://test.txt")
    val in = new FileInputStream(file)
    val bytes = new Array[Byte](file.length.toInt)
    in.read(bytes)
    println(bytes.mkString)
    in.close
  }
  
  def writeToFile = {
    val source = Source.fromFile("F://test.txt","utf-8")
    val fos = new PrintWriter(new File("F://test1.txt"))
    for(line <- source.getLines) {
      fos.println(line)
    }
    source.close
    fos.close
  }
  
  def showSubDirs(dir:File):Iterator[File] = {
    val subDirs = dir.listFiles.filter(_ isDirectory)
    subDirs.iterator ++ subDirs.iterator.flatMap(showSubDirs _)
  }
  
  /**
   * 从一个文件中读取内容并把所有字符串数大于4的单词打印到控制台
   */
  def readFile() {
    val source = Source.fromFile("F://test.txt","UTF-8")
    //简写
    source.mkString.split("\\s+").filter { _.length >= 4 }.map{println _}
    //原型：println(source.mkString.split("\\s+").filter((x:String)=>{x.length>=4}).mkString("=>"))
    //原型：println(source.mkString.split("\\s+").filter(x=>{x.length>=4}).mkString("=>"))
    // filter{_.length>=4} ---> filter{(String) => Boolean} --> filter(def f(x:String)={x.length>=4})
    source.close
  }
  
  /**
   *  编写Scala程序，盘点给定目录及其子目录中总共有多少以.class为扩展名的文件
   */
  def getClassFileNum(f:File):ArrayBuffer[String]={
    val fileNum: ArrayBuffer[String] = new ArrayBuffer[String]()
    val subDir = f.listFiles().iterator
    for( filePath <- subDir) {
      if(filePath.isDirectory()) {
        fileNum ++= getClassFileNum(filePath)
      } else {
        if(filePath.getName().endsWith(".class")) {
          fileNum += filePath.getName
        }
      }
    }
    fileNum
  }
}

object MainClass4 extends App {
  //val obj1 = new FileOperation().readFromFile01
	//val obj2 = new FileOperation().readFromFile02
  //val obj3 = new FileOperation().readFromFile03WithPeek
	//val obj4 = new FileOperation().readFileWithSplitWord
  //val obj5 = new FileOperation().readFromConsole
  //val obj6 = new FileOperation().readFromURL
	//val obj7 = new FileOperation().readByteFile
  //val obj8 = new FileOperation().writeToFile  
  //val obj9 = new FileOperation().showSubDirs(new File("E://Youdao//YoudaoNote"))
  //for(dir <- obj9) println(dir)
   // val obj10 = new FileOperation().readFile() 
    val obj11 = new FileOperation().getClassFileNum(new File("F:\\hadoop_git\\scala\\scalaimpatient"))
    obj11.foreach(println _)
}