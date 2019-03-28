package io.rathr.audrey.instrumentation.nodes;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.Scope;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.EventContext;
import com.oracle.truffle.api.instrumentation.TruffleInstrument;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.nodes.InvalidAssumptionException;
import com.oracle.truffle.api.utilities.CyclicAssumption;
import io.rathr.audrey.instrumentation.Audrey;
import io.rathr.audrey.instrumentation.InstrumentationContext;
import io.rathr.audrey.storage.Project;
import io.rathr.audrey.storage.Sample;
import io.rathr.audrey.storage.SampleStorage;

import java.util.Arrays;
import java.util.Iterator;

public class RootOnlySamplerNode extends SamplerNode {
    private final CyclicAssumption cyclicEnabledAssumption;

    @CompilerDirectives.CompilationFinal
    private Assumption enabled;

    public RootOnlySamplerNode(final Audrey audrey,
                           final EventContext context,
                           final TruffleInstrument.Env env,
                           final Project project,
                           final SampleStorage storage,
                           final InstrumentationContext instrumentationContext,
                           final boolean samplingEnabled,
                           final Integer samplingStep,
                           final Integer maxExtractions) {

        super(
            audrey,
            context,
            env,
            project,
            storage,
            instrumentationContext,
            samplingEnabled,
            samplingStep,
            maxExtractions
        );

        this.cyclicEnabledAssumption = new CyclicAssumption("Node enabled");
        this.enabled = cyclicEnabledAssumption.getAssumption();
    }

    @Override
    protected void onEnter(final VirtualFrame frame) {
        try {
            enabled.check();

//            if (extractions > maxExtractions) {
//                CompilerDirectives.transferToInterpreterAndInvalidate();
//                replace(new DisabledNode(this));
//                return;
//            }

            handleOnEnter(frame.materialize());
            extractions++;
        } catch (InvalidAssumptionException e) {
            replace(new DisabledNode(this));
        }
    }

    @CompilerDirectives.TruffleBoundary
    private void handleOnEnter(final MaterializedFrame frame) {
        if (audrey.isExtractingSample()) {
            return;
        }

        audrey.setExtractingSample(true);

        if (samplingEnabled && entered % samplingStep != 0) {
            audrey.setExtractingSample(false);
            return;
        }

        entered++;

        final Iterator<Scope> scopeIterator = env.findLocalScopes(instrumentedNode, frame).iterator();
        if (!scopeIterator.hasNext()) {
            return;
        }

        final Scope scope = scopeIterator.next();
        final TruffleObject arguments = (TruffleObject) scope.getArguments();

        try {
            final TruffleObject keys = getKeys(arguments);
            final int keySize = getSize(keys);
            if (keySize == 0) {
                audrey.setExtractingSample(false);
                return;
            }

            for (int index = 0; index < keySize; index++) {
                try {
                    final String identifier = (String) read(keys, index);

                    if (Arrays.stream(IDENTIFIER_BLACKLIST).anyMatch(identifier::contains)) {
                        // Skip iteration because we don't care about these values.
                        continue;
                    }

                    final Object valueObject = read(arguments, identifier);
                    final Object metaObject = getMetaObject(valueObject);

                    final Sample sample = new Sample(
                        identifier,
                        index,
                        getString(valueObject),
                        getString(metaObject),
                        "ARGUMENT",
                        sourceSection,
                        rootNodeId,
                        -1
                    );

                    storage.add(sample);
                } catch (UnknownIdentifierException | UnsupportedMessageException e) {
                    e.printStackTrace();
                }
            }
        } catch (UnsupportedMessageException e) {
            e.printStackTrace();
        } finally {
            audrey.setExtractingSample(false);
        }
    }


    @Override
    protected void onReturnValue(final VirtualFrame frame, final Object result) {
        try {
            enabled.check();

//            if (extractions > maxExtractions) {
//                CompilerDirectives.transferToInterpreterAndInvalidate();
//                replace(new DisabledNode(this));
//                return;
//            }

            handleOnReturn(result);
            extractions++;
        } catch (InvalidAssumptionException e) {
            replace(new DisabledNode(this));
        }
    }

    @CompilerDirectives.TruffleBoundary
    private void handleOnReturn(final Object result) {
        if (audrey.isExtractingSample()) {
            return;
        }

        if (samplingEnabled && entered % samplingStep != 0) {
            return;
        }

        audrey.setExtractingSample(true);

        final Object metaObject = getMetaObject(result);
        final Sample sample = new Sample(
            null,
            0,
            getString(result),
            getString(metaObject),
            "RETURN",
            sourceSection,
            rootNodeId,
            -1
        );

        audrey.setExtractingSample(false);
        storage.add(sample);
    }
}
