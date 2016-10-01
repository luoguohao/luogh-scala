package com.luogh.scala.test.scalaimpatient

/**
 * @author Kaola
 * 模式匹配 与 样例类 练习
 */
class patternMatcherAndCaseClassExcecise {
  /**
   * 1.利用模式匹配，编写一个swap函数，接受一个整型的对偶，返回对偶的
   * 两个组成部件互换位置的新对偶
   */
  def swap(tuple:(Int,Int))=
    tuple match {
      case (a,b) => (b,a)
    }
  
  /**
   * 2.利用模式匹配，编写一个swap函数，交换数组中的前两个元素的位置。前提条件是
   * 数组长度至少为2
   */
  def swapArray[T](arr:Array[T])=
    arr match {
    case Array(_) if arr.length<2 => "length is less 2"
    case Array(a,b, rest @ _*) => (b +: a +: rest).mkString(",")
  }
  
  /**
   * 3.可以用列表制作只在叶子节点存放值的树。列表((3 8) 2 (5 (4 7)))描述的是如下的一颗树：
   *              .
   *           /  |  \
   *          .   2   .
   *         / \      |
   *        3   8     .
   *                 / \
   *                5   .
   *                   / \
   *                  4   7              
   * 不过有些列表元素是数字，而另一些是列表。在scala中，不能拥有异构的列表，必须使用List[Any]
   * .编写一个leafSum函数，计算所有叶子节点中的元素之和，用模式匹配来区分数字和列表
   */
  
  def leafSum(list : List[Any]):Int =
   list.map{
    _ match {
      case x:List[Any] => leafSum(x)
      case v:Int => v
    }
  }.sum
  
  
  /*
   * 4.升级版leafSum
   */
  def leafSumV2(node : BinaryTree):Int = {
    node match {
      case Leaf(v) => v
      case Node(l,r) => leafSumV2(l) + leafSumV2(r)
    }
  }
   
  /**
   * 5.扩展前一个树，使得每个节点可以有任意多的后代
   */
  
  def leafSumV3(node : Tree):Int = {
    node match {
      case TreeLeaf(v) => v
      case TreeNode(trees @ _*) => trees.map{ leafSumV3 _}.sum
    }
  }
  
  /**
   * 6.扩展前一个树，使得每个非叶子节点除了后代之外，能够存放一个操作符。然后通过编写一个eval函数来计算他的值，
   *                  +
   *                / | \
   *               *  2  /
   *              / \    |
   *             3   8   10
   *             
   * 这棵树的值等于 (3*8) + 2 +(1/10) = 26.1 //从左往右计算
   * 表示法：
   * OpTreeNode(Add,OpTreeNode(Multi,OpTreeLeaf(3),OpTreeLeaf(8)),OpTreeLeaf(2),OpTreeNode(Divide,OpTreeLeaf(10)))
   */
  
  def eval(opTree :OpTree):Float = {
    opTree match {
      case OpTreeLeaf(v) => v
      case OpTreeNode(op, trees @ _*) =>  op match {
        case Add => trees.map(eval _).foldLeft(0.0f)( _ + _)
        case Sub => trees.map(eval _).foldLeft(0.0f)( _ - _)
        case Multi => trees.map(eval _).foldLeft(1.0f)( _ * _)
        case Divide => {
          val values = trees.map(eval _)
          if (values.length>1) {
            values.reduceLeft(_ / _)
          } else {
            1/values(0)
          } 
        } 
      }
    }
  }
  
  /**
   * 编写一个函数，计算List[Option[Int]]中所有非None值之和，不得使用match语句
   */
  def sumNotNone(list : List[Option[Int]]):Int = {
    /*var sum = 0;
    for(op <- list) {
      sum += op.getOrElse(0)
    }
    sum*/
    list.foldLeft(0)(_ + _.getOrElse(0))
  }
  
  /**
   * 编写一个函数，将两个类型为Double=>Option[Double]的函数组合起来，产生另一个同样类型的函数。
   * 如果其中一个返回None,则组合函数返回None.例如：
   * def f(x:Double) = if(x>0) Some(sqrt(x)) else None
   * def g(x:Double) = if(x!=1) Some(sqrt(1/(x-1))) else None
   * val h = compose(f,g)
   * h(2)将得到Some(1),而h(1)和h(0)将得到None
   */
  
  def compose[T](f:(T)=>Option[T],g:(T)=>Option[T])(arg:T):Option[T] = {
    (f(arg),g(arg)) match {
      case (fS:Some[T],gS:Some[T]) =>gS
      case _ => None
    }
  }
  
  def compose01[T](f:(T)=>Option[T],g:(T)=>Option[T]) = {
    (v:T) =>f(v) match {
      case Some(_) => g(v)
      case None => None
    }
  }
}

/**
   * 制作这样的树的最好的做法是使用样例类，不妨从二叉树开始
   *                  .
   *                 / \
   *                5   .
   *                   / \
   *                  4   7  
   * 此时可以表示为: Node(Leaf(5),Node(Leaf(4),Leaf(7)))   
   */
sealed abstract class BinaryTree
case class Leaf(value:Int) extends BinaryTree
case class Node(left:BinaryTree,right:BinaryTree) extends BinaryTree

/**
   * 制作这样的树的最好的做法是使用样例类，不妨从二叉树开始
   *                  .
   *                / | \
   *               5  2  .
   *                   / | \
   *                  4  7  10
   * 此时可以表示为: TreeNode(TreeLeaf(5),TreeLeaf(2),TreeNode(TreeLeaf(4),TreeLeaf(7),TreeLeaf(10)))   
   */
sealed abstract class Tree
case class TreeLeaf(value:Int) extends Tree
case class TreeNode(trees:Tree *) extends Tree


/**
 * 带运算符的树
 */

sealed abstract class OpTree
case class OpTreeLeaf(value:Float) extends OpTree
case class OpTreeNode(operator:Operator,trees:OpTree *) extends OpTree

/**
 * 运算符样例类
 */
sealed abstract class Operator
case object Add extends Operator  //加
case object Sub extends Operator  //减
case object Multi extends Operator //乘
case object Divide extends Operator //除


object mainClass16 extends App {
  val obj01 = new patternMatcherAndCaseClassExcecise
  println(obj01.swap((2,3)))
  println(obj01.swapArray(Array(2,3,4)))
  println(obj01.leafSum(List(3,8) :: 2 :: List(5,List(4,7))))
  println(obj01.leafSumV2(Node(Leaf(5),Node(Leaf(4),Leaf(7)))))
  println(obj01.leafSumV3(TreeNode(TreeLeaf(5),TreeLeaf(2),TreeNode(TreeLeaf(4),TreeLeaf(7),TreeLeaf(10))) ))
  println(obj01.eval(OpTreeNode(Add,OpTreeNode(Multi,OpTreeLeaf(3),OpTreeLeaf(8)),OpTreeLeaf(2),OpTreeNode(Divide,OpTreeLeaf(10))) ))
  println(obj01.eval(OpTreeNode(Divide,OpTreeLeaf(10)) ))
  println(obj01.sumNotNone(List(Some(1),Some(2),Some(3),Some(4),None,Some(5),None)))
  println(obj01.compose(f _,g _)(2))
  println(obj01.compose(f _,g _)(1))
  println(obj01.compose(f _,g _)(0))
  
  println(obj01.compose01(f _,g _)(2))
  println(obj01.compose01(f _,g _)(1))
  println(obj01.compose01(f _,g _)(0))
  println(genFunction[String]()("test"))
  import scala.math._
  def f(x:Double)= {
    if(x>0) Some(sqrt(x)) else None
  }
  def g(x:Double) = {
    if(x!=1) Some(sqrt(1/(x-1))) else None
  }
  
  /**
   * return a function : 
   */
  def genFunction[T]()={
    (v:T) => v
  }
}

