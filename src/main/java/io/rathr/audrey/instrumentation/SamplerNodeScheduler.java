package io.rathr.audrey.instrumentation;

import io.rathr.audrey.instrumentation.nodes.SchedulableNode;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SamplerNodeScheduler {
    /**
     * How often to update the current bucket.
     */
    private final long intervalInSeconds;
    private final ScheduledExecutorService executor;

    private static final int CORE_POOL_SIZE = 2;

    /**
     * How many buckets to distribute the nodes in.
     */
    private final int numBuckets;

    /**
     * The bucket of nodes that are currently enabled.
     */
    private int currentBucket = 0;

    /**
     * Next bucket to place new nodes in.
     *
     * @see #register(SchedulableNode)
     */
    private int bucketForNewNodes = 0;

    private final Map<Integer, Set<SchedulableNode>> buckets = new ConcurrentHashMap<>();

    SamplerNodeScheduler(long intervalInSeconds, int numBuckets) {
        this.intervalInSeconds = intervalInSeconds;
        this.numBuckets = numBuckets;
        this.executor = Executors.newScheduledThreadPool(CORE_POOL_SIZE);
    }

    /**
     * Starts enabling/disabling nodes with the configured schedule.
     */
    void start() {
        executor.scheduleAtFixedRate(() -> {
            final int prevBucket = currentBucket;
            currentBucket = (currentBucket + 1) % numBuckets;

            buckets.computeIfAbsent(currentBucket, i -> ConcurrentHashMap.newKeySet());
            buckets.get(currentBucket).forEach(SchedulableNode::enable);

            buckets.computeIfAbsent(prevBucket, i -> ConcurrentHashMap.newKeySet());
            buckets.get(prevBucket).forEach(SchedulableNode::disable);

            System.out.println("==== Enabled bucket " + currentBucket + " with " + buckets.get(currentBucket).size() + " nodes");
        }, intervalInSeconds, intervalInSeconds, TimeUnit.SECONDS);
    }

    void stop() {
        executor.shutdownNow();
    }

    /**
     * Register a node for scheduling. Note that the node is initially disabled when passed to this method.
     * @param node the node to enable/disable with the configured schedule.
     */
    void register(final SchedulableNode node) {
        node.disable();

        buckets.computeIfAbsent(bucketForNewNodes, i -> ConcurrentHashMap.newKeySet());
        buckets.get(bucketForNewNodes).add(node);
        bucketForNewNodes = (bucketForNewNodes + 1) % numBuckets;
    }
}
