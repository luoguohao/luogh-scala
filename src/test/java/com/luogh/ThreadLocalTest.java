package com.luogh;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import static java.lang.System.out;

/**
 * @author luogh
 * @date 2016/9/29
 *
 * ThreadLocal對象保存在Thread中的ThreadLocalMap對象中，並且ThreadLocalMap對象中每一個Entry都是WeakReference<ThreadLocal<?>>
 *  類型，每個Entry中value字段保存ThreadLocal中真實存放的數據。当用户将ThreadLocal对象的强引用去除，那么当进行GC操作时，相应的
 *  ThreadLocalMap对象中保存的Entry.get()方法将返回null,此时在有新的ThreadLocal对象添加到当前ThreadLocalMap中，会去除掉Entry[]
 *  中Entry.get()为空的数据，GC将会去除该数据，并且如果TheadLocal对象个数超过Entry[] 大小，那么需要将Entry[] 扩容，并且所有数据rehash 。
 *  (如果ThreadLocalMap中的数据过多，OOM)
 *
 *  当线程结束，会调用Thread.exit()方法，该方法中将ThreadLocal.ThreadLocalMap对象置 null,应为ThreadLocal对象实际存储的数据都是通过
 *  ThreacLocalMap来存储的，所以，线程结束，对应的ThreadLocalMap为null,最后，线程的所有数据将被GC.
 */
public class ThreadLocalTest {
    private static final ThreadLocal<String> threadData = new ThreadLocal<String>(){
        protected String initialValue(){
            return "DEFAULT";
        }
    };

    private static final ThreadLocal<String> threadData2 = new ThreadLocal<String>(){
        protected String initialValue(){
            return "DEFAULT_2";
        }
    };


    private static final ReferenceQueue<ThreadLocal<String>> queue = new ReferenceQueue<>();
    private static final WeakReference<ThreadLocal<String>> weakThreadData = new WeakReference<>(new ThreadLocal<>(),queue);

    public static void main(String[] args) throws Exception {
        Thread t1 = new NewThread("thread1");
        Thread t2 = new NewThread("thread2");

        t1.start();
//        t2.start();

        t1.join();
        out.println("t1 thread finish");
//        t2.join();
        out.println("t2 thread finish");

        out.println("after all thread joined. invoke gc, to see ThreadLocal Entry wheather has already been reclaimed.");
        threadData.set("test-after");
        System.gc();
        Thread.sleep(40000);
//        out.println("threadData:"+threadData.get());
    }

    static class NewThread extends Thread {
        NewThread(){}
        NewThread(String threadName) {
            super(threadName);
        }
        public void run() {
            threadData.set("test_before");
            ThreadLocal<String> data = weakThreadData.get();
            if(data!=null) {
                data.set("test_before_2");
            }

            try {
                data = null; // remove strong reference
                System.gc();
//                Thread.sleep(3000);
                out.println("threadData value:"+threadData.get());

                // create multi ThreadLocal Object to invoke ThreadLocalMap rehash,the default rehash threshold is 10
                for(int i=0;i<10;i++) {
                    ThreadLocal<String> str = new ThreadLocal<>();
                    str.set("test_after_"+i);
                }
                while(true) {
                    Reference<? extends ThreadLocal<String>> ref = queue.remove(100);
                    if(ref!=null) {
                        out.println("weak reference has already removed.");
                        break;
                    } else {
                        out.println("reference queue is null");
                    }
                }
                out.println(Thread.currentThread().getName()+" end");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
