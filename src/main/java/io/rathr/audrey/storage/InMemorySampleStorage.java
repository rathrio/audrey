package io.rathr.audrey.storage;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemorySampleStorage extends SampleStorage {
    private final Map<String, Set<Sample>> sampleMap = new ConcurrentHashMap<>();

    @Override
    public void add(final Sample sample) {
        sampleMap.computeIfAbsent(sample.getRootNodeId(), section -> ConcurrentHashMap.newKeySet());
        sampleMap.get(sample.getRootNodeId()).add(sample);
    }

    @Override
    public void clear() {
        sampleMap.clear();
    }

    @Override
    public Set<Sample> getSamples() {
        final HashSet<Sample> samples = new HashSet<>();
        sampleMap.values().forEach(samples::addAll);
        return samples;
    }

    public SampleFilter newSearch() {
        return new SampleFilter(getSamples());
    }
}
