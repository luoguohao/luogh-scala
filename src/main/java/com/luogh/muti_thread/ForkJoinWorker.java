package com.luogh.muti_thread;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;
import java.util.stream.IntStream;

import static java.lang.System.out;

/**
 * Fork/Join 使用场景 ：如果一个应用程序能够被分解多个子任务，而且结合多个子任务的结果就能够得到
 * 最终的结果。
 *
 * ForkJoinTask: 我们使用ForkJoin框架，必须首先创建一个ForkJoin任务。他提供在任务执行fork()和join()
 * 的操作机制。通常我们不直接继承ForkJoinTask类，只是需要直接继承其子类。
 *      1.RecursiveAction: 用于没有返回结果的任务
 *      2.RecursiveTask: 用于有返回值的任务
 * ForkJoinPool: task要通过ForkJoinPool来执行，分隔的子任务也会添加到当前工作线程的双端队列中，进入
 * 队列的头部。当一个工作线程中没有任务时，会从其他工作下那成的队列尾部获取一个任务。
 *
 * ForkJoin框架使用了工作窃取的思想 (work-stealing),算法从其他队列中窃取任务来执行执行。
 * 通过此算法来降低线程等待和竞争。
 * @author luogh
 */
public class ForkJoinWorker {
    static class CountTask extends RecursiveTask<Integer> {

        public static final int threshold = 2;
        private int start;
        private int end;


        public CountTask(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        protected Integer compute() {
            int sum;
            // 如果任务足够小，就直接执行任务
            boolean canCompute = (end - start) <= threshold;
            if (canCompute) {
                sum = IntStream.rangeClosed(start, end).sum();
            } else {
                // 如果任务大于阀值，就分裂两个子任务来执行
                int mid = start + (end - start) / 2;
                CountTask leftTask = new CountTask(start, mid);
                CountTask rightTask = new CountTask(mid + 1, end);

                // 执行子任务
                leftTask.fork();
                rightTask.fork();

                // 等待任务执行结束合并结果
                int leftResult = leftTask.join();
                int rightResult = rightTask.join();
                sum = leftResult + rightResult;
            }
            return sum;
        }
    }

    public static void main(String[] args) {
        ForkJoinPool pool = new ForkJoinPool();
        CountTask task = new CountTask(1, 5);
        Future<Integer> result = pool.submit(task);

        try {
            out.println(result.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
