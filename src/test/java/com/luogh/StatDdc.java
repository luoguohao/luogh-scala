package com.luogh;

import static java.lang.System.out;

/**
 * @author luogh
 * @date 2016/10/20
 */
public class StatDdc {

    public static void main(String[]  args) throws Exception {
        test();
    }

    public static int test() {
        try {
            out.println("in ...");
            return 100;
        } finally {
            out.println("finally");
        }
    }
}
