package edu.itc.se.lab01.task5;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.ThreadLocalRandom;

public class Bank {
    static final class Account {
        final int id;
        private long balance;

        Account(int id, long balance) {
            this.id = id;
            this.balance = balance;
        }

        long balance() {
            return balance;
        }

        void withdraw(long amount) {
            balance -= amount;
        }

        void deposit(long amount) {
            balance += amount;
        }
    }

    static void transfer(Account from, Account to, long amount) {
        synchronized (from) {
            synchronized (to) {
                if (from.balance() >= amount) {
                    from.withdraw(amount);
                    to.deposit(amount);
                }
            }
        }
    }

    private static void startDeadlockWatchdog() {
        Thread watchdog = new Thread(() -> {
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            while (true) {
                long[] deadlockedIds = threadBean.findDeadlockedThreads();
                if (deadlockedIds != null) {
                    ThreadInfo[] infos = threadBean.getThreadInfo(deadlockedIds);
                    StringBuilder names = new StringBuilder();
                    for (ThreadInfo info : infos) {
                        if (!names.isEmpty()) {
                            names.append(", ");
                        }
                        names.append(info.getThreadName());
                    }
                    System.out.println("[watchdog] DEADLOCK detected between: " + names);
                }
                try {
                    Thread.sleep(1_000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }, "deadlock-watchdog");
        watchdog.setDaemon(true);
        watchdog.start();
    }

    public static void main(String[] args) throws InterruptedException {
        Account[] accounts = new Account[4];
        for (int i = 0; i < accounts.length; i++) {
            accounts[i] = new Account(i, 1_000);
        }

        startDeadlockWatchdog();
        Thread[] workers = new Thread[8];
        for (int worker = 0; worker < workers.length; worker++) {
            workers[worker] = new Thread(() -> {
                ThreadLocalRandom random = ThreadLocalRandom.current();
                while (!Thread.currentThread().isInterrupted()) {
                    Account from = accounts[random.nextInt(accounts.length)];
                    Account to = accounts[random.nextInt(accounts.length)];
                    if (from != to) {
                        transfer(from, to, random.nextInt(1, 50));
                    }
                }
            }, "transfer-" + worker);
            workers[worker].start();
        }

        for (Thread worker : workers) {
            worker.join();
        }
    }
}
