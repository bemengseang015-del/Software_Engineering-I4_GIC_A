package edu.itc.se.lab01.task1;

/*
 * With normal start(), the countdowns run concurrently and Mission control:
 * all launched is printed while they are running. With --sequential, each
 * thread is started and joined before the next one starts, so the countdowns
 * finish one at a time before the all-launched message.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        boolean sequential = args.length > 0 && args[0].equals("--sequential");

        Thread apollo = new Countdown("apollo");
        Thread gemini = new Thread(new CountdownTask(), "gemini");
        Thread mercury = new Thread(() -> new CountdownTask().run(), "mercury");
        Thread artemis = Thread.ofVirtual().name("artemis").unstarted(new CountdownTask());
        Thread[] threads = {apollo, gemini, mercury, artemis};

        if (sequential) {
            for (Thread thread : threads) {
                thread.start();
                thread.join();
            }
        } else {
            for (Thread thread : threads) {
                thread.start();
            }
        }
        System.out.println("Mission control: all launched");

        for (Thread thread : threads) {
            thread.join();
        }
        System.out.println("Mission control: all landed");

        for (Thread thread : threads) {
            System.out.printf(
                "%s: isVirtual=%s, state=%s%n",
                thread.getName(),
                thread.isVirtual(),
                thread.getState()
            );
        }
    }
}
