package edu.itc.se.lab01.task1;

public class Countdown extends Thread {
    public Countdown(String name) {
        super(name);
    }

    @Override
    public void run() {
        new CountdownTask().run();
    }
}
