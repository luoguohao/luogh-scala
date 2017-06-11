package com.luogh.muti_thread;

import com.clearspring.analytics.util.Lists;
import org.jboss.netty.channel.socket.Worker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static java.lang.System.out;

/**
 * 多个线程同时执行相同的逻辑，获取最先完成的任务结果，其他线程则停止执行。
 * @author luogh
 */
public class FetchFirstCompleteTask<T> {

    final ExecutorService service = Executors.newCachedThreadPool();
    final int parallel;
    private volatile boolean isDone;
    private final Supplier<T> func;
    List<Future<T>> runningTask;


    public FetchFirstCompleteTask(int parallel, Supplier<T> func) {
        this.parallel = parallel;
        runningTask = new ArrayList<>(parallel);
        this.func = func;
    }

    public T getResult() {
        IntStream.range(0, parallel).forEach( index ->
                runningTask.add(service.submit(new WorkerCallable()))
        );

        Future<T> result = service.submit(() -> {
            boolean completed = false;
            T r = null;
            while (!completed) {
                for (Future<T> run : runningTask) {
                    if (run.isDone()) {
                        r = run.get();
                        completed = true;
                        runningTask.remove(run);
                        runningTask.stream().forEach(task -> task.cancel(false)); // cancel other running job
                        out.println("one task is done, cancel all other running jobs.");
                        break;
                    }
                }
            }
            return r;
        });

        try {
            return result.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return null;
    }


    class WorkerCallable implements Callable<T> {


        @Override
        public T call() throws Exception {
            T r = null;
            if (!isDone) {
                r = func.get();
                isDone = true;
            }
            return r;
        }
    }

    public void shutdown() {
        this.service.shutdownNow();
    }

    public static void main(String[] args) {
        FetchFirstCompleteTask<Integer> task = new FetchFirstCompleteTask(5, () -> {
            int i = (int)(Math.random() * 10000) + 100;
            try {
                Thread.sleep(i);
            } catch (InterruptedException e) {

            }
            out.println(Thread.currentThread().getName() + " done with result=" + i);
            return i;
        });

        out.println("result=" + task.getResult());
        out.println("shut down executor pool.");
        task.shutdown();
    }
}
