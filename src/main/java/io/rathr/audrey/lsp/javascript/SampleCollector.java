package io.rathr.audrey.lsp.javascript;

import io.rathr.audrey.lsp.AudreyServer;
import io.rathr.audrey.storage.Sample;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;

import java.util.HashSet;
import java.util.Set;

public class SampleCollector implements NodeVisitor {
    private final Set<Sample> samples = new HashSet<>();

    @Override
    public boolean visit(final AstNode node) {
        if (node instanceof FunctionNode) {
            visit((FunctionNode) node);
        }

        return true;
    }

    public boolean visit(final FunctionNode node) {
        final Name functionName = node.getFunctionName();
        if (functionName == null) {
            return false;
        }

        final String rootNodeId = functionName.getIdentifier();
        AudreyServer.LOG.info("Detected root: " + rootNodeId);
        return false;
    }

    public Set<Sample> getSamples() {
        return samples;
    }
}
