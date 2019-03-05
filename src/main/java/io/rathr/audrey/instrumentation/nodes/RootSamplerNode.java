package io.rathr.audrey.instrumentation.nodes;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.EventContext;
import com.oracle.truffle.api.instrumentation.TruffleInstrument;
import io.rathr.audrey.instrumentation.Audrey;
import io.rathr.audrey.instrumentation.InstrumentationContext;
import io.rathr.audrey.storage.Project;
import io.rathr.audrey.storage.Sample;
import io.rathr.audrey.storage.SampleStorage;

public final class RootSamplerNode extends SamplerNode {
    public RootSamplerNode(final Audrey audrey,
                           final EventContext context,
                           final TruffleInstrument.Env env,
                           final Project project,
                           final SampleStorage storage,
                           final InstrumentationContext instrumentationContext) {

        super(audrey, context, env, project, storage, instrumentationContext);
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
        if (audrey.isExtractingSample()) {
            return;
        }

        audrey.setExtractingSample(true);

        final Object metaObject = getMetaObject(result);
        final Sample sample = new Sample(
            null,
            getString(result),
            getString(metaObject),
            "RETURN",
            sourceSection,
            rootNodeId
        );

        audrey.setExtractingSample(false);
        storage.add(sample);
    }
}
