package io.rathr.audrey.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemorySampleStorage implements SampleStorage {
    private final Map<String, Set<Sample>> sampleMap = new ConcurrentHashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void add(final Sample sample) {
        sampleMap.computeIfAbsent(sample.getRootNodeId(), section -> ConcurrentHashMap.newKeySet());
        sampleMap.get(sample.getRootNodeId()).add(sample);
    }

    public Set<Sample> getAll() {
        return new HashSet(sampleMap.values());
    }

    @Override
    public void clear() {
        sampleMap.clear();
    }

    @Override
    public void onDispose() {
        System.out.println(GSON.toJson(sampleMap.values().toArray()));
        clear();
    }
}
