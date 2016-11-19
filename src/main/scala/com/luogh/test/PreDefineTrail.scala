package com.luogh.test

/**
  * 提前定义(define advanced)、lazy字段声明
  *
  * 同时需要注意，重写lazy字段在多态继承情况下的变化。(lazy字段实际是对应一个方法，该方法在第一次调用时去初始化，后续调用都是使用初始化后的值)
  * 所以，在多态继承情况下，因为子类中重写lazy字段，即重写lazy对应的方法，所以，在真正获取lazy值的时候是去取子类对应的初始化lazy
  * 值的方法。
  *
  * lazy val t:T = expr 无论expr是什么东西，字面量也好，方法调用也好。
  * Scala的编译器都会把这个expr包在一个方法中，并且生成一个flag来决定
  * 只在t第一次被访问时才调用该方法。
  *
  *
  *     private final String property$lzycompute$1(ObjectRef property$lzy$1, VolatileByteRef bitmap$0$1)
  *     {
  *         synchronized (this) //加锁
  *         {
  *             if ((byte)(bitmap$0$1.elem & 0x1) == 0) //如果属性不为null
  *             { //那么进行初始化
  *                 property$lzy$1.elem = init();bitmap$0$1.elem = ((byte)(bitmap$0$1.elem | 0x1));
  *             }
  *             return (String)property$lzy$1.elem;
  *         }
  *     }

  * @author luogh
  */
object PreDefineTrail {
  def main(args: Array[String]) {
    val ant = new Ant

    /**
      * 结果为0，因为在Creature特质中初始化env变量的时候，通过getter()方法获取range的值，但是因为range被Ant子类重写了，
      * 而此时Ant子类并没有初始化，所以getter()方法返回0，因此env初始化的时候，env 的length字段等于0,这样即使在range的
      * getter方法重写，env变量已经实例化完成，不可变了。
      *
      * 解决方式：
      * 1) 子类使用提前定义，让父类初始化的时候，能提前获取到子类重写的getter方法。
      * 2) 对父类中的变量声明为lazy,只有在该变量真正需要被使用的时候再去初始化。
      */
    println(ant.env.length) // 结果为0

    val ant_DefineAdvanced = new Ant_DefineAdvanced // 使用提前定义
    println(ant_DefineAdvanced.env.length) // 结果为2

    val ant_WithLazy = new AntWithLazy //使用lazy
    println(ant_WithLazy.env.length)
  }
}

class Creature {
  val range = 10
  val env = new Array[Int](range)
  println(s"env length:${env.length}")
}

class CreatureWihLazy {
  val range = 10
  lazy val env = new Array[Int](range)
  println(s"CreatureWihLazy env length:${env.length}") // 打印0, 虽然env定义为lazy,但是在env.length方法调用时候，需要去获取range的值，
  // 而，scala val的字段的重写，实际是在子类中存在一个final成员属性range,以及一个重写父类中range的getter方法range(),
  // 并且在子类构造器中去初始化range属性，因为子类构造器并没有执行，所以，此时，range的值依然没有被子类初始化。
  // 所以子类中range的getter方法range()返回0.所以，env.length = 0
  // 使用提前定义可以解决,实际是在子类调用父类构造器之前，先初始化range值，这样，在父类需要应用range的值时，可以获取到子类的值。
}

class CreatureWihLazyRangeLazy {
  lazy val range = 10
  val env = new Array[Int](range)
  println(s"lazy env length1:${env.length}") // 打印2, 因为range 字段为lazy,并且在子类中重写了lazy的值，所以,在获取值的时候
                                            // 获取的是子类lazy对应初始化方法
}


class Ant extends Creature {
  override val range = 2
}

class AntWithLazy extends CreatureWihLazy {
  override val range = 2
}

class AntWithRangeLazy extends CreatureWihLazyRangeLazy {
  override lazy val range = 2
}

class Ant_DefineAdvanced extends {
  // 提前定义,实际是在子类调用父类构造器之前，先初始化range值，这样，在父类需要应用range的值时，可以获取到子类的值。
  override val range = 2
} with Creature


trait T1 {
  var z: Int
  // 可变抽象字段，子类实现， var z
  val m: Int
  val a: Int = 10
  // 不可变抽象字段，子类实现， val m
  def k(): Int // 抽象方法，子类实现，  def k(): Int 或者更严格的，k()方法只返回固定值，使用  val k:Int
}

class ClazA extends T1 {
  var z: Int = 10
  override val a: Int = 11
  val m: Int = 3
  //  def k():Int = 19
  val k: Int = 20
}