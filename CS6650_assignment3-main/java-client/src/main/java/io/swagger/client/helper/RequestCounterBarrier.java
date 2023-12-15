package io.swagger.client.helper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class RequestCounterBarrier {

    private AtomicInteger count = new AtomicInteger(0);
    private int successfulRequests = 0;
    private int failedRequests = 0;

    public void inc() {
        count.incrementAndGet();
    }

    public int getVal() {
        return this.count.get();
    }

    public synchronized void incrementSuccessfulRequests() {
        successfulRequests++;
    }

    public synchronized void incrementFailedRequests() {
        failedRequests++;
    }

    public int getSuccessfulRequests() {
        return successfulRequests;
    }

    public int getFailedRequests() {
        return failedRequests;
    }
}
