package com.kronos.runtime.io;

import static com.kronos.utils.FutureUtils.assertNoException;

import java.util.concurrent.CompletableFuture;

/** */
public class MultipleFuturesAvailabilityHelper {

    private final CompletableFuture<?>[] futuresToCombine;

    private volatile CompletableFuture<?> availableFuture = new CompletableFuture<>();

    public MultipleFuturesAvailabilityHelper(int size) {
        futuresToCombine = new CompletableFuture[size];
    }

    /** @return combined future using anyOf logic */
    public CompletableFuture<?> getAvailableFuture() {
        return availableFuture;
    }

    public void resetToUnAvailable() {
        if (availableFuture.isDone()) {
            availableFuture = new CompletableFuture<>();
        }
    }

    private void notifyCompletion() {
        availableFuture.complete(null);
    }

    /**
     * Combine {@code availabilityFuture} using anyOf logic with other previously registered
     * futures.
     */
    public void anyOf(final int idx, CompletableFuture<?> availabilityFuture) {
        if (futuresToCombine[idx] == null || futuresToCombine[idx].isDone()) {
            futuresToCombine[idx] = availabilityFuture;
            assertNoException(availabilityFuture.thenRun(this::notifyCompletion));
        }
    }
}
