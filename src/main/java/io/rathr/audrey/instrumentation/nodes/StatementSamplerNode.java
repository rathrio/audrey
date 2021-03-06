package io.rathr.audrey.instrumentation.nodes;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.Scope;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.EventContext;
import com.oracle.truffle.api.instrumentation.TruffleInstrument;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import io.rathr.audrey.instrumentation.Audrey;
import io.rathr.audrey.instrumentation.InstrumentationContext;
import io.rathr.audrey.storage.Project;
import io.rathr.audrey.storage.Sample;
import io.rathr.audrey.storage.SampleStorage;

import java.util.Arrays;
import java.util.Iterator;

public final class StatementSamplerNode extends SamplerNode {
    @CompilerDirectives.CompilationFinal
    FirstStatementState isFirstStatement = FirstStatementState.looking;

    public StatementSamplerNode(final Audrey audrey,
                                final EventContext context,
                                final TruffleInstrument.Env env,
                                final Project project,
                                final SampleStorage storage,
                                final InstrumentationContext instrumentationContext,
                                final boolean samplingEnabled,
                                final int samplingStep,
                                final int maxExtractions) {

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
            if (extractions > maxExtractions) {
                // TODO: Find a way to completely remove this sampler node.
                return;
            }
            handleOnEnter(frame.materialize());
            extractions++;
        }
    }

    @CompilerDirectives.TruffleBoundary
    private void handleOnEnter(final MaterializedFrame frame) {
        if (audrey.isExtractingSample()) {
            return;
        }

        audrey.setExtractingSample(true);
        isFirstStatement = FirstStatementState.isFirst;

        if (samplingEnabled && entered % samplingStep != 0) {
            exit();
            return;
        }

        entered++;

        final Iterator<Scope> scopeIterator = env.findLocalScopes(instrumentedNode, frame).iterator();
        if (!scopeIterator.hasNext()) {
            exit();
            return;
        }

        final Scope scope = scopeIterator.next();

        // NOTE that getVariables will return ALL local variables in this scope, not just the ones that have
        // been defined at this point of execution. I guess they've been extracted in a semantic analysis
        // step beforehand.
        final TruffleObject variables = (TruffleObject) scope.getVariables();
        final int frameId = frame.hashCode();

        try {
            final TruffleObject keys = getKeys(variables);
            final int keySize = getSize(keys);
            if (keySize == 0) {
                exit();
                return;
            }

            for (int index = 0; index < keySize; index++) {
                try {
                    final String identifier = (String) read(keys, index);

                    if (Arrays.stream(IDENTIFIER_BLACKLIST).anyMatch(identifier::contains)) {
                        // Skip iteration because we don't care about these values.
                        continue;
                    }

                    final Object valueObject = read(variables, identifier);
                    final Object metaObject = getMetaObject(valueObject);

                    final Sample sample = new Sample(
                        identifier,
                        index,
                        getString(valueObject),
                        getString(metaObject),
                        "ARGUMENT",
                        sourceSection,
                        rootNodeId,
                        frameId
                    );

                    storage.add(sample);
                } catch (UnknownIdentifierException | UnsupportedMessageException e) {
                    e.printStackTrace();
                }
            }
        } catch (UnsupportedMessageException e) {
            e.printStackTrace();
        } finally {
            exit();
        }
    }

    // Should be called when exiting from the extraction process so that flags are reset.
    private void exit() {
        audrey.setExtractingSample(false);
        // If we just extracted argument samples, let the following event know that we're done with
        // arguments.
        instrumentationContext.setLookingForFirstStatement(false);
    }

    @Override
    public void enable() {

    }

    @Override
    public void disable() {

    }
}
