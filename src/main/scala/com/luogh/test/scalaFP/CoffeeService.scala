package com.luogh.test.scalaFP

/**
  * @author luogh
  */
object CoffeeService {
  def bugCoffee(cc: CreditCard): (Coffee, Charge) = {
    val coffee = new Coffee
    (coffee,Charge(cc,coffee.price))
  }

  def bugCoffees(cc: CreditCard, num: Int): (List[Coffee], Charge) = {
    val purchases: List[(Coffee, Charge)] = List.fill(5)(bugCoffee(cc))
    val (coffees, charges) = purchases.unzip
    (coffees, charges.reduce(_.combine(_)))
  }

  /**
    * coalesce Charge with same CreditCard
    * @param charges
    * @return
    */
  def coalesce(charges: List[Charge]): List[Charge] = {
    charges.groupBy(_.cc).values.map(_.reduce(_.combine(_))).toList
  }

  def main(args: Array[String]): Unit = {
    CoffeeService.bugCoffees(CreditCard(11),10)
  }
}


class Coffee {
  val price = 10
}

case class CreditCard(id: Int)

case class Charge(cc: CreditCard, amount: Float) {
  def combine(other: Charge): Charge = {
    if (this.cc == other.cc) Charge(cc,this.amount + other.amount)
    else throw new RuntimeException("can`t combine different creditCard Charge.")
  }
}

