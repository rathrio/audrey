package io.rathr.audrey.instrumentation.nodes;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.EventContext;
import com.oracle.truffle.api.instrumentation.TruffleInstrument;
import io.rathr.audrey.instrumentation.InstrumentationContext;
import io.rathr.audrey.sampling_strategies.SamplingStrategy;
import io.rathr.audrey.storage.Project;
import io.rathr.audrey.storage.Sample;
import io.rathr.audrey.storage.SampleStorage;

public final class RootSamplerNode extends SamplerNode {
    public RootSamplerNode(final TruffleInstrument.Env env,
                           final EventContext context,
                           final Project project,
                           final SampleStorage storage,
                           final SamplingStrategy samplingStrategy,
                           final InstrumentationContext instrumentationContext) {
        super(context, env, project, storage, samplingStrategy, instrumentationContext);
    }

    @Override
    protected void onEnter(final VirtualFrame frame) {
        if (CompilerDirectives.inInterpreter()) {
            instrumentationContext.setLookingForFirstStatement(true);
        }
    }


    @Override
    protected void onReturnValue(final VirtualFrame frame, final Object result) {
        handleOnReturn(result);
    }

    @CompilerDirectives.TruffleBoundary
    private void handleOnReturn(final Object result) {
        if (extractingSample.get()) {
            return;
        }

        extractingSample.set(true);

        final Object metaObject = getMetaObject(result);
        final Sample sample = new Sample(
            null,
            getString(result),
            getString(metaObject),
            "RETURN",
            sourceSection,
            rootNodeId
        );

        extractingSample.set(false);
        storage.add(sample);
    }
}
