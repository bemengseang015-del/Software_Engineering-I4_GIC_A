package edu.itc.se.lab01.task3;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public final class Producer implements Runnable {
    private static final Order POISON_PILL = new Order(-1, "poison", 0);

    private final String name;
    private final int firstOrderId;
    private final int orderCount;
    private final int consumerCount;
    private final OrderQueue<Order> queue;
    private final AtomicInteger remainingProducers;

    public Producer(
            String name,
            int firstOrderId,
            int orderCount,
            int consumerCount,
            OrderQueue<Order> queue,
            AtomicInteger remainingProducers) {
        this.name = name;
        this.firstOrderId = firstOrderId;
        this.orderCount = orderCount;
        this.consumerCount = consumerCount;
        this.queue = queue;
        this.remainingProducers = remainingProducers;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < orderCount; i++) {
                int id = firstOrderId + i;
                int qty = ThreadLocalRandom.current().nextInt(1, 6);
                queue.put(new Order(id, "item-" + id, qty));
                Thread.sleep(ThreadLocalRandom.current().nextLong(21));
            }

            if (remainingProducers.decrementAndGet() == 0) {
                for (int i = 0; i < consumerCount; i++) {
                    queue.put(POISON_PILL);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println(name + " was interrupted before finishing production");
        }
    }
}
