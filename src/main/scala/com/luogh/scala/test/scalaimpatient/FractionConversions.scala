package com.luogh.scala.test.scalaimpatient

/**
 * @author Kaola
 */
object FractionConversions {
  implicit def long2Fraction(n:Int)= Fraction(n,1)
  implicit def double2Fraction(n:Int)= Fraction(n,1)
}