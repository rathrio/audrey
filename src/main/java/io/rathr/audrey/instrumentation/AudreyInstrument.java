package io.rathr.audrey.instrumentation;

import com.oracle.truffle.api.instrumentation.TruffleInstrument;
import com.oracle.truffle.api.instrumentation.TruffleInstrument.Registration;
import org.graalvm.options.OptionDescriptors;

@Registration(id = AudreyInstrument.ID, name = "Audrey", services = {AudreySampler.class})
public final class AudreyInstrument extends TruffleInstrument {

    public static final String ID = "audrey";
    private AudreySampler sampler;

    @Override
    protected void onCreate(TruffleInstrument.Env env) {
        if (!env.getOptions().get(AudreyCLI.ENABLED)) {
            return;
        }

        final String projectId = env.getOptions().get(AudreyCLI.PROJECT);
        if (projectId.isEmpty()) {
            throw new Error("Provide a unique project ID with --Audrey.Project=\"<Project ID>\"");
        }

        final String rootPath = env.getOptions().get(AudreyCLI.ROOT_PATH);
        final String storageType = env.getOptions().get(AudreyCLI.STORAGE).toLowerCase();
        final String samplingStrategy = env.getOptions().get(AudreyCLI.SAMPLE).toLowerCase();
        final String pathFilter = env.getOptions().get(AudreyCLI.FILTER_PATH);

        sampler = new AudreySampler(
            env,
            projectId,
            rootPath,
            storageType,
            samplingStrategy,
            pathFilter
        );

        sampler.enable();
        env.registerService(sampler);
    }

    @Override
    protected OptionDescriptors getOptionDescriptors() {
        return new AudreyCLIOptionDescriptors();
    }
}
