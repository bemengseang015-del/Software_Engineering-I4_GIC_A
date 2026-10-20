package edu.itc.se.lab01.task3;

import java.util.ArrayDeque;
import java.util.Objects;

public class BoundedBuffer<T> implements OrderQueue<T> {
    private final ArrayDeque<T> items = new ArrayDeque<>();
    private final int capacity;

    public BoundedBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.capacity = capacity;
    }

    @Override
    public synchronized void put(T item) throws InterruptedException {
        Objects.requireNonNull(item, "item");
        while (items.size() == capacity) {
            wait();
        }
        assert items.size() < capacity : "buffer is full";
        items.addLast(item);
        assert items.size() <= capacity : "buffer exceeded capacity";
        notifyAll();
    }

    @Override
    public synchronized T take() throws InterruptedException {
        while (items.isEmpty()) {
            wait();
        }
        assert !items.isEmpty() : "buffer is empty";
        T item = items.removeFirst();
        assert item != null : "buffer returned a null item";
        notifyAll();
        return item;
    }
}
