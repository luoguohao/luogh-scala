package com.luogh.test.scalaTypeSystem

/**
  * @author luogh
  *
  * http://stackoverflow.com/questions/19428376/scala-covariance-and-lower-type-bounds-explanation
  */
object TypeVariantTrial {

//    Consider the followng hierarchy:

  class Foo
  class Bar extends Foo { def bar = () }
  class Baz extends Bar { def baz = () }

  class Cov[+T](val item: T, val existing: List[T] = Nil) {

    def append[S >: T](value: S) = new Cov[S](value, item :: existing)

  }
//  Then we can construct three instances for each of the Foo sub-types:

  val cFoo = new Cov(new Foo)
  val cBar = new Cov(new Bar)
  val cBaz = new Cov(new Baz)
//  And a test function that requires bar elements:

  def test(c: Cov[Bar]) = c.item.bar
//  It holds:
//  test(cFoo) // failed,not possible (otherwise `bar` would produce a problem)

  test(cBaz) // ok, since T covariant, Baz <: Bar --> Cov[Baz] <: Cov[Bar]; Baz has bar

//  Now the append method, falling back to lower bound:
  val cFoo2 = cBar.append(new Foo)

//  This is ok, because Foo >: Bar, List[Foo] >: List[Bar], Cov[Foo] >: Cov[Bar].
//  Now, correctly your bar access has gone:

//  cFoo2.item.bar // failed, bar is not a member of Foo

//  To understand why you need the lower-bound, imagine the following was possible

 /* class Cov1[+T](val item: T, val existing: List[T] = Nil) {

    def append(value: T) = new Cov[T](value, item :: existing)

  }

  class BarCov extends Cov1[Bar](new Bar) {
    override def append(value: Bar) = {
      value.bar // !
      super.append(value)
    }
  }

  //  Then you could write
  def test2[T](cov: Cov1[T], elem: T): Cov[T] = cov.append(elem)
//  And the following illegal behaviour would be allowed:

  test2[Foo](new BarCov, new Foo) // BarCov <: Cov[Foo]*/
//  where value.bar would be called on a Foo. Using (correctly) the lower bound, you wouldn't be able to implement append as in the hypothetical last example:

  class BarCov1 extends Cov[Bar](new Bar) {
    override def append[S >: Bar](value: S) = {
//      value.bar //failed, error: value bar is not a member of type parameter S
      super.append(value)
    }
  }
//  So the type system remains sound.

}
