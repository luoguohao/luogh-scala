package com.luogh.test.scalaTypeSystem

/**
  * In general, covariant type parameter can be used as immutable field type, method return type and also as method
  * argument type if the method argument type has a lower bound. Because of those restrictions, covariance
  * is most commonly used in producers (types that return something) and immutable types.
  *
  * @author luogh
  */
object CovariantParameterTypeTrial {
  class VendingMachine[+A](val currentItem: Option[A], items: List[A]) {
    def this(items: List[A]) = this(None, items)

    def dispenseNext(): VendingMachine[A] =
      items match {
        case Nil => {
          if (currentItem.isDefined)
            new VendingMachine(None, Nil)
          else
            this
        }
        case t :: ts => {
          new VendingMachine(Some(t), ts)
        }
      }

//    def addAll1(newItems: List[A]): VendingMachine[A] = new VendingMachine[A](items ++ newItems)
    def addAll[B >: A](newItems: List[B]): VendingMachine[B] =
      new VendingMachine[B](items ++ newItems)

    def copy[B >: A](newItem: VendingMachine[B]) = new VendingMachine[B](newItem.currentItem, List(newItem.currentItem.get))
  }

  class Drink
  class SoftDrink extends Drink
  class Cola extends SoftDrink
  class ColaSub extends Cola
  class TonicWater extends SoftDrink

  val colasVM: VendingMachine[Cola] = new VendingMachine[Cola](List(new Cola, new Cola))
  val tonicWaterVM: VendingMachine[TonicWater] = new VendingMachine[TonicWater](List(new TonicWater, new TonicWater))
  val softDrinksVM: VendingMachine[SoftDrink] = colasVM.addAll(List(new SoftDrink))
  val softDrinksVM2: VendingMachine[SoftDrink] = colasVM.addAll(List(new ColaSub))
  val softDrinksVM1: VendingMachine[SoftDrink] = colasVM.addAll(List[TonicWater](new TonicWater))
  val copy: VendingMachine[SoftDrink] = colasVM.copy(tonicWaterVM)


  /**
    * Covariant (and contravariant) type parameter as mutable field type
    *
    * Type parameter with variance annotation (covariant + or contravariant -)
    * can be used as mutable field type only if the field has object private
    * scope (private[this]). This is explained in Programming In Scala [Odersky2008].
    *
    *
    * Object private members can be accessed only from within the object in which
    *  they are defined. It turns out that accesses to variables from the same object
    *  in which they are defined do not cause problems with variance. The intuitive
    *  explanation is that, in order to construct a case where variance would lead
    *  to type errors, you need to have a reference to a containing object that has
    *  a statically weaker type than the type the object was defined with.
    *  For accesses to object private values, however, this is impossible.
    */


  trait Bullet
  class NormalBullet extends Bullet
  class ExplosiveBullet extends Bullet

  final class AmmoMagazine[+A <: Bullet](private[this] var bullets: List[A]) {

    def hasBullets: Boolean = !bullets.isEmpty

    def giveNextBullet(): Option[A] =
      bullets match {
        case Nil => None
        case t :: ts => {
          bullets = ts
          Some(t)
        }
      }
  }

  final class Gun(private var ammoMag: AmmoMagazine[Bullet]) {
    def reload(ammoMagazine: AmmoMagazine[Bullet]): Unit = {
      this.ammoMag = ammoMagazine
    }

    def hasAmmo: Boolean = ammoMag.hasBullets

    /** Returns Bullet that was shoot or None if there is ammo left */
    def shoot(): Option[Bullet] = ammoMag.giveNextBullet()
  }

  val gun = new Gun(new AmmoMagazine[NormalBullet](List(new NormalBullet)))
  gun.reload(new AmmoMagazine[ExplosiveBullet](List(new ExplosiveBullet)))


}

