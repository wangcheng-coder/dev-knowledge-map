package mapdemo;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ThreadPoolReject {
    public static void main(String[] args) throws InterruptedException {
        CountDownLatch release = new CountDownLatch(1);
        AtomicBoolean entered = new AtomicBoolean();
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                1, 1, 0, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(1),
                runnable -> {
                    Thread thread = new Thread(runnable, "worker-1");
                    thread.setDaemon(true);
                    return thread;
                },
                (runnable, executor) -> System.out.println("拒绝: 唯一线程在忙，队列容量 1 已满"));
        pool.execute(() -> {
            entered.set(true);
            try {
                release.await();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        });
        while (!entered.get()) {
            Thread.onSpinWait();
        }
        System.out.println("第 1 个任务占用 worker-1，池大小=" + pool.getPoolSize() + " 队列=" + pool.getQueue().size());
        pool.execute(() -> System.out.println("队列中的任务现在才开始执行"));
        System.out.println("第 2 个任务入队后，池大小=" + pool.getPoolSize() + " 队列=" + pool.getQueue().size());
        pool.execute(() -> System.out.println("如果打印了这行，说明拒绝策略没有生效"));
        System.out.println("第 3 个任务被拒绝后，池大小=" + pool.getPoolSize() + " 队列=" + pool.getQueue().size());
        release.countDown();
        pool.shutdown();
        pool.awaitTermination(2, TimeUnit.SECONDS);
    }
}
