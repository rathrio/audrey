package io.rathr.audrey.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.oracle.truffle.api.instrumentation.TruffleInstrument;

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

    public void onDispose(final TruffleInstrument.Env env) {

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
}
