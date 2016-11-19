package com.luogh;

/**
 * @author luogh
 */
public class InitialOrder {


    public static void main(String[] args) {
        System.out.println(new Bar().bar());
    }
}

class Foo {
    private final int foo;
    private final int bar;

    public int foo() {
        return foo;
    }

    public int bar() {
        return bar;
    }

    public Foo() {
        foo = 10;
        bar = foo();
    }
}

class Bar extends Foo {
    private final int foo;

    public int foo() {
        return foo;
    }

    public Bar() {
        super();
        foo = 20;
    }
}
