package com.luogh.test.scalaimpatient

/**
 * 自定义getter、setter方法，来实现内部逻辑的改变，不需要客户端的访问代码
 */
class Person (val name:String) {
  def id:Int = 1
  val age:Int = 1
  var sex1:String ="" //将之前的sex变量改名为sex1
  def sex = sex1  //自定义sex的getter方法，实际上是对sex1进行操作
  def sex_= (newValue:String){ this.sex1 = newValue}  //自定义sex的setter方法
}

class Employee(name:String) extends Person(name) {
  override def id: Int = name.hashCode()
  //val id:Int = name.hashCode()
  override val age: Int = ???
  //override var sex1: String = ???
  
}

object mainClass1 extends App {
  new Person("name").sex = 21.toString  //客户端依然对sex执行赋值操作，但是此时执行的是自定义的sex_=方法，而不是编译器自动生成的setter方法
}