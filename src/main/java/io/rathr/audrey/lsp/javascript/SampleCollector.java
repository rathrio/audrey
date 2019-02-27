package io.rathr.audrey.lsp.javascript;

import io.rathr.audrey.lsp.AudreyServer;
import io.rathr.audrey.storage.Sample;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.ObjectProperty;

import java.util.HashSet;
import java.util.Set;

public class SampleCollector implements NodeVisitor {
    private final Set<Sample> samples = new HashSet<>();
    private final String uri;
    private final int column;
    private final int line;

    public SampleCollector(final String uri, final int line, final int column) {
        this.uri = uri;
        this.line = line;
        this.column = column;
    }

    @Override
    public boolean visit(final AstNode node) {
        if (node.getLineno() != line) {
            return true;
        }

        // Forgive me Jan, for I have sinned, but there was no other way.
        if (node instanceof FunctionNode) {
            return visit((FunctionNode) node);
        }

        if (node instanceof ObjectProperty) {
            return visit((ObjectProperty) node);
        }

        return true;
    }

    private boolean visit(final FunctionNode node) {
        final Name functionName = node.getFunctionName();
        if (functionName == null) {
            return true;
        }

        final String rootNodeId = functionName.getIdentifier();
        AudreyServer.LOG.info("Root from FunctionNode: " + rootNodeId);

        return true;
    }

    private boolean visit(final ObjectProperty node) {
        final AstNode right = node.getRight();
        if (!(right instanceof FunctionNode)) {
            return true;
        }

        final AstNode left = node.getLeft();
        AudreyServer.LOG.info("Root from ObjectProperty: " + left.getString());

        return true;
    }

    public Set<Sample> getSamples() {
        return samples;
    }
}
