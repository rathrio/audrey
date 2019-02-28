package io.rathr.audrey.lsp.javascript;

import io.rathr.audrey.lsp.AudreyServer;
import io.rathr.audrey.storage.Sample;
import io.rathr.audrey.storage.Search;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.ObjectProperty;

import java.util.Set;
import java.util.stream.Collectors;

public class SampleCollector implements NodeVisitor {
    private final String uri;
    private final int column;
    private final int line;
    private final Search search;

    public SampleCollector(final String uri, final int line, final int column, final Set<Sample> samples) {
        this.uri = uri;
        this.line = line;
        this.column = column;
        this.search = new Search(samples);
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
        final String rootNodeId = left.getString();

        AudreyServer.LOG.info("Root from ObjectProperty: " + rootNodeId);

        return true;
    }

    public Set<Sample> getSamples() {
        return search.search().collect(Collectors.toSet());
    }
}
