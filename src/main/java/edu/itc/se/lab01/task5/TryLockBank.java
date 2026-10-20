package edu.itc.se.lab01.task5;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public final class TryLockBank {
    private static final int ACCOUNT_COUNT = 4;
    private static final int WORKER_COUNT = 8;
    private static final int TRANSFERS_PER_WORKER = 10_000;
    private static final long INITIAL_BALANCE = 1_000;

    static final class Account {
        final int id;
        final ReentrantLock lock = new ReentrantLock();
        long balance;

        Account(int id, long balance) {
            this.id = id;
            this.balance = balance;
        }
    }

    private final Account[] accounts = new Account[ACCOUNT_COUNT];
    private final AtomicLong retries = new AtomicLong();
    private final AtomicLong transfers = new AtomicLong();
    private final AtomicLong successfulTransfers = new AtomicLong();

    public TryLockBank() {
        for (int i = 0; i < accounts.length; i++) {
            accounts[i] = new Account(i, INITIAL_BALANCE);
        }
    }

    private boolean transfer(Account from, Account to, long amount) throws InterruptedException {
        if (from == to) {
            return false;
        }
        while (true) {
            boolean lockedFrom = false;
            boolean lockedTo = false;
            boolean transferred = false;
            try {
                lockedFrom = from.lock.tryLock(10, TimeUnit.MILLISECONDS);
                if (lockedFrom) {
                    lockedTo = to.lock.tryLock(10, TimeUnit.MILLISECONDS);
                }
                if (lockedFrom && lockedTo && from.balance >= amount) {
                    from.balance -= amount;
                    to.balance += amount;
                    transferred = true;
                }
                if (lockedFrom && lockedTo) {
                    return transferred;
                }
            } finally {
                if (lockedTo) {
                    to.lock.unlock();
                }
                if (lockedFrom) {
                    from.lock.unlock();
                }
            }

            retries.incrementAndGet();
            Thread.sleep(ThreadLocalRandom.current().nextLong(1, 11));
        }
    }

    private void doRandomTransfer() throws InterruptedException {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Account from = accounts[random.nextInt(accounts.length)];
        Account to = accounts[random.nextInt(accounts.length)];
        if (from != to && transfer(from, to, random.nextLong(1, 50))) {
            successfulTransfers.incrementAndGet();
        }
        transfers.incrementAndGet();
    }

    private long totalBalance() {
        for (Account account : accounts) {
            account.lock.lock();
        }
        try {
            long total = 0;
            for (Account account : accounts) {
                total += account.balance;
            }
            return total;
        } finally {
            for (int i = accounts.length - 1; i >= 0; i--) {
                accounts[i].lock.unlock();
            }
        }
    }

    private void runFixedWorkload() throws InterruptedException {
        Thread[] workers = new Thread[WORKER_COUNT];
        long start = System.nanoTime();

        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Thread(() -> {
                try {
                    for (int transfer = 0; transfer < TRANSFERS_PER_WORKER; transfer++) {
                        doRandomTransfer();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "trylock-transfer-" + i);
            workers[i].start();
        }
        for (Thread worker : workers) {
            worker.join();
        }

        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
        long expected = ACCOUNT_COUNT * INITIAL_BALANCE;
        long total = totalBalance();
        System.out.printf("transfers=%d retries=%d total=%d (expected %d) in %d ms%n",
                transfers.get(), retries.get(), total, expected, elapsedMillis);
        if (total != expected) {
            throw new AssertionError("Account total changed");
        }
    }

    void transferUntilInterrupted() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                doRandomTransfer();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new TryLockBank().runFixedWorkload();
    }

    long completedTransfers() {
        return successfulTransfers.get();
    }

    long transferAttempts() {
        return transfers.get();
    }

    long retryCount() {
        return retries.get();
    }

    long currentTotalBalance() {
        return totalBalance();
    }
}
