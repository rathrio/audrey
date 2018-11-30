package io.rathr.audrey.storage;

import com.oracle.truffle.api.source.SourceSection;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemorySampleStorage implements SampleStorage {
    private final Map<String, Set<Sample>> sampleMap = new ConcurrentHashMap<>();

    @Override
    public void add(final Sample sample) {
        sampleMap.computeIfAbsent(sample.getId(), section -> ConcurrentHashMap.newKeySet());
        sampleMap.get(sample.getId()).add(sample);
    }

    @Override
    public void clear() {
        sampleMap.clear();
    }
}
