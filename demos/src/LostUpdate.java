package mapdemo;

import java.util.concurrent.atomic.AtomicInteger;

public class LostUpdate {
    static volatile int volatileCount;
    static final AtomicInteger atomicCount = new AtomicInteger();
    static int syncCount;

    static void run(String title, Runnable body, int threads, int loops) throws InterruptedException {
        Thread[] workers = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            workers[i] = new Thread(() -> {
                for (int j = 0; j < loops; j++) {
                    body.run();
                }
            });
            workers[i].start();
        }
        for (Thread worker : workers) {
            worker.join();
        }
        System.out.println(title);
    }

    public static void main(String[] args) throws InterruptedException {
        int threads = 8;
        int loops = 100_000;
        int expect = threads * loops;
        run("volatile 自增做完", () -> volatileCount++, threads, loops);
        System.out.println("  期望 " + expect + " 实际 " + volatileCount);
        run("AtomicInteger 做完", atomicCount::incrementAndGet, threads, loops);
        System.out.println("  期望 " + expect + " 实际 " + atomicCount.get());
        Object lock = new Object();
        run("synchronized 做完", () -> {
            synchronized (lock) {
                syncCount++;
            }
        }, threads, loops);
        System.out.println("  期望 " + expect + " 实际 " + syncCount);
    }
}
