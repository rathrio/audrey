package io.rathr.audrey.instrumentation;

import com.oracle.truffle.api.instrumentation.TruffleInstrument;

import java.io.Closeable;
import java.io.IOException;

public class AudreySampler implements Closeable {
    private final TruffleInstrument.Env env;
    private boolean enabled = false;

    public AudreySampler(final TruffleInstrument.Env env) {
        this.env = env;
    }

    public void enable() {
        this.enabled = true;
    }

    @Override
    public void close() throws IOException {
    }
}
