package edu.itc.se.lab01.task3;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public final class BlockingOrderQueue implements OrderQueue<Order> {
    private final BlockingQueue<Order> queue;

    public BlockingOrderQueue(int capacity) {
        queue = new ArrayBlockingQueue<>(capacity);
    }

    @Override
    public void put(Order order) throws InterruptedException {
        queue.put(order);
    }

    @Override
    public Order take() throws InterruptedException {
        return queue.take();
    }
}
