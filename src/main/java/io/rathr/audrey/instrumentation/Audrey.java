package io.rathr.audrey.instrumentation;

import com.oracle.truffle.api.instrumentation.EventBinding;
import com.oracle.truffle.api.instrumentation.SourceFilter;
import com.oracle.truffle.api.instrumentation.SourceSectionFilter;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.instrumentation.TruffleInstrument;
import io.rathr.audrey.instrumentation.nodes.RootSamplerNode;
import io.rathr.audrey.instrumentation.nodes.StatementSamplerNode;
import io.rathr.audrey.storage.InMemorySampleStorage;
import io.rathr.audrey.storage.Project;
import io.rathr.audrey.storage.RedisSampleStorage;
import io.rathr.audrey.storage.SampleStorage;
import org.graalvm.polyglot.Engine;

import java.io.Closeable;

public class Audrey implements Closeable {
    private static final Class STATEMENT_TAG = StandardTags.StatementTag.class;
    private static final Class ROOT_TAG = StandardTags.RootTag.class;

    private final TruffleInstrument.Env env;
    private SourceSectionFilter statementSourceSectionFilter;
    private Project project;
    private SampleStorage storage;
    private String pathFilter;
    private InstrumentationContext instrumentationContext;

    private EventBinding<?> activeRootBinding;
    private EventBinding<?> activeStatementBinding;

    private SourceSectionFilter rootSourceSectionFilter;

    /**
     * Used to prevent infinite recursions in case a language does an allocation during meta
     * object lookup or toString call.
     */
    private final ThreadLocal<Boolean> extractingSample = ThreadLocal.withInitial(() -> false);

    public Audrey(final TruffleInstrument.Env env) {
        this.env = env;
    }

    public static Audrey find(Engine engine) {
        return AudreyInstrument.getAudrey(engine);
    }

    public SampleStorage getStorage() {
        return storage;
    }

    public boolean isExtractingSample() {
        return extractingSample.get();
    }

    public void setExtractingSample(boolean value) {
        extractingSample.set(value);
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
                this,
                context,
                env,
                project,
                storage,
                instrumentationContext
            )
        );

        this.activeStatementBinding = env.getInstrumenter().attachExecutionEventFactory(
            statementSourceSectionFilter,
            context -> new StatementSamplerNode(
                this,
                context,
                env,
                project,
                storage,
                instrumentationContext
            )
        );
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
                break;
            case "none":
                break;
            case "random":
                break;
            case "temporal":
                break;
            default:
                throw new IllegalArgumentException("Unknown sampling_strategies strategy: " + samplingStrategy);
        }

        this.statementSourceSectionFilter = buildSourceSectionFilter(STATEMENT_TAG);
        this.rootSourceSectionFilter = buildSourceSectionFilter(ROOT_TAG);

        this.instrumentationContext = new InstrumentationContext();
    }
}
