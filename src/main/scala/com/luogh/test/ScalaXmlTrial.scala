package com.luogh.test

object ScalaXmlTrial {
  val xmlFile = <symbols>
    <symbol ticker="AAPL">
      <units>200</units>
    </symbol>

    <units>300</units>

    <symbol ticker="IBM">
      <units>400</units>
    </symbol>
  </symbols>
  println(xmlFile)
  println(xmlFile.getClass.getName) //scala.xml.Elem

  /**
    * 使用单反斜杠 \ 方法提取出来的unit元素仅仅只是当前节点<symbols>下的<units>元素，
    * 如果想同时提取xmlFile中嵌套在<symbol>中的<units>元素，此时应该使用双反斜杠 \\
    * 方法来代替即可。
    */
  // 提取<units>元素  <units>300</units>
  val unitXml = xmlFile \ "units"
  println(unitXml.mkString("\n"))

  // 提取所有<units>元素 <units>200</units><units>300</units><units>400</units>
  val unitXml_all = xmlFile \\ "units"
  println("unit all :"+unitXml_all)

  println(unitXml.getClass) //scala.xml.NodeSeq$$anon$1

  // 提取<units>300</units>中的数值300，使用scala中的模式匹配,也可以使用unitXml_all(0).text方法获取
  // <units>{numOfUnits}</units> 之间不能有空格。

  unitXml_all(0) match {
    case <units>{numOfUnits}</units> =>
      println("num of units:"+numOfUnits)
  }
  println("num of units:"+unitXml_all(0).text)

  println(xmlFile \ "symbol")
  println(xmlFile \ "symbol" text)
  println("test:"+( xmlFile \ "symbolt" text)) // 不存在的，返回为空值

  //同时提取称类似于<symbol ticker="AAPL">中的ticket属性值，使用模式匹配
  // scala中<symbols>{allSymbol @ _*} </symbols> 中的@符号用于声明一个临时变量allSymbol来引用
  // <symbols></symbols>下的所有内容(即下划线加*,表示所有的<symbol></symbol>和其他节点元素)
  // 而for循环中的@<symbol>{_*}</symbol>则表示allSymbol中的symbol元素。
  // xml处理中使用@属性名称  可以将xml中的ticker属性获取出来。

  xmlFile match {
    case <symbols>{allSymbol @ _*}</symbols> =>
      for(symbolNode @ <symbol>{_*}</symbol> <- allSymbol) {
        println("symbNode:"+symbolNode)
        println("%-7s %s".format(symbolNode \ "@ticker",(symbolNode \ "units").text))
      }
  }


  def main(args: Array[String]):Unit = {

  }

}
