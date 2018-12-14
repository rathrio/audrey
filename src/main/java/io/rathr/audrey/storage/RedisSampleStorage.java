package io.rathr.audrey.storage;


import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.rathr.audrey.instrumentation.Project;

public final class RedisSampleStorage implements SampleStorage {
    private final RedisClient client = RedisClient.create("redis://localhost");
    private final StatefulRedisConnection<String, String> connection = client.connect();
    private final RedisAsyncCommands<String, String> async = connection.async();
    private final Project project;

    public RedisSampleStorage(final Project project) {
        this.project = project;
    }

    @Override
    public void add(final Sample sample) {
        async.sadd("audrey_samples", sample.toString());
    }

    @Override
    public void clear() {
    }
}
