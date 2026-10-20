package edu.itc.se.lab01.task5;

public final class GracefulShutdown {
    private static final int WORKER_COUNT = 8;

    private GracefulShutdown() {
    }

    public static void main(String[] args) throws InterruptedException {
        TryLockBank bank = new TryLockBank();
        Thread[] workers = new Thread[WORKER_COUNT];

        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Thread(bank::transferUntilInterrupted, "transfer-" + i);
            workers[i].start();
        }

        Thread.sleep(2_000);
        System.out.println("[main] interrupting " + workers.length + " workers after 2000 ms");
        for (Thread worker : workers) {
            worker.interrupt();
        }
        for (Thread worker : workers) {
            worker.join(1_000);
        }

        boolean allStopped = true;
        for (Thread worker : workers) {
            if (worker.isAlive()) {
                allStopped = false;
            }
        }

        long expectedTotal = 4_000;
        long total = bank.currentTotalBalance();
        System.out.printf("completed transfers=%d attempts=%d retries=%d%n",
                bank.completedTransfers(), bank.transferAttempts(), bank.retryCount());
        System.out.printf("all workers stopped=%s total=%d (expected %d)%n",
                allStopped, total, expectedTotal);
        if (!allStopped || total != expectedTotal) {
            throw new AssertionError("Workers did not stop cleanly or account total changed");
        }
    }
}
