package edu.itc.se.lab01.task3;

public class Main2 {
    public static void main(String[] args) throws InterruptedException {
        ProducerConsumerDemo.run(new BlockingOrderQueue(5));
    }
}
