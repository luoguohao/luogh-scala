package com.luogh.muti_thread;

import com.clearspring.analytics.util.Lists;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.System.out;

/**
 * @author luogh
 *
 * 栅栏，当所有的线程达到相同的位置，那么将触发一个操作。
 * 该操作中可以修改一些状态，比如判断任务线程是否需要继续
 * 执行，如果不需要，可以触发线程Terminate事件。如果，需要，
 * 那么继续让工作线程执行，后续可能又会在相同的栅栏上触发。
 * 所以，称为cyclic Barrier
 */
public class CyclicBarrierTest {

    private final CyclicBarrier barrier;

    public CyclicBarrierTest(int barrierCnt) {
        this.barrier = new CyclicBarrier(barrierCnt, new MergeThread());
    }

    public static void main(String[] args) throws Exception {
        ExecutorService service = Executors.newCachedThreadPool();
        CyclicBarrierTest app = new CyclicBarrierTest(4);

        List<Future<?>> runningTask = Lists.newArrayList();
        IntStream.range(0, 4).forEach(index -> {
                Future<?> result = service.submit(app.new WorkerThread((int)(Math.random() * 20) + 10));
                    runningTask.add(result);
                }

        );

        final int totalThread = 4;
        int remaingThread = totalThread;
        while (runningTask.size() > 0) {
            int old = remaingThread;
            runningTask.removeAll(runningTask.stream().filter(task -> task.isDone()).collect(Collectors.toList()));
            remaingThread = runningTask.size();
            if (remaingThread < old) {
                runningTask.forEach(future -> future.cancel(true));
            }
            out.println("runningTask size=" + runningTask.size());
            Thread.sleep(100);
        }

        out.println("All job done.");
        service.awaitTermination(5, TimeUnit.MINUTES);
    }

    class WorkerThread implements Runnable {
        private volatile  boolean isDone;
        private final int loopSize;
        WorkerThread(int loopSize) {
            this.loopSize = loopSize;
        }

        @Override
        public void run() {
            int initLoop = loopSize;
            while(initLoop-- > 0 && !isDone) {
                try {
                    out.println("...");
                    Thread.sleep((long)(Math.random() * 500) + 100L);
                } catch (InterruptedException e) {
                    out.println("worker thread InterruptedException = " + e);
                    isDone = true;
                }
                if (initLoop % 2 == 0) { // 如果存在工作线程已经执行完，那么其他等待barrier的线程将会一直等待下去。
                    out.println("Thread " + Thread.currentThread().getName() + "Complete. and stay at barrier, and current await thread numbers=" + barrier.getNumberWaiting());
                    try {
                        barrier.await();
                    } catch (InterruptedException e) {
                        out.println("barrier InterruptedException = " + e);
                        isDone = true;
                    } catch (BrokenBarrierException e) {
                        out.println("BrokenBarrierException = " + e);
                        isDone = true;
                    }
                }
            }
            isDone = true;
        }
    }

    class MergeThread implements Runnable {

        @Override
        public void run() {
            out.println("MergeThread begin to execute ...");
        }
    }

}
