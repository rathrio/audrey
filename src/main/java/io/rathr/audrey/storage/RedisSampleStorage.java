package io.rathr.audrey.storage;

import com.oracle.truffle.api.instrumentation.TruffleInstrument;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public final class RedisSampleStorage extends SampleStorage {
    private final RedisClient client = RedisClient.create("redis://localhost");
    private final StatefulRedisConnection<String, String> connection = client.connect();
    private final RedisAsyncCommands<String, String> async = connection.async();
    private final Project project;
    private final String samplesKey;
    private final String projectKey;

    public RedisSampleStorage(final Project project) {
        this.project = project;
        this.projectKey = "audrey:" + project.getId();
        this.samplesKey = projectKey + ":samples";

        registerProject(project);
    }

    @Override
    public void add(final Sample sample) {
        async.sadd(samplesKey, toJson(sample));
    }

    private void registerProject(final Project project) {
        async.hset(projectKey, "path", project.getRootPath());
    }

    public Set<Sample> getSamples() {
        try {
            return async.smembers(samplesKey)
                .get()
                .stream()
                .map(this::fromJson)
                .collect(Collectors.toSet());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return new HashSet<>();
    }

    @Override
    public void clear() {
    }

    @Override
    public void onDispose(final TruffleInstrument.Env env) {
    }
}
