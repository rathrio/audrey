package io.rathr.audrey.instrumentation.nodes;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.ExecutionEventNode;
import com.oracle.truffle.api.nodes.InvalidAssumptionException;
import com.oracle.truffle.api.utilities.CyclicAssumption;

class DisabledNode extends ExecutionEventNode {
    /**
     * The node that was disabled (replaced) by this node.
     */
    private final SamplerNode node;

    private final CyclicAssumption cyclicDisabledAssumption;

    @CompilerDirectives.CompilationFinal
    private Assumption disabled;

    DisabledNode(SamplerNode node) {
        this.node = node;
        this.cyclicDisabledAssumption = new CyclicAssumption("Node disabled");
        this.disabled = cyclicDisabledAssumption.getAssumption();
    }

    @Override
    protected void onEnter(final VirtualFrame frame) {
        try {
            disabled.check();
        } catch (InvalidAssumptionException e) {
            replace(node);
        }
    }

    public void enable() {
        disabled = cyclicDisabledAssumption.getAssumption();
    }

    // TODO: find a better name. This reenables the actual sampler node by disabling this node.
    public void disable() {
        cyclicDisabledAssumption.invalidate();
    }
}
