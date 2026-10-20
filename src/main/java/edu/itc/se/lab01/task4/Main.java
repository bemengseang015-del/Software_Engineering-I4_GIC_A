package edu.itc.se.lab01.task4;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Main {
    private static final long LIMIT = 4_000_000;
    private static final int CHUNKS = 16;
    private static final int DOWNLOADS = 500;
    private static final int TIMEOUT_INDEX = 1;

    public static void main(String[] args) throws Exception {
        benchmarkPrimes();
        benchmarkDownloads("fixed(20) platform threads", Executors.newFixedThreadPool(20));
        benchmarkDownloads("virtual threads", Executors.newVirtualThreadPerTaskExecutor());
    }

    private static void benchmarkPrimes() throws InterruptedException, ExecutionException {
        long start = System.nanoTime();
        long singleThreaded = new PrimeCounter(0, LIMIT).call();
        System.out.printf("single-threaded: %d primes in %d ms%n",
                singleThreaded, elapsedMillis(start));

        start = System.nanoTime();
        try (ExecutorService pool = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors())) {
            List<Future<Long>> futures = new ArrayList<>();
            long chunkSize = LIMIT / CHUNKS;
            for (int chunk = 0; chunk < CHUNKS; chunk++) {
                long from = chunk * chunkSize;
                long to = chunk == CHUNKS - 1 ? LIMIT : from + chunkSize;
                futures.add(pool.submit(new PrimeCounter(from, to)));
            }

            long total = 0;
            for (Future<Long> future : futures) {
                total += future.get();
            }
            System.out.printf("pool (%d threads): %d primes in %d ms%n",
                    Runtime.getRuntime().availableProcessors(), total, elapsedMillis(start));
            if (total != singleThreaded) {
                throw new AssertionError("Parallel prime count does not match single-threaded result");
            }
        }
    }

    private static void benchmarkDownloads(String label, ExecutorService executor)
            throws InterruptedException {
        long start = System.nanoTime();
        int ok = 0;
        int failed = 0;
        int timedOut = 0;

        try (executor) {
            List<Future<String>> futures = new ArrayList<>(DOWNLOADS);
            for (int i = 0; i < DOWNLOADS; i++) {
                boolean shouldFail = (i + 1) % 50 == 0;
                long delay = i == TIMEOUT_INDEX ? 5_000 : 200;
                futures.add(executor.submit(new FakeDownload(
                        "https://example.test/" + i, shouldFail, delay)));
            }

            try {
                futures.get(TIMEOUT_INDEX).get(1, TimeUnit.SECONDS);
                ok++;
            } catch (TimeoutException e) {
                futures.get(TIMEOUT_INDEX).cancel(true);
                timedOut++;
                System.out.println(label + ": download " + TIMEOUT_INDEX + " timed out");
            } catch (ExecutionException e) {
                failed++;
                System.err.println(label + ": download " + TIMEOUT_INDEX + " failed: " + e.getCause());
            }

            for (int i = 0; i < futures.size(); i++) {
                if (i == TIMEOUT_INDEX) {
                    continue;
                }
                try {
                    futures.get(i).get();
                    ok++;
                } catch (ExecutionException e) {
                    failed++;
                }
            }
        }

        System.out.printf("downloads on %-30s: ok=%d failed=%d timedOut=%d in %d ms%n",
                label, ok, failed, timedOut, elapsedMillis(start));
        if (ok != 489 || failed != 10 || timedOut != 1) {
            throw new AssertionError(
                    "Unexpected download summary: ok=" + ok + ", failed=" + failed + ", timedOut=" + timedOut);
        }
    }

    private static long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
