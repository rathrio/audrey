package io.rathr.audrey.instrumentation;

import com.oracle.truffle.api.instrumentation.TruffleInstrument;
import com.oracle.truffle.api.instrumentation.TruffleInstrument.Registration;
import org.graalvm.options.OptionDescriptors;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Instrument;

@Registration(id = AudreyInstrument.ID, name = "Audrey", services = {Audrey.class})
public final class AudreyInstrument extends TruffleInstrument {

    public static final String ID = "audrey";
    private Audrey audrey;

    public static Audrey getAudrey(Engine engine) {
        Instrument instrument = engine.getInstruments().get(ID);
        if (instrument == null) {
            throw new IllegalStateException("Audrey is not installed.");
        }

        final Audrey audrey = instrument.lookup(Audrey.class);
        return audrey;
    }

    @Override
    protected void onCreate(TruffleInstrument.Env env) {
        audrey = new Audrey(env);

        if (env.getOptions().get(AudreyCLI.ENABLED)) {
            String projectId = env.getOptions().get(AudreyCLI.PROJECT);
            if (projectId == null || projectId.isEmpty()) {
                projectId = Audrey.DEFAULT_PROJECT_ID;
            }

            final String rootPath = env.getOptions().get(AudreyCLI.ROOT_PATH);
            final String storageType = env.getOptions().get(AudreyCLI.STORAGE).toLowerCase();
            final String pathFilter = env.getOptions().get(AudreyCLI.FILTER_PATH);
            final boolean samplingEnabled = env.getOptions().get(AudreyCLI.SAMPLING_ENABLED);
            final Integer samplingRate = env.getOptions().get(AudreyCLI.SAMPLING_RATE);
            final Integer maxExtractions = env.getOptions().get(AudreyCLI.MAX_EXTRACTIONS);

            audrey.initialize(
                projectId,
                rootPath,
                storageType,
                pathFilter,
                samplingEnabled,
                samplingRate,
                maxExtractions
            );

            audrey.enable();
        }

        env.registerService(audrey);
    }

    @Override
    protected void onDispose(final Env env) {
        if (!env.getOptions().get(AudreyCLI.ENABLED)) {
            return;
        }

        audrey.close();
    }

    @Override
    protected OptionDescriptors getOptionDescriptors() {
        return new AudreyCLIOptionDescriptors();
    }
}
