
/**
 * 包定义、类继承、方法、字段重写、匿名子类（结构类型）
 */
package scalaimpatient {
  package impatient {
    class Person(val name:String){
        override def toString = getClass.getName + "[name="+name+"]"
        def +(age:Int)=age+1
    }
  
    class SercretAgent(codename:String) extends Person(codename) {
       override val name = "secret1"
       //override val toString = "Secret"
       override def toString = "Secret"
       override def +(age:Int)=age-1
       def friend(p:Person{ def +(age:Int):Int}) = p + 100  //p:Person{ def +(age:Int):Int} 表示结构类型：带方法名为+,参数为(Int) 返回值为：Int的Person对象
    }
  }

  package test {
    object testClaz {
      import impatient.Person
      val name = new Person("test").name
      println(name)
    }
  }
  
  object mainClass extends App{
    import impatient.SercretAgent
    val person = new SercretAgent("luo")
    println(person + 12) // result  is 11
    println(person) // result is Secret
    
    //匿名子类
    import impatient.Person
    val agent = new Person("anonymous") {
      override def +(age:Int)={println(age-2);age-2}
    }
    println(agent + 10) //result is 8
    
    person.friend(agent)
    person.friend(new Person("anonymous") {
       override def  +(age:Int)={println(age);age-2}
    })
    import test.testClaz  
    testClaz
  }
   
  }
