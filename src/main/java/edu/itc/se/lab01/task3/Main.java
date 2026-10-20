package edu.itc.se.lab01.task3;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        ProducerConsumerDemo.run(new BoundedBuffer<>(5));
    }
}
