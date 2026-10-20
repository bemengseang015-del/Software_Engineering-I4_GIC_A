package edu.itc.se.lab01.task4;

import java.util.concurrent.Callable;

public final class PrimeCounter implements Callable<Long> {
    private final long from;
    private final long to;

    public PrimeCounter(long from, long to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public Long call() {
        long count = 0;
        for (long number = Math.max(from, 2); number < to; number++) {
            if (isPrime(number)) {
                count++;
            }
        }
        return count;
    }

    static boolean isPrime(long number) {
        for (long divisor = 2; divisor <= number / divisor; divisor++) {
            if (number % divisor == 0) {
                return false;
            }
        }
        return true;
    }
}
