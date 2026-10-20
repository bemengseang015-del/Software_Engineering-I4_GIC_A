package edu.itc.se.lab01.task3;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public final class Consumer implements Runnable {
    private final String name;
    private final OrderQueue<Order> queue;
    private final AtomicInteger processedCount;

    public Consumer(String name, OrderQueue<Order> queue, AtomicInteger processedCount) {
        this.name = name;
        this.queue = queue;
        this.processedCount = processedCount;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Order order = queue.take();
                if (order.id() == -1) {
                    System.out.println(name + " received poison pill, stopping");
                    return;
                }

                Thread.sleep(ThreadLocalRandom.current().nextLong(5, 31));
                processedCount.incrementAndGet();
                System.out.printf("%s processed %s%n", name, order);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println(name + " was interrupted before finishing");
        }
    }
}
