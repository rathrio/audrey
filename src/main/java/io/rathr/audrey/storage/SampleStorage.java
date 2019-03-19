package io.rathr.audrey.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.oracle.truffle.api.instrumentation.TruffleInstrument;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

public abstract class SampleStorage {
    private static final Gson GSON;

    static {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Sample.class, new SampleAdapter());
        GSON = gsonBuilder.create();
    }

    public void add(Sample sample) {

    }

    public void clear() {

    }

    public void onDispose(final String dumpFilePath) {
        if (dumpFilePath != null && !dumpFilePath.isEmpty()) {
            writeJson(getSamples(), dumpFilePath);
        }

        clear();
    }

    public Set<Sample> getSamples() {
        return new HashSet<>();
    }

    String toJson(final Sample sample) {
        return GSON.toJson(sample);
    }

    String toJson(final Set<Sample> samples) {
        return GSON.toJson(samples);
    }

    Sample fromJson(final String json) {
        return GSON.fromJson(json, Sample.class);
    }

    void writeJson(final Set<Sample> samples, final String dumpFilePath) {
        try {
            Writer writer = new FileWriter(dumpFilePath);
            writer.write(toJson(samples));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
