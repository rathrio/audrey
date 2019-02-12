package io.rathr.audrey.instrumentation;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.Scope;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.EventBinding;
import com.oracle.truffle.api.instrumentation.EventContext;
import com.oracle.truffle.api.instrumentation.SourceFilter;
import com.oracle.truffle.api.instrumentation.SourceSectionFilter;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.instrumentation.TruffleInstrument;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import io.rathr.audrey.sampling_strategies.SampleAll;
import io.rathr.audrey.sampling_strategies.SampleNone;
import io.rathr.audrey.sampling_strategies.SampleRandom;
import io.rathr.audrey.sampling_strategies.SamplingStrategy;
import io.rathr.audrey.sampling_strategies.TemporalShardingStrategy;
import io.rathr.audrey.storage.InMemorySampleStorage;
import io.rathr.audrey.storage.Project;
import io.rathr.audrey.storage.RedisSampleStorage;
import io.rathr.audrey.storage.Sample;
import io.rathr.audrey.storage.SampleStorage;
import org.graalvm.polyglot.Engine;

import java.io.Closeable;
import java.util.Arrays;

import static com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

public class Audrey implements Closeable {
    private static final Class STATEMENT_TAG = StandardTags.StatementTag.class;
    private static final Class ROOT_TAG = StandardTags.RootTag.class;

    private final TruffleInstrument.Env env;
    private SourceSectionFilter statementSourceSectionFilter;
    private Project project;
    private SampleStorage storage;
    private SamplingStrategy samplingStrategy;
    private String pathFilter;
    private InstrumentationContext instrumentationContext;

    private EventBinding<?> activeRootBinding;
    private EventBinding<?> activeStatementBinding;

    private SourceSectionFilter rootSourceSectionFilter;

    public Audrey(final TruffleInstrument.Env env) {
        this.env = env;
    }

    public static Audrey find(Engine engine) {
        return AudreyInstrument.getAudrey(engine);
    }

    public SampleStorage getStorage() {
        return storage;
    }

    private SourceSectionFilter buildSourceSectionFilter(final Class tag) {
        final SourceFilter sourceFilter = SourceFilter.newBuilder()
            .includeInternal(false)
            .sourceIs(source -> {
                final String path = source.getName();

                // Internal stuff we don't care about.
                if (path.startsWith("(")) {
                    return false;
                }

                // Internal node stuff we don't care about.
                if (path.startsWith("internal/")) {
                    return false;
                }

                // Reject any non-project absolute paths.
                if (path.startsWith("/") && !project.contains(path)) {
                    return false;
                }

                if (pathFilter.isEmpty()) {
                    return true;
                }

                return path.toLowerCase().contains(pathFilter.toLowerCase());
            })
            .build();

        final SourceSectionFilter.Builder builder = SourceSectionFilter.newBuilder();
        final SourceSectionFilter sourceSectionFilter = builder.sourceFilter(sourceFilter)
            .tagIs(tag)
            .build();

        return sourceSectionFilter;
    }

    public void enable() {
        this.activeRootBinding = env.getInstrumenter().attachExecutionEventFactory(
            rootSourceSectionFilter,
            context -> new RootSamplerNode(
                env,
                context,
                project,
                storage,
                samplingStrategy,
                instrumentationContext
            )
        );

        this.activeStatementBinding = env.getInstrumenter().attachExecutionEventFactory(
            statementSourceSectionFilter,
            context -> new StatementSamplerNode(
                context,
                env,
                project,
                storage,
                samplingStrategy,
                instrumentationContext
            )
        );
    }

    public void disable() {
        if (this.activeRootBinding == null) {
            return;
        }

        activeRootBinding.dispose();
        activeRootBinding = null;
    }

    @Override
    public void close() {
        storage.onDispose(env);
    }

    public void initialize(final String projectId,
                           final String rootPath,
                           final String storageType,
                           final String samplingStrategy,
                           final String pathFilter) {

        this.project = new Project(projectId, rootPath);
        this.pathFilter = pathFilter;

        switch (storageType) {
            case "in_memory":
                storage = new InMemorySampleStorage();
                break;
            case "redis":
                storage = new RedisSampleStorage(project);
                break;
            default:
                throw new IllegalArgumentException("Unknown storage type: " + storageType);
        }

        switch (samplingStrategy) {
            case "all":
                this.samplingStrategy = new SampleAll();
                break;
            case "none":
                this.samplingStrategy = new SampleNone();
                break;
            case "random":
                this.samplingStrategy = new SampleRandom();
                break;
            case "temporal":
                this.samplingStrategy = new TemporalShardingStrategy();
                break;
            default:
                throw new IllegalArgumentException("Unknown sampling_strategies strategy: " + samplingStrategy);
        }

        this.statementSourceSectionFilter = buildSourceSectionFilter(STATEMENT_TAG);
        this.rootSourceSectionFilter = buildSourceSectionFilter(ROOT_TAG);

        this.instrumentationContext = new InstrumentationContext();
    }

    static final class InstrumentationContext {
        // I.e. for our purposes: we are instrumenting the first statement in a root node.
        private boolean lookingForFirstStatement = false;

        public boolean isLookingForFirstStatement() {
            return lookingForFirstStatement;
        }

        public void setLookingForFirstStatement(final boolean flag) {
            this.lookingForFirstStatement = flag;
        }
    }

    enum FirstStatementState {
        looking,
        isFirst,
        isNotFirst
    }
    private static final class StatementSamplerNode extends SamplerNode {

        @CompilerDirectives.CompilationFinal
        FirstStatementState isFirstStatement = FirstStatementState.looking;

        public StatementSamplerNode(final EventContext context, final TruffleInstrument.Env env,
                                    final Project project, final SampleStorage storage,
                                    final SamplingStrategy samplingStrategy,
                                    final InstrumentationContext instrumentationContext) {
            super(context, env, project, storage, samplingStrategy, instrumentationContext);
        }


        @Override
        protected void onEnter(final VirtualFrame frame) {
            if (isFirstStatement == FirstStatementState.looking) {
                if (instrumentationContext.isLookingForFirstStatement()) {
                    isFirstStatement = FirstStatementState.isFirst;
                } else {
                    isFirstStatement = FirstStatementState.isNotFirst;
                }
            }
            if (isFirstStatement == FirstStatementState.isFirst) {
                handleOnEnter(frame.materialize());
            }
        }

        @TruffleBoundary
        private void handleOnEnter(final MaterializedFrame frame) {
            isFirstStatement = FirstStatementState.isFirst;
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

                for (int index = 0; index < keySize; index++) {
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
                            "ARGUMENT",
                            sourceSection,
                            rootNodeId
                        );

                        storage.add(sample);
                    } catch (UnknownIdentifierException | UnsupportedMessageException e) {
                        e.printStackTrace();
                    }
                }
            } catch (UnsupportedMessageException e) {
                e.printStackTrace();
            } finally {
                // If we just extracted argument samples, let the following event know that we're done with
                // arguments.
                instrumentationContext.setLookingForFirstStatement(false);
            }
        }
    }

    // TODO: Rename
    private static final class RootSamplerNode extends SamplerNode {
        RootSamplerNode(final TruffleInstrument.Env env,
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

        @TruffleBoundary
        private void handleOnReturn(final Object result) {
            final Object metaObject = env.findMetaObject(languageInfo, result);

            final Sample sample = new Sample(
                null,
                getString(languageInfo, result),
                getString(languageInfo, metaObject),
                "RETURN",
                sourceSection,
                rootNodeId
            );

            storage.add(sample);
        }
    }
}
