package edu.itc.se.lab01.task4;

import java.io.IOException;
import java.util.concurrent.Callable;

public final class FakeDownload implements Callable<String> {
    private final String url;
    private final boolean shouldFail;
    private final long delayMillis;

    public FakeDownload(String url, boolean shouldFail, long delayMillis) {
        this.url = url;
        this.shouldFail = shouldFail;
        this.delayMillis = delayMillis;
    }

    @Override
    public String call() throws IOException, InterruptedException {
        Thread.sleep(delayMillis);
        if (shouldFail) {
            throw new IOException("boom");
        }
        return url + " -> " + url.length();
    }
}
