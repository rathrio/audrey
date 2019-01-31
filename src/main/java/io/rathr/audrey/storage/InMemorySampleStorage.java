package io.rathr.audrey.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.oracle.truffle.api.instrumentation.TruffleInstrument;

import java.io.PrintStream;
import java.util.ArrayList;
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

    @Override
    public void clear() {
        sampleMap.clear();
    }

    @Override
    public void onDispose(final TruffleInstrument.Env env) {
        final ArrayList<Sample> samples = new ArrayList<>();
        sampleMap.values().forEach(samples::addAll);
        final String output = GSON.toJson(samples);

        final PrintStream out = new PrintStream(env.out());
        out.println(output);

        clear();
    }

    public Map<String, Set<Sample>> getSampleMap() {
        return sampleMap;
    }
}
