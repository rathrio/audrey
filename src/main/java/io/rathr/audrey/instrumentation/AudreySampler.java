package io.rathr.audrey.instrumentation;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.*;

import java.io.Closeable;
import java.io.IOException;

import static com.oracle.truffle.api.CompilerDirectives.*;

public class AudreySampler implements Closeable {
    private final TruffleInstrument.Env env;
    private final SourceSectionFilter sourceSectionFilter;
    private EventBinding<?> activeBinding;

    public AudreySampler(final TruffleInstrument.Env env, final SourceSectionFilter sourceSectionFilter) {
        this.env = env;
        this.sourceSectionFilter = sourceSectionFilter;
    }

    public void enable() {
        this.activeBinding = env.getInstrumenter().attachExecutionEventFactory(
            sourceSectionFilter,
            context -> new SamplerNode(env, context)
        );
    }

    public void disable() {
        if (this.activeBinding == null) {
            return;
        }

        activeBinding.dispose();
        activeBinding = null;
    }

    @Override
    public void close() throws IOException {
    }

    private class SamplerNode extends ExecutionEventNode {
        private final EventContext context;
        private final TruffleInstrument.Env env;

        SamplerNode(final TruffleInstrument.Env env, final EventContext context) {
            this.env = env;
            this.context = context;
        }

        @Override
        protected void onEnter(final VirtualFrame frame) {
            handleOnEnter(frame.materialize());
        }

        @TruffleBoundary
        private void handleOnEnter(final MaterializedFrame frame) {
            System.out.println("I GOT HERE!!");
        }
    }
}
