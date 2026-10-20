package edu.itc.se.lab01.task5;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

public final class OrderedBank {
    private static final int ACCOUNT_COUNT = 4;
    private static final int WORKER_COUNT = 8;
    private static final int TRANSFERS_PER_WORKER = 10_000;
    private static final long INITIAL_BALANCE = 1_000;

    static final class Account {
        final int id;
        private long balance;

        Account(int id, long balance) {
            this.id = id;
            this.balance = balance;
        }
    }

    private final Account[] accounts = new Account[ACCOUNT_COUNT];
    private final AtomicLong transfers = new AtomicLong();

    public OrderedBank() {
        for (int i = 0; i < accounts.length; i++) {
            accounts[i] = new Account(i, INITIAL_BALANCE);
        }
    }

    private void transfer(Account first, Account second, long amount) {
        if (first == second) {
            return;
        }
        Account lowerId = first.id < second.id ? first : second;
        Account higherId = first.id < second.id ? second : first;

        synchronized (lowerId) {
            synchronized (higherId) {
                if (first.balance >= amount) {
                    first.balance -= amount;
                    second.balance += amount;
                }
            }
        }
    }

    private void doRandomTransfer() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Account from = accounts[random.nextInt(accounts.length)];
        Account to = accounts[random.nextInt(accounts.length)];
        transfer(from, to, random.nextInt(1, 50));
        transfers.incrementAndGet();
    }

    private long totalBalance() {
        long total = 0;
        for (Account account : accounts) {
            synchronized (account) {
                total += account.balance;
            }
        }
        return total;
    }

    public static void main(String[] args) throws InterruptedException {
        OrderedBank bank = new OrderedBank();
        Thread[] workers = new Thread[WORKER_COUNT];
        long start = System.nanoTime();

        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Thread(() -> {
                for (int transfer = 0; transfer < TRANSFERS_PER_WORKER; transfer++) {
                    bank.doRandomTransfer();
                }
            }, "ordered-transfer-" + i);
            workers[i].start();
        }
        for (Thread worker : workers) {
            worker.join();
        }

        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
        long expected = ACCOUNT_COUNT * INITIAL_BALANCE;
        long total = bank.totalBalance();
        System.out.printf("transfers=%d total=%d (expected %d) in %d ms%n",
                bank.transfers.get(), total, expected, elapsedMillis);
        if (total != expected) {
            throw new AssertionError("Account total changed");
        }
    }
}
