package io.rathr.audrey;

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.*;
import com.oracle.truffle.api.instrumentation.TruffleInstrument.Registration;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import org.graalvm.options.OptionDescriptors;

import java.util.Arrays;
import java.util.List;

import static com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

@Registration(id = AudreyInstrument.ID, name = "Audrey", services = {Object.class})
public final class AudreyInstrument extends TruffleInstrument {

    public static final String ID = "audrey";

    private static final Class CALL_TAG = StandardTags.CallTag.class;
    private static final Class ROOT_TAG = StandardTags.RootTag.class;

    @Override
    protected void onCreate(TruffleInstrument.Env env) {
        if (!env.getOptions().get(AudreyCLI.ENABLED)) {
            return;
        }

        final SourceFilter sourceFilter = SourceFilter.newBuilder().includeInternal(false).build();
        final SourceSectionFilter.Builder builder = SourceSectionFilter.newBuilder();
        final SourceSectionFilter filter = builder.sourceFilter(sourceFilter)
                .tagIs(ROOT_TAG)
                .build();

        Instrumenter instrumenter = env.getInstrumenter();
        instrumenter.attachExecutionEventFactory(filter, context -> new ExecutionEventNode() {
            @Override
            protected void onEnter(VirtualFrame frame) {
                handleOnEnter(frame);
            }

            @Override
            protected void onReturnValue(VirtualFrame frame, Object result) {
                handleOnReturnValue(frame, result);
            }

            @TruffleBoundary
            private void handleOnEnter(VirtualFrame frame) {
//                final FrameDescriptor descriptor = frame.getFrameDescriptor();
//                final List<? extends FrameSlot> slots = descriptor.getSlots();
//                slots.forEach(slot -> {
//                    Object value = frame.getValue(slot);
//                    System.out.println(slot.getIdentifier());
//                    final String string = getString("ruby", value);
//                    System.out.println(string);
//                });
            }

            @TruffleBoundary
            private void handleOnReturnValue(VirtualFrame frame, Object result) {
                final FrameDescriptor descriptor = frame.getFrameDescriptor();
//                final List<? extends FrameSlot> slots = descriptor.getSlots();
//                slots.forEach(slot -> {
//                    System.out.println("slot name: " + slot.getIdentifier());
//
//                    final Object value = frame.getValue(slot);
//                    final String string = getString("ruby", value);
//                    System.out.println("slot value: " + string + "\n");
//                });

                final SourceSection sourceSection = context.getInstrumentedSourceSection();
                final String languageId = sourceSection.getSource().getLanguage();
                final Source source = sourceSection.getSource();

                System.out.println("source: " + source.getName());
                System.out.println("internal: " + source.isInternal());

                if (descriptor.getSize() > 0) {
                    final Object[] arguments = frame.getArguments();
                    Arrays.asList(arguments).forEach(arg -> {
                        final String string = getString(languageId, arg);
                        System.out.println("argument: " + string);
                    });
                    System.out.println("\n");
                }

                if (result != null) {
                    final String string = getString(languageId, result);
                    System.out.println("return: " + string + "\n");
                }
            }


            /**
             * @return guest language string representation of object.
             */
            @TruffleBoundary
            private String getString(String languageId, Object object) {
                if (isSimple(object)) {
                    return object.toString();
                }

                return env.toString(env.getLanguages().get(languageId), object);
            }

            private boolean isSimple(Object object) {
                return object instanceof String
                    || object instanceof Integer
                    || object instanceof Double
                    || object instanceof Boolean;
            }
        });
    }

    @Override
    protected OptionDescriptors getOptionDescriptors() {
        return new AudreyCLIOptionDescriptors();
    }
}
