package io.swagger.client.helper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class RequestCounterBarrier {

    private AtomicInteger count = new AtomicInteger(0);

    public void inc() {
        count.incrementAndGet();
    }

    public int getVal() {
        return this.count.get();
    }
}

