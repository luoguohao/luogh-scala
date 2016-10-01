package com.luogh;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.out;

/**
 * @author luogh
 * @date 2016/9/30
 */
public class InheritableThreadLocalTest {
    private static Lock lock = new ReentrantLock();
    private static Condition condition = lock.newCondition();
    private static volatile boolean init = false;
    private static ThreadLocal<String> parentData = new ThreadLocal<>();
    private static InheritableThreadLocal<String> inHeritableData = new InheritableThreadLocal<String>() {
        protected String childValue(String parentValue) {
            return "PARENT_"+parentValue;
        }
    };

    public static void main(String[] args) {
        inHeritableData.set("main_test");
        parentData.set("parent_data");
        new SimpleThread().start();
        out.println("main:"+inHeritableData.get());
        out.println("main:"+inHeritableData.get());

        lock.lock();
        inHeritableData.set("main_test_modify"); // not effect child thread local data any more.
        init = true;
        condition.signalAll();
        lock.unlock();
    }

    static class SimpleThread extends Thread {

        @Override
        public void run() {
            out.println("SimpleThread:"+inHeritableData.get()); // return parent ThreadLocal Data.
            inHeritableData.set("child_test");
            out.println("SimpleThread:"+inHeritableData.get()); // return child ThreadLocal Data.
            out.println("SimpleThread:"+parentData.get());

            try {
                lock.lock();
                while(!init) { // wait until parent thread modify inHeritableData.
                    condition.await();
                }
                out.println("SimpleThread modify:"+inHeritableData.get()); // return child ThreadLocal Data ,even though parent threadLocal data has already modified.
                lock.unlock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
