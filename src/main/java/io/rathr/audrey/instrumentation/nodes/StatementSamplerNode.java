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
import io.rathr.audrey.instrumentation.InstrumentationContext;
import io.rathr.audrey.sampling_strategies.SamplingStrategy;
import io.rathr.audrey.storage.Project;
import io.rathr.audrey.storage.Sample;
import io.rathr.audrey.storage.SampleStorage;

import java.util.Arrays;

public final class StatementSamplerNode extends SamplerNode {
    /**
     * Used to prevent infinite recursions in case a language does an allocation during meta
     * object lookup or toString call.
     */
    ThreadLocal<Boolean> extractingSample = ThreadLocal.withInitial(() -> false);

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

    @CompilerDirectives.TruffleBoundary
    private void handleOnEnter(final MaterializedFrame frame) {
        if (extractingSample.get()) {
            return;
        }

        extractingSample.set(true);

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
                    final Object metaObject = getMetaObject(valueObject);

                    final Sample sample = new Sample(
                        identifier,
                        getString(valueObject),
                        getString(metaObject),
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
            extractingSample.set(false);
            // If we just extracted argument samples, let the following event know that we're done with
            // arguments.
            instrumentationContext.setLookingForFirstStatement(false);
        }
    }
}
