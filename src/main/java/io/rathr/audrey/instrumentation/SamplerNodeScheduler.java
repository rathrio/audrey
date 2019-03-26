package io.rathr.audrey.instrumentation;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SamplerNodeScheduler {
    private final long intervalInSeconds;
    private final ScheduledExecutorService executor;

    public SamplerNodeScheduler(long intervalInSeconds) {
        this.intervalInSeconds = intervalInSeconds;
        this.executor = Executors.newScheduledThreadPool(2);
    }

    public void start() {
        executor.scheduleAtFixedRate(() -> {
            System.out.println("======================================= HI FROM SCHEDULAR");
        }, intervalInSeconds, intervalInSeconds, TimeUnit.SECONDS);
    }

    public void stop() {
        executor.shutdownNow();
    }
}
