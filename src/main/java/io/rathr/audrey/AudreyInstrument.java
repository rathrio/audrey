package io.rathr.audrey;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.*;
import com.oracle.truffle.api.instrumentation.TruffleInstrument.Registration;
import com.oracle.truffle.api.source.SourceSection;
import org.graalvm.options.OptionDescriptors;

import static com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

@Registration(id = AudreyInstrument.ID, name = "Audrey", services = {Object.class})
public final class AudreyInstrument extends TruffleInstrument {

    public static final String ID = "audrey";
    private static final String RUBY_MIME_TYPE = "application/x-ruby";

    @Override
    protected void onCreate(Env env) {
        if (!env.getOptions().get(AudreyCLI.ENABLED)) {
            return;
        }

        final SourceFilter sourceFilter = SourceFilter.newBuilder().includeInternal(false).build();
        final SourceSectionFilter.Builder builder = SourceSectionFilter.newBuilder();
        final SourceSectionFilter filter = builder.sourceFilter(sourceFilter)
                .tagIs(StandardTags.CallTag.class)
                .build();

        Instrumenter instrumenter = env.getInstrumenter();
        instrumenter.attachExecutionEventFactory(filter, context -> new ExecutionEventNode() {
            @Override
            protected void onReturnValue(VirtualFrame frame, Object result) {
                handleOnReturnValue(frame, result);
            }

            @TruffleBoundary
            private void handleOnReturnValue(VirtualFrame frame, Object result) {
                if (result == null) {
                    return;
                }

                final boolean isSimple = (
                    result instanceof String
                        || result instanceof Integer
                        || result instanceof Double
                        || result instanceof Boolean
                );

                final String string = isSimple
                    ? result.toString()
                    : env.toString(env.findLanguage(result), result);

//                final SourceSection sourceSection = context.getInstrumentedSourceSection();
                System.out.println(string);
            }
        });
    }

    @Override
    protected OptionDescriptors getOptionDescriptors() {
        return new AudreyCLIOptionDescriptors();
    }
}
