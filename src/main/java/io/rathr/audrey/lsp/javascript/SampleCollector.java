package io.rathr.audrey.lsp.javascript;

import io.rathr.audrey.lsp.AudreyServer;
import io.rathr.audrey.storage.Sample;
import io.rathr.audrey.storage.Search;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.ObjectProperty;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SampleCollector implements NodeVisitor {
    private final int column;
    private final int line;
    private final Search search;

    /**
     * Whether we actually encountered a relevant node during visiting.
     */
    private boolean foundNode;

    public SampleCollector(final String uri, final int line, final int column, final Set<Sample> samples) {
        this.line = line;
        this.column = column;
        this.search = new Search(samples).source(uri);
        this.foundNode = false;
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
        foundNode = true;

        AudreyServer.LOG.info("Detected FunctionNode: " + functionName.getIdentifier());
        search.rootNodeId(functionName.getIdentifier());
        return true;
    }

    private boolean visit(final ObjectProperty node) {
        final AstNode right = node.getRight();
        if (!(right instanceof FunctionNode)) {
            return true;
        }
        foundNode = true;

        final AstNode left = node.getLeft();
        AudreyServer.LOG.info("Detected ObjectProperty: " + left.getString());
        search.rootNodeId(left.getString());
        return true;
    }

    public Set<Sample> getSamples() {
        if (!foundNode) {
            return new HashSet<>();
        }

        return search.search().collect(Collectors.toSet());
    }
}
