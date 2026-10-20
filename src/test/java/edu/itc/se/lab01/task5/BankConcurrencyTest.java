package edu.itc.se.lab01.task5;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class BankConcurrencyTest {
    @Test
    void orderedLocksCompleteWorkloadAndPreserveTotal() throws Exception {
        String output = captureOutput(() -> OrderedBank.main(new String[0]));

        assertTrue(output.contains("transfers=80000"));
        assertTrue(output.contains("total=4000 (expected 4000)"));
    }

    @Test
    void tryLockCompletesWorkloadAndPreservesTotal() throws Exception {
        String output = captureOutput(() -> TryLockBank.main(new String[0]));

        assertTrue(output.contains("transfers=80000"));
        assertTrue(output.contains("total=4000 (expected 4000)"));
    }

    @Test
    void workersStopAfterInterruptionAndPreserveTotal() throws Exception {
        String output = captureOutput(() -> GracefulShutdown.main(new String[0]));

        assertTrue(output.contains("all workers stopped=true"));
        assertTrue(output.contains("total=4000 (expected 4000)"));
    }

    private static String captureOutput(ThrowingRunnable action) throws Exception {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            action.run();
        } finally {
            System.setOut(originalOut);
        }
        return output.toString(StandardCharsets.UTF_8);
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
