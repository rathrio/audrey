package io.rathr.audrey;

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.*;
import com.oracle.truffle.api.instrumentation.TruffleInstrument.Registration;
import org.graalvm.options.OptionDescriptors;

import java.util.Arrays;
import java.util.List;

import static com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

@Registration(id = AudreyInstrument.ID, name = "Audrey", services = {Object.class})
public final class AudreyInstrument extends TruffleInstrument {

    public static final String ID = "audrey";

    private static final String RUBY_MIME_TYPE = "application/x-ruby";

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
            protected void onInputValue(VirtualFrame frame, EventContext inputContext, int inputIndex, Object inputValue) {
                handleOnInputValue(frame, inputContext, inputIndex, inputValue);
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
//                final FrameDescriptor descriptor = frame.getFrameDescriptor();
//                final List<? extends FrameSlot> slots = descriptor.getSlots();
//                slots.forEach(slot -> {
//                    System.out.println("slot name: " + slot.getIdentifier());
//
//                    final Object value = frame.getValue(slot);
//                    final String string = getString("ruby", value);
//                    System.out.println("slot value: " + string + "\n");
//                });



                final Object[] arguments = frame.getArguments();
                Arrays.asList(arguments).forEach(arg -> {
                    final String string = getString("ruby", arg);
                    System.out.println("arg " + string + "\n");
                });


//                if (arguments.length == 0) {
//                    return;
//                }

//                final SourceSection sourceSection = context.getInstrumentedSourceSection();
//                final String languageId = sourceSection.getSource().getLanguage();
//
//                System.out.println(sourceSection + "\n");
//                System.out.println("Returned: " + getString(languageId, result) + "\n\n");
//                System.out.println("First arg: " + firstArg + "\n\n");
            }

            @TruffleBoundary
            private void handleOnInputValue(VirtualFrame frame, EventContext inputContext, int inputIndex, Object inputValue) {
                System.out.println("GOT HERE");
//                if (inputValue == null) {
//                    return;
//                }
//
//                System.out.println(getString("ruby", inputValue));
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
