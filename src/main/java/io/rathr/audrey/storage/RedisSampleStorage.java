package io.rathr.audrey.storage;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisSetCommands;
import io.lettuce.core.api.sync.RedisStringCommands;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public final class RedisSampleStorage extends SampleStorage {
    private final RedisClient client = RedisClient.create("redis://localhost");
    private final StatefulRedisConnection<String, String> connection = client.connect();
    private final RedisAsyncCommands<String, String> async = connection.async();
//    private final RedisSetCommands<String, String> sync = connection.sync();


    private final Project project;
    private final String samplesKey;
    private final String projectKey;

    public RedisSampleStorage(final Project project, final boolean registerProject) {
        this.project = project;
        this.projectKey = "audrey:" + project.getId();
        this.samplesKey = projectKey + ":samples";

        if (registerProject) {
            registerProject(project);
        }
    }

    public RedisSampleStorage(final Project project) {
        this(project, true);
    }

    @Override
    public void add(final Sample sample) {
        async.sadd(samplesKey, toJson(sample));
//        sync.sadd(samplesKey, toJson(sample));
    }

    @Override
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

    private void registerProject(final Project project) {
        async.hset(projectKey, "path", project.getRootPath());
    }
}
