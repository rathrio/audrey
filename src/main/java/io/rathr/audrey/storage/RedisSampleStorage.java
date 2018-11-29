package io.rathr.audrey.storage;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public final class RedisSampleStorage implements SampleStorage {
    private final JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");

    @Override
    public void add(final Sample sample) {
        final Jedis redis = pool.getResource();
        redis.sadd(sample.getRootNodeId(), sample.toString());
    }

    @Override
    public void clear() {
    }
}
