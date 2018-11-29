package io.rathr.audrey;

import com.oracle.truffle.api.Scope;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.*;
import com.oracle.truffle.api.instrumentation.TruffleInstrument.Registration;
import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.Message;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.nodes.LanguageInfo;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.SourceSection;
import org.graalvm.options.OptionDescriptors;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

@Registration(id = AudreyInstrument.ID, name = "Audrey")
public final class AudreyInstrument extends TruffleInstrument {

    public static final String ID = "audrey";
    private static final Class CALL_TAG = StandardTags.CallTag.class;
    private static final Class STATEMENT_TAG = StandardTags.StatementTag.class;
    private static final Class ROOT_TAG = StandardTags.RootTag.class;

    private Map<SourceSection, Set<Sample>> sampleMap = new ConcurrentHashMap<>();

    private static String extractRootName(final Node instrumentedNode) {
        RootNode rootNode = instrumentedNode.getRootNode();
        if (rootNode != null) {
            if (rootNode.getName() == null) {
                return rootNode.toString();
            } else {
                return rootNode.getName();
            }
        } else {
            return "<Unknown>";
        }
    }

    class InstrumentationContext {
        private boolean enteringRoot = false;

        public boolean isEnteringRoot() {
            return enteringRoot;
        }

        public void setEnteringRoot(final boolean enteringRoot) {
            this.enteringRoot = enteringRoot;
        }
    }

    @Override
    protected void onCreate(TruffleInstrument.Env env) {
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            sampleMap.forEach((sourceSection, samples) -> {
//                System.out.println(sourceSection + ":");
//                samples.forEach(sample -> System.out.print(sample.value + ", "));
//                System.out.println();
//            });
//        }));

        if (!env.getOptions().get(AudreyCLI.ENABLED)) {
            return;
        }

        final SourceFilter sourceFilter = SourceFilter.newBuilder()
            .includeInternal(false)
            .sourceIs(source -> {
                final String pathFilter = env.getOptions().get(AudreyCLI.FILTER_PATH);
                if (pathFilter.isEmpty()) {
                    return true;
                }

                return source.getName().toLowerCase().contains(pathFilter.toLowerCase());
            })
            .build();

        final SourceSectionFilter.Builder builder = SourceSectionFilter.newBuilder();
        final SourceSectionFilter statementFilter = builder.sourceFilter(sourceFilter)
            .tagIs(STATEMENT_TAG)
            .build();

        final SourceSectionFilter rootFilter = builder.sourceFilter(sourceFilter)
            .tagIs(ROOT_TAG)
            .build();

        Instrumenter instrumenter = env.getInstrumenter();
        final InstrumentationContext instrumentationContext = new InstrumentationContext();

        instrumenter.attachExecutionEventFactory(rootFilter, context -> new ExecutionEventNode() {
            @Override
            protected void onEnter(final VirtualFrame frame) {
                handleOnEnter();
            }

            @TruffleBoundary
            private void handleOnEnter() {
                instrumentationContext.setEnteringRoot(true);
            }
        });

        instrumenter.attachExecutionEventFactory(statementFilter, context -> new ExecutionEventNode() {
            @Override
            protected void onEnter(final VirtualFrame frame) {
                handleOnEnter(frame);
            }


            @TruffleBoundary
            private void handleOnEnter(final VirtualFrame frame) {
                final FrameDescriptor descriptor = frame.getFrameDescriptor();
                final Node instrumentedNode = context.getInstrumentedNode();

                final SourceSection sourceSection = context.getInstrumentedSourceSection();
                final String languageId = sourceSection.getSource().getLanguage();

                final Scope scope = env.findLocalScopes(instrumentedNode, frame).iterator().next();
                final Object variables = scope.getVariables();

                try {
                    final TruffleObject keys = ForeignAccess.sendKeys(
                        Message.KEYS.createNode(),
                        (TruffleObject) variables
                    );

                    System.out.println("hi");

                } catch (UnsupportedMessageException e) {
                    e.printStackTrace();
                }


//                if (descriptor.getSize() > 0) {
//
//                    System.out.println("Root node: " + extractRootName(instrumentedNode));
//
//                    Iterable<Scope> scopes = env.findLocalScopes(instrumentedNode, frame);
//
//                    for (Scope scope : scopes) {
//                        final Object variables = scope.getVariables();
//                        System.out.println("slkjfdslkjf");
//                    }
//
//                    System.out.println("Source section: " + sourceSection);
//
//                    final List<? extends FrameSlot> slots = descriptor.getSlots();
//                    slots.forEach(slot -> {
//                        System.out.println("slot name: " + slot.getIdentifier());
//
//                        final Object value = frame.getValue(slot);
//                        final String string = getString(languageId, value);
//                        System.out.println("slot value: " + string);
//                    });
//
//                    final Object[] arguments = frame.getArguments();
//                    Arrays.asList(arguments).forEach(arg -> {
//                        final String string = getString(languageId, arg);
//                        System.out.println("arg: " + string);
//                    });
//
//
//                    System.out.println("\n");
//                }
            }

//            @Override
//            protected void onReturnValue(VirtualFrame frame, Object result) {
//                handleOnReturnValue(frame, result);
//            }
//
//            @TruffleBoundary
//            private void handleOnReturnValue(VirtualFrame frame, Object result) {
//                if (result == null) {
//                    return;
//                }
//
//                final SourceSection sourceSection = context.getInstrumentedSourceSection();
//                final String languageId = sourceSection.getSource().getLanguage();
//
//                final String value = getString(languageId, result);
//                final Object metaObject = env.findMetaObject(getLanguageInfo(languageId), result);
//
//                final Node instrumentedNode = context.getInstrumentedNode();
//
//                final Sample sample = new Sample(
//                    value,
//                    getString(languageId, metaObject),
//                    "return",
//                    extractRootName(instrumentedNode)
//                );
//
//                sampleMap.computeIfAbsent(sourceSection, section -> ConcurrentHashMap.newKeySet());
//                sampleMap.get(sourceSection).add(sample);
//            }

            /**
             * @return guest language string representation of object.
             */
            private String getString(String languageId, Object object) {
                if (isSimple(object)) {
                    return object.toString();
                }

                return env.toString(getLanguageInfo(languageId), object);
            }

            private LanguageInfo getLanguageInfo(String languageId) {
                return env.getLanguages().get(languageId);
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
