package io.rathr.audrey;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.*;
import com.oracle.truffle.api.instrumentation.TruffleInstrument.Registration;
import com.oracle.truffle.api.source.SourceSection;

import static com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

@Registration(id = Instrument.ID, services = Object.class)
public final class Instrument extends TruffleInstrument {

    public static final String ID = "audrey";

    private static final String RUBY_MIME_TYPE = "application/x-ruby";

    @Override
    protected void onCreate(Env env) {
        final SourceFilter sourceFilter = SourceFilter.newBuilder().includeInternal(false).build();
        final SourceSectionFilter.Builder builder = SourceSectionFilter.newBuilder();
        final SourceSectionFilter filter = builder.sourceFilter(sourceFilter)
                .tagIs(StandardTags.CallTag.class)
                .mimeTypeIs(RUBY_MIME_TYPE)
                .build();

        Instrumenter instrumenter = env.getInstrumenter();
        instrumenter.attachExecutionEventFactory(filter, context -> new ExecutionEventNode() {
            @Override
            protected void onReturnValue(VirtualFrame frame, Object result) {
                handleOnReturnValue(frame, result);
            }

            @TruffleBoundary
            private void handleOnReturnValue(VirtualFrame frame, Object result) {
                final SourceSection sourceSection = context.getInstrumentedSourceSection();
                System.out.println(sourceSection.getSource().toString());
            }
        });
    }
}
