package io.rathr.audrey.storage;

import com.oracle.truffle.api.instrumentation.TruffleInstrument;

import java.util.Set;

public interface SampleStorage {
    void add(Sample sample);
    void clear();
    void onDispose(final TruffleInstrument.Env env);
}
