
package com.luogh

import java.io.{File, PrintStream}

import scala.io.Source

/**
  * @author luogh 
  */
object Encoder {
  def main(args: Array[String]): Unit = {
//    encode()
    decode()
//    test()
  }

  def decode(file: File = new File("C:\\Users\\luogh\\Desktop\\security_enc.bin")): Unit = {
    val decFile = Source.fromFile(file)
    val decStrList = decFile.getLines().map { line =>
      val arr = line.toCharArray.sliding(2, 2).foldLeft(Seq.empty[Byte]){ (bts, hex) =>
        val b = (0xff & Integer.parseInt(new String(hex), 16)).asInstanceOf[Byte]
        bts :+ b
      }
      new String(arr.toArray, "utf-8")
    }
    println(decStrList.mkString("\n"))
  }

  def encode(inputFile: File = new File("C:\\Users\\luogh\\Desktop\\security.txt"), desFile: File = new File("C:\\Users\\luogh\\Desktop\\security_enc.bin")): Unit = {
    val file = Source.fromFile(inputFile)
    val result = file.getLines().map { line =>
      line.replaceAll("\t", " ").toCharArray.foldLeft(Seq.empty[String]){ (total, ch) =>
        val hex = Integer.toHexString(ch.toInt)
        total :+ hex
      }.mkString
    }
    val writer = new PrintStream(desFile)
    result.foreach(writer.println)
  }

  def test(): Unit = {
    val list = List((1, 2 ,3), (2, 3 ,4))
    val test = list.map(x => x.productIterator.toList).reduceLeft {(x, y) => x.zip(y)}
    test.map{ x =>
      val temp = x.asInstanceOf[Tuple2[Int, Int]]
      temp._1 + temp._2
    }.foreach(println)
  }

}
