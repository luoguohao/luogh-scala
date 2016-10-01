package com.luogh.scala.test.scalaimpatient

import scala.io.Source

/**
 * @author Kaola
 * 正则表达式
 */

@SerialVersionUID(42L) class RegexT extends Serializable {
  def regexApiTest = {
    val numPattern = "[0-9]+".r
    //如果正则表达式包含反斜杠或者是引号，使用"原始"字符串语法 """....."""
    val wsnumwsPattern = """\S+[0-9]+\S+""".r  //使用转义 "\\s+[0-9]+\\s+"
    
    //查找所有匹配的项
    for(mathchString <- numPattern.findAllIn("99 bottles,98 bottles")) println(mathchString)
    //查找字符串中的首个匹配项
    println(numPattern.findFirstIn("99 bottles,98 bottles"))  //返回的是Some(99) or None
    //查询字符串是否开始部分能匹配
    println(numPattern.findPrefixMatchOf("bttles,toas")) //返回none
    println(wsnumwsPattern.findPrefixMatchOf("bttles12toas1212")) //返回Some(bttles12,toas1212)
    //替换某个匹配项或全部匹配项
    println(numPattern.replaceFirstIn("99 bottles,98 bottles", "100")) //100 bottles,98 bottles
    println(numPattern.replaceAllIn("99 bottles,98 bottles", "100")) //100 bottles,100 bottles
    
    //正则表达式组
    val numitemPattern = "([0-9]+) ([a-z]+)".r
    //val numitemPattern(num,item) = "99 bottles 23432 asdfxc 23dasdfa"
    for(numitemPattern(num,item) <- numitemPattern.findAllIn("99 bottles 23432 asdfxc 23dasdfa")) println(num+":"+item)
    //
    for(numitemPattern(num,item) <- numitemPattern.findAllMatchIn("99 bottles 23432 asdfxc 23dasdfa")) println(num+":"+item)
  }
  
  /**
   * 编写正则表达式，匹配java或c++程序中的类似"like this,maybe with \" or \\"" 这样的带引号的字符串
   * 
   */
  def example01 = {
      val pattern = """\S+\s+"+\S*""".r
      val matcherResult = pattern.findAllIn("like this,mybe with \" or \\ or \\\"")
      matcherResult.foreach { println }
  }
  
  /**
   * 编写正则表达式，打印出某个网页中所有img标签的src属性，使用正则表达式和分组
   * 
   */
  def example02 = {
    val pattern = "<img[^>]+src=\"([^\"]+)\"[^>]+>".r
    val source = Source.fromURL("http://www.baidu.com","UTF-8")
    for (pattern(result) <- pattern.findAllIn(source.mkString)) println(result)
    source.close
  }
}

object mainClass05 extends App {
  // println("like this,mybe with \" or \\ or \\\"")  // like this,mybe with " or \ or \"
  new RegexT().example01
  new RegexT().example02
}