package io.rathr.audrey.instrumentation;

import io.rathr.audrey.instrumentation.nodes.SamplerNode;
import io.rathr.audrey.instrumentation.nodes.SchedulableNode;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SamplerNodeScheduler {
    private final long intervalInSeconds;
    private final ScheduledExecutorService executor;
    private static final int NUM_BUCKETS = 10;
    private int currentBucket = 0;

    private final Map<Integer, Set<SamplerNode>> buckets = new ConcurrentHashMap<>();

    public SamplerNodeScheduler(long intervalInSeconds) {
        this.intervalInSeconds = intervalInSeconds;
        this.executor = Executors.newScheduledThreadPool(2);
    }

    public void start() {
        executor.scheduleAtFixedRate(() -> {
            final int prevBucket = currentBucket;
            currentBucket = (currentBucket + 1) % NUM_BUCKETS;

            buckets.computeIfAbsent(currentBucket, i -> ConcurrentHashMap.newKeySet());
            buckets.get(currentBucket).forEach(SchedulableNode::enable);

            buckets.computeIfAbsent(prevBucket, i -> ConcurrentHashMap.newKeySet());
            buckets.get(prevBucket).forEach(SchedulableNode::disable);

            System.out.println("==== Enabled bucket " + currentBucket + " with " + buckets.get(currentBucket).size() + " nodes");

        }, intervalInSeconds, intervalInSeconds, TimeUnit.SECONDS);
    }

    public void stop() {
        executor.shutdownNow();
    }

    public void register(final SamplerNode node) {
        final int bucket = node.getSourceSectionId() % NUM_BUCKETS;
        buckets.computeIfAbsent(bucket, i -> ConcurrentHashMap.newKeySet());
        node.disable();
        buckets.get(bucket).add(node);
    }
}
