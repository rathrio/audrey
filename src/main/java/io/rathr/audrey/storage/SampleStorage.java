package io.rathr.audrey.storage;

import com.oracle.truffle.api.instrumentation.TruffleInstrument;

public interface SampleStorage {
    void add(Sample sample);
    void clear();
    void onDispose(final TruffleInstrument.Env env);
}
