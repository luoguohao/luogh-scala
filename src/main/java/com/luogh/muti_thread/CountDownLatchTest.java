package com.luogh.muti_thread;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static java.lang.System.out;

/**
 * @author luogh
 */
public class CountDownLatchTest {

    private final CountDownLatch latch;

    public CountDownLatchTest (int latchSize) {
        latch = new CountDownLatch(latchSize);
    }

    public static void main(String[] args) throws Exception {
        ExecutorService service = Executors.newCachedThreadPool();
        CountDownLatchTest app = new CountDownLatchTest(4);
        IntStream.range(0, 4).forEach(index ->
                service.execute(app.new WorkerThread((int)(Math.random() * 500) + 100))
        );
        service.execute(app.new MergeThread());

        service.awaitTermination(5, TimeUnit.MINUTES);
    }

    class WorkerThread implements Runnable {

        private final int loopSize;
        WorkerThread(int loopSize) {
            this.loopSize = loopSize;
        }

        @Override
        public void run() {
            int initLoop = loopSize;
            while(initLoop-- > 0) {
                try {
                    out.println("...");
                    Thread.sleep((long)(Math.random() * 500) + 100L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            latch.countDown();
            out.println("Thread " + Thread.currentThread().getName() + "Complete. and current latch remaining = " + latch.getCount());
        }
    }

    class MergeThread implements Runnable {

        @Override
        public void run() {
            out.print("wait latch release....");
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            out.println("MergeThread begin to execute ...");
        }
    }
}
