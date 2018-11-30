package io.rathr.audrey.instrumentation;

import com.oracle.truffle.api.Scope;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.*;
import com.oracle.truffle.api.instrumentation.TruffleInstrument.Registration;
import com.oracle.truffle.api.interop.*;
import com.oracle.truffle.api.nodes.LanguageInfo;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.SourceSection;
import io.rathr.audrey.storage.InMemorySampleStorage;
import io.rathr.audrey.storage.RedisSampleStorage;
import io.rathr.audrey.storage.Sample;
import io.rathr.audrey.storage.SampleStorage;
import org.graalvm.options.OptionDescriptors;

import java.util.Arrays;
import java.util.stream.IntStream;

import static com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

@Registration(id = AudreyInstrument.ID, name = "Audrey")
public final class AudreyInstrument extends TruffleInstrument {

    public static final String ID = "audrey";
    private static final Class STATEMENT_TAG = StandardTags.StatementTag.class;
    private static final Class ROOT_TAG = StandardTags.RootTag.class;

    private static final Node READ_NODE = Message.READ.createNode();
    private static final Node KEYS_NODE = Message.KEYS.createNode();

    private SampleStorage storage;

    private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();

    private static final String[] IDENTIFIER_BLACKLIST = {"(self)", "rubytruffle_temp"};

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

    private final class InstrumentationContext {
        private String rootNodeId = "<Unknown>";
        private boolean enteringRoot = false;

        public String getRootNodeId() {
            return rootNodeId;
        }

        public boolean isEnteringRoot() {
            return enteringRoot;
        }

        public void enter(final String rootNodeId) {
            this.rootNodeId = rootNodeId;
            this.enteringRoot = true;
        }

        public void setEnteringRoot(final boolean flag) {
            this.enteringRoot = flag;
        }
    }

    @Override
    protected void onCreate(TruffleInstrument.Env env) {
        if (!env.getOptions().get(AudreyCLI.ENABLED)) {
            return;
        }

        final String storageType = env.getOptions().get(AudreyCLI.STORAGE).toLowerCase();
        switch (storageType) {
            case "in_memory":
                storage = new InMemorySampleStorage();
                break;
            case "redis":
                storage = new RedisSampleStorage();
                break;
            default:
                throw new IllegalArgumentException("Unknown storage type: " + storageType);
        }

        final InstrumentationContext instrumentationContext = new InstrumentationContext();

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

        final Instrumenter instrumenter = env.getInstrumenter();
        final SourceSectionFilter.Builder builder = SourceSectionFilter.newBuilder();

        // Filter for language agnostic "root" sections. We're only interested in "callable" constructs though,
        // e.g. methods in Ruby or functions in JS.
//        final SourceSectionFilter rootFilter = builder.sourceFilter(sourceFilter)
//            .tagIs(ROOT_TAG)
//            .build();
//
//        instrumenter.attachExecutionEventFactory(rootFilter, context -> new ExecutionEventNode() {
//            @Override
//            protected void onEnter(final VirtualFrame frame) {
//                handleOnEnter();
//            }
//
//            @TruffleBoundary
//            private void handleOnEnter() {
//                final Node instrumentedNode = context.getInstrumentedNode();
//                final String rootNodeId = extractRootName(instrumentedNode);
//                instrumentationContext.enter(rootNodeId);
//            }
//        });

        // Filter for language agnostic statement source sections.
        final SourceSectionFilter statementFilter = builder.sourceFilter(sourceFilter)
            .tagIs(STATEMENT_TAG, ROOT_TAG)
            .build();

        instrumenter.attachExecutionEventFactory(statementFilter, context -> new ExecutionEventNode() {
            @Override
            protected void onEnter(final VirtualFrame frame) {
                handleOnEnter(frame);
            }

            @TruffleBoundary
            private void handleOnEnter(final VirtualFrame frame) {
                final Node instrumentedNode = context.getInstrumentedNode();

                // If we're entering a root node, let the instrumentation context know, so that the samples
                // extracted from the following statement in the root body can be marked as argument samples.
                if (context.hasTag(ROOT_TAG)) {
                    final String rootNodeId = extractRootName(instrumentedNode);
                    instrumentationContext.enter(rootNodeId);
                    return;
                }

                String _sampleCategory = "STATEMENT";
                if (context.hasTag(STATEMENT_TAG) && instrumentationContext.isEnteringRoot()) {
                    _sampleCategory = "ARGUMENT";
                }
                final String sampleCategory = _sampleCategory;

                // TODO: Introduce CLI flag for this.
                // Ensures that we only extract variables from the first statement in a root node (i.e. arguments).
                if (!sampleCategory.equals("ARGUMENT")) {
                    return;
                }

                final SourceSection sourceSection = context.getInstrumentedSourceSection();
                final String languageId = sourceSection.getSource().getLanguage();
                final LanguageInfo languageInfo = getLanguageInfo(languageId);

                final Scope scope = env.findLocalScopes(instrumentedNode, frame).iterator().next();

                // NOTE that getVariables will return ALL local variables in this scope, not just the ones that have
                // been defined at this point of execution. I guess they've been extracted in a semantic analysis
                // step beforehand.
                final TruffleObject variables = (TruffleObject) scope.getVariables();

                try {
                    final TruffleObject keys = getKeys((TruffleObject) scope.getVariables());
                    final int keySize = getSize(keys);
                    if (keySize == 0) {
                        return;
                    }

                    IntStream.range(0, keySize).forEach(index -> {
                        try {
                            final String identifier = (String) read(keys, index);

                            if (Arrays.stream(IDENTIFIER_BLACKLIST).anyMatch(identifier::contains)) {
                                // Skip iteration because we don't care about these values.
                                return;
                            }

                            final Object valueObject = read(variables, identifier);
                            final Object metaObject = env.findMetaObject(languageInfo, valueObject);

                            final Sample sample = new Sample(
                                identifier,
                                getString(languageInfo, valueObject),
                                getString(languageInfo, metaObject),
                                sampleCategory,
                                sourceSection,
                                instrumentationContext.getRootNodeId()
                            );

                            storage.add(sample);
                        } catch (UnknownIdentifierException | UnsupportedMessageException e) {
                            e.printStackTrace();
                        }
                    });
                } catch (UnsupportedMessageException e) {
                    e.printStackTrace();
                } finally {
                    // If we just extracted argument samples, let the following event know that we're done with
                    // arguments.
                    if (sampleCategory.equals("ARGUMENT")) {
                        instrumentationContext.setEnteringRoot(false);
                    }
                }
            }

            private int getSize(final TruffleObject keys) throws UnsupportedMessageException {
                return ((Number) ForeignAccess.sendGetSize(Message.GET_SIZE.createNode(), keys)).intValue();
            }

            private TruffleObject getKeys(final TruffleObject variables) throws UnsupportedMessageException {
                return ForeignAccess.sendKeys(KEYS_NODE, variables);
            }

            private Object read(final TruffleObject object, final Object identifier) throws UnsupportedMessageException, UnknownIdentifierException {
                return ForeignAccess.sendRead(READ_NODE, object, identifier);
            }

            private LanguageInfo getLanguageInfo(String languageId) {
                return env.getLanguages().get(languageId);
            }

            /**
             * @return guest language string representation of object.
             */
            private String getString(LanguageInfo languageInfo, Object object) {
                if (isSimple(object)) {
                    return object.toString();
                }

                return env.toString(languageInfo, object);
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
