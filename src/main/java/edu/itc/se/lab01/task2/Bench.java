package edu.itc.se.lab01.task2;

import java.util.ArrayList;
import java.util.List;

public class Bench {
    static final int THREADS = 8;
    static final int ITERATIONS = 1_000_000;

    static void run(String label, Counter counter) throws InterruptedException {
        long start = System.nanoTime();
        List<Thread> threads = new ArrayList<>();
        for (int t = 0; t < THREADS; t++) {
            Thread thread = new Thread(() -> {
                for (int i = 0; i < ITERATIONS; i++) {
                    counter.increment();
                }
            }, "inc-" + t);
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        long millis = (System.nanoTime() - start) / 1_000_000;
        long expected = (long) THREADS * ITERATIONS;
        long value = counter.get();
        System.out.printf("%-22s value=%,d  correct=%-5b  %d ms%n",
                label, value, value == expected, millis);
    }

    public static void main(String[] args) throws InterruptedException {
        // Observed unsafe values: 2,145,772; 4,403,453; 8,000,000.
        // Lost updates are nondeterministic; the third run happened to be correct.
        run("UnsafeCounter run 1", new UnsafeCounter());
        run("UnsafeCounter run 2", new UnsafeCounter());
        run("UnsafeCounter run 3", new UnsafeCounter());

        run("SynchronizedCounter", new SynchronizedCounter());
        run("LockCounter", new LockCounter());
        run("AtomicCounter", new AtomicCounter());
        run("LongAdderCounter", new LongAdderCounter());
    }
}
