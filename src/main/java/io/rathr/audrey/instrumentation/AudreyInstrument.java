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

    private static final String[] IDENTIFIER_BLACKLIST = {"(self)"};

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

    @Override
    protected void onCreate(TruffleInstrument.Env env) {
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            storage.toString();
//            System.out.println("HEY MA LOOK");
//        }));

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

//        // Filter for language agnostic "root" sections. We're only interested in "callable" constructs though,
//        // e.g. methods in Ruby or functions in JS.
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
////                final Node instrumentedNode = context.getInstrumentedNode();
//                System.out.println("got here");
//            }
//        });

        // Filter for language agnostic statement source sections.
        final SourceSectionFilter statementFilter = builder.sourceFilter(sourceFilter)
            .tagIs(STATEMENT_TAG)
            .build();

        instrumenter.attachExecutionEventFactory(statementFilter, context -> new ExecutionEventNode() {
            @Override
            protected void onEnter(final VirtualFrame frame) {
                handleOnEnter(frame);
            }

            @TruffleBoundary
            private void handleOnEnter(final VirtualFrame frame) {
                final Node instrumentedNode = context.getInstrumentedNode();

                final SourceSection sourceSection = context.getInstrumentedSourceSection();
                final String languageId = sourceSection.getSource().getLanguage();

                final Scope scope = env.findLocalScopes(instrumentedNode, frame).iterator().next();
                final TruffleObject variables = (TruffleObject) scope.getVariables();

                try {
                    final TruffleObject keys = getKeys(variables);
                    final int keySize = getSize(keys);
                    if (keySize == 0) {
                        return;
                    }

                    IntStream.range(0, keySize).forEach(index -> {
                        try {
                            final String identifier = (String) read(keys, index);

                            if (Arrays.asList(IDENTIFIER_BLACKLIST).contains(identifier)) {
                                // Skip iteration because we don't care about these values.
                                return;
                            }

                            final Object valueObject = read(variables, identifier);
                            final Object metaObject = env.findMetaObject(getLanguageInfo(languageId), valueObject);

                            final Sample sample = new Sample(
                                identifier,
                                getString(languageId, valueObject),
                                getString(languageId, metaObject),
                                "STATEMENT",
                                sourceSection,
                                extractRootName(instrumentedNode)
                            );

                            storage.add(sample);
                        } catch (UnknownIdentifierException | UnsupportedMessageException e) {
                            e.printStackTrace();
                        }
                    });
                } catch (UnsupportedMessageException e) {
                    e.printStackTrace();
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
            private String getString(String languageId, Object object) {
                if (isSimple(object)) {
                    return object.toString();
                }

                return env.toString(getLanguageInfo(languageId), object);
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
