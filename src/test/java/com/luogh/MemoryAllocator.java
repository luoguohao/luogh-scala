package com.luogh;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @author luogh
 * @date 2016/10/7
 */
public class MemoryAllocator {

    private static Unsafe _UNSAFE;

    static {
        try {
            Field cause1 = Unsafe.class.getDeclaredField("theUnsafe");
            cause1.setAccessible(true);
            _UNSAFE = (Unsafe)cause1.get((Object)null);
        } catch (Throwable var3) {
            _UNSAFE = null;
        }
    }


    public static void main(String[] args) {
        long addr = _UNSAFE.allocateMemory(100l);
        System.out.println("addr:"+addr);
        System.out.println("int max:"+Math.pow(2,32)*8);
    }
}
