package com.luogh.test

/**
  * val 字段在多态继承关系中的问题，通过javap 查看字节码可以明白。
  * FooVAL，Bar代码等价于 java代码：

      public class FooVAL {
        private final int foo;
        private final int bar;
        public int foo() {
            return foo ;
        }
        public int bar() {
            return bar ;
        }
        public Foo() {
            foo = 10;
            bar = foo(); // bar字段实际是调用foo()方法，因为foo()方法，被子类重写，所以会调用子类重写的方法foo()方法
                         // 因为子类foo()方法返回子类成员foo字段，但是因为子类Bar并没有初始化foo字段，所以,foo()方法
                         // 返回 0 ，因此，父类中的bar字段的值就变成了0.
        }
    }
    public class Bar extends FooVAL {
        private final int foo; // 拷贝了父类foo字段，因为foo定义为val.
        public int foo() {
            return foo ;
        }
        public Bar() {
            super();
            foo = 20;  // 子类构造器中，会先调用父类的构造器，再初始化子类的成员属性 foo = 20
        }

        public static void main(String[] args) {
            System.out.println(new Bar().bar());
        }
    }
  * @author luogh
  */
object ValFieldProblemTrail {
  def main(args: Array[String]): Unit = {
    println(new Bar(). bar)  //打印结果既不是10也不是20，而是0,解释见https://www.zybuluo.com/MiloXia/note/83745
  }
}

class FooVAL {
  val foo = 10
  val bar = foo
}

class Bar extends FooVAL {
  override val foo = 20
}

