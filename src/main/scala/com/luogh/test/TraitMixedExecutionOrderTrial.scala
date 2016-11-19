package com.luogh.test

/**
  * verify mixing multiplex traits, what the Execution Order will be for each Trait.
  * Using observer pattern design to verify.
  *
  * @author luogh
  */
object TraitMixedExecutionOrderTrial {
  var initialOrder: Int = 0 // 初始化顺序
  var invokeOrder: Int = 0  // 方法执行顺序
  def main(args: Array[String]): Unit = {
//    val button = new Button("button_1") with ObservableClick
    val button1 = new Button("button_2") with ObservableClick with VetoableClicks {
      initialOrder += 1
      println(s"Button Runtime initialOrder:$initialOrder")
      override val maxAllowed = 3
      println(s"Button Runtime initialOrder:$initialOrder End")
    }

    val button2 = new Button("button_2") with VetoableClicks with ObservableClick {
      initialOrder += 1
      println(s"Button Runtime initialOrder:$initialOrder")
      println(s"Button Runtime initialOrder:$initialOrder End")
    }

//    val buttonObserver = new ClickCountButtonObserver
    val buttonObserver1 = new ClickCountButtonObserver
    val buttonObserver2 = new ClickCountButtonObserver

//    button.addListeners(buttonObserver)
    button1.addListeners(buttonObserver1)
    button2.addListeners(buttonObserver2)

//    (1 to 5).foreach(_ => button.click())
    (1 to 5).foreach(_ => button1.click())
    (1 to 5).foreach(_ => button2.click())


//    assert(buttonObserver.count == 5, s"button:${buttonObserver.count} != 5")

    /**
      * button1，button2混入了ObeserverClick及VetoableClicks特质，只是他们的顺序不一样，并且这两个特质都同时继承自特质Clickable,都重写了click方法,
      * 那么，在button1,button2对象调用click()方法的时候，各个特质的调用顺序是scala编译器通过血缘关系算法决定的，具体算法思想可以参考Programing in scala.
      *
      * 首先将button1根据对象声明顺序依次将Button,ObserableClicks,VetoableClick压入栈中，同时因为Button继承自Widget,Clickable,ObseveralbeClick,VetoableClick
      * 都继承自Clickable,所以Clickable放在最后执行，
      * 即button1对象最后的调用顺序是:(从右往左)
      * Button <- Widget <- Clickable <- ObeserverClick <- VetoableClick
      * 而button2对象最后的调用顺序是:(从右往左)
      * Button <- Widget <- Clickable <- VetoableClick <- ObeserableClick
      *
      * 所以,对于button1,首先调用VetoableClick特质中的click()方法，该方法中会首先判断点击是否超过阀值，如果没有超过，
      * 继续调用super.click()方法，此处的super则表示ObserableClick特质中的click()方法，ObserableClick特质中的click()方法
      * 接着继续调用super.clcik()方法，此时super.click()方法表示Clickable特质中的click()方法，而Clickable特质中的click()方法
      * 将调用updateUI()方法，而updateUI()方法被Button类重写，因此最后调用Button类中的updateUI()方法，最后，整个递归调用结束，
      * 返回到OberserableClick特质中的click()方法，继续调用notifyStateChange()方法，调用结束后，回到VetoableClick中的click()方法，
      * 完成button1对象的一次click()方法调用。
      *
      * 而对于button2,首先调用OberserableClick特质中的click()方法，该方法首先调用super.click()方法，即VetoableClicks特质中的click()方法被调用，
      * vetoableClick特质中的click()方法判断调用次数是否超过阀值，如果没有继续调用super.click()方法，即Clickable特质中的click()方法，最后调用Button
      * 类重写的updateUI()方法完成，VetoableClick特质中的click()方法调用，然后回到OberveableClick特质中的click()方法调用，继续执行notifyStateChange()方法
      *
      * button1和button2的调用顺序的不一致，导致结果也不一样。
      * 1) button1每次调用click方法先去判断调用次数是否超过阀值，如果没有，才会去调用OberseableClick，去notifyStateChange
      * 2) button2每次调用click()方法则不管是否调用超过VetoableClick设定的调用次数阀值，他都会去notifyStateChange.
      * 这就导致了下面的结果：
      */
    assert(buttonObserver1.count == 3, s"button1:${buttonObserver1.count} != 3") // success
    assert(buttonObserver2.count == 2, s"button2:${buttonObserver2.count}  != 2") // failed
  }

  trait Clickable {
    initialOrder += 1
    println(s"Clickable initialOrder:$initialOrder")

    def click(): Unit = {
      invokeOrder += 1
      val invoke = invokeOrder
      println(s"Clickable invokeOrder:$invoke")
      updateUI()
      println(s"Clickable invokeOrder:$invoke End")
    }

    protected def updateUI(): Unit
    println(s"Clickable initialOrder:$initialOrder End")
  }

  trait Subject[State] {

    initialOrder += 1
    println(s"Subject initialOrder:$initialOrder")

    var observers: List[Observer[State]] = Nil

    def addListeners(observer: Observer[State]): Unit = observers ::= observer

    def notifyChange(state: State): Unit = observers.foreach(_.stateChanged(state))

    println(s"Subject initialOrder:$initialOrder End")
  }

  trait ObservableClick extends Clickable with Subject[Clickable] {
    initialOrder += 1
    println(s"ObservableClick initialOrder:$initialOrder")

    abstract override def click(): Unit = {
      invokeOrder += 1
      val invoke = invokeOrder
      println(s"ObservableClick invokeOrder:$invoke")
      super.click()
      notifyChange(this)
      println(s"ObservableClick invokeOrder:$invoke End")
    }

    println(s"ObservableClick initialOrder:$initialOrder End")
  }

  trait VetoableClicks extends Clickable {

    initialOrder += 1
    println(s"VetoableClicks initialOrder:$initialOrder")

    // default number of allowed clicks
    val maxAllowed = 2
    private var count = 0

    abstract override def click(): Unit = {
      invokeOrder += 1
      val invoke = invokeOrder
      println(s"VetoableClicks invokeOrder:$invoke")
      if (count < maxAllowed) {
        super.click()
        count += 1
      }
      println(s"VetoableClicks invokeOrder:$invoke End")
    }

    println(s"VetoableClicks initialOrder:$initialOrder End")
  }

  trait Observer[-State] {
    def stateChanged(state: State): Unit
  }

  abstract class Widget {
    initialOrder += 1
    println(s"Widget initialOrder:$initialOrder")
    val x = 1
    println(s"Widget initialOrder:$initialOrder End")
  }

  class Button(val name: String) extends Widget with Clickable {
    initialOrder += 1
    println(s"Button initialOrder:$initialOrder")

    protected def updateUI(): Unit = println("update UI")
    println(s"Button initialOrder:$initialOrder End")
  }


  class ObservableButton(override val name: String) extends Button(name) with Subject[Button] {
    override def click(): Unit = {
      super.click()
      notifyChange(this)
    }
  }

  class ClickCountButtonObserver extends Observer[Clickable] {
    var count: Int = 0

    override def stateChanged(state: Clickable): Unit = {
      count += 1
//      println(s"button click,${count}")
    }
  }

}





