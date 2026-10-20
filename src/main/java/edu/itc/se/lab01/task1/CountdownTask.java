package edu.itc.se.lab01.task1;

public class CountdownTask implements Runnable {
    @Override
    public void run() {
        String name = Thread.currentThread().getName();
        String[] lines = {"3", "2", "1", "liftoff!"};

        for (int i = 0; i < lines.length; i++) {
            System.out.println(name + ": " + lines[i]);
            if (i < lines.length - 1) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}
