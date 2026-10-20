package edu.itc.se.lab01.task3;

import java.util.concurrent.atomic.AtomicInteger;

final class ProducerConsumerDemo {
    private static final int PRODUCER_COUNT = 2;
    private static final int ORDERS_PER_PRODUCER = 50;
    private static final int CONSUMER_COUNT = 3;

    private ProducerConsumerDemo() {
    }

    static void run(OrderQueue<Order> queue) throws InterruptedException {
        AtomicInteger processedCount = new AtomicInteger();
        AtomicInteger remainingProducers = new AtomicInteger(PRODUCER_COUNT);
        Thread[] consumers = new Thread[CONSUMER_COUNT];
        Thread[] producers = new Thread[PRODUCER_COUNT];
        long start = System.nanoTime();

        for (int i = 0; i < CONSUMER_COUNT; i++) {
            String name = "consumer-" + (i + 1);
            consumers[i] = new Thread(new Consumer(name, queue, processedCount), name);
            consumers[i].start();
        }

        for (int i = 0; i < PRODUCER_COUNT; i++) {
            String name = "producer-" + (i + 1);
            int firstId = i * ORDERS_PER_PRODUCER + 1;
            producers[i] = new Thread(
                    new Producer(name, firstId, ORDERS_PER_PRODUCER, CONSUMER_COUNT, queue, remainingProducers),
                    name);
            producers[i].start();
        }

        for (Thread producer : producers) {
            producer.join();
        }
        for (Thread consumer : consumers) {
            consumer.join();
        }

        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
        int expected = PRODUCER_COUNT * ORDERS_PER_PRODUCER;
        int actual = processedCount.get();
        System.out.printf("Total processed: %d (expected %d) in %d ms%n",
                actual, expected, elapsedMillis);
        if (actual != expected) {
            throw new AssertionError("Expected " + expected + " processed orders, got " + actual);
        }
    }
}
