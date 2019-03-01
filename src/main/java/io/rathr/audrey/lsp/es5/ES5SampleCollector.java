package io.rathr.audrey.lsp.es5;

import io.rathr.audrey.lsp.AudreyServer;
import io.rathr.audrey.storage.Sample;
import io.rathr.audrey.storage.SampleFilter;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.ObjectProperty;
import org.mozilla.javascript.ast.ReturnStatement;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ES5SampleCollector implements NodeVisitor {
    private final int column;
    private final int line;
    private final SampleFilter search;

    /**
     * Whether we actually encountered a relevant node during visiting.
     */
    private boolean foundNode;

    ES5SampleCollector(final Set<Sample> samples, final String uri, final int line, final int column) {
        this.line = line;
        this.column = column;
        this.search = new SampleFilter(samples).source(uri);
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

        if (node instanceof ReturnStatement) {
            return visit((ReturnStatement) node);
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
        search.rootNodeId(functionName.getIdentifier())
            .startLine(node.getLineno())
            .endLine(node.getEndLineno());

        return true;
    }

    private boolean visit(final ObjectProperty node) {
        final AstNode right = node.getRight();
        if (!(right instanceof FunctionNode)) {
            return true;
        }
        foundNode = true;
        final FunctionNode functionNode = (FunctionNode) right;

        final AstNode left = node.getLeft();
        AudreyServer.LOG.info("Detected ObjectProperty: " + left.getString());
        search.rootNodeId(left.getString())
            .startLine(functionNode.getLineno())
            .endLine(functionNode.getEndLineno());

        return true;
    }

    private boolean visit(final ReturnStatement node) {
        final AstNode parent = node.getParent().getParent();
        if (!(parent instanceof FunctionNode)) {
            return false;
        }

        String rootNodeId;
        final FunctionNode functionNode = (FunctionNode) parent;
        final Name functionName = functionNode.getFunctionName();
        if (functionName == null) {
            final AstNode fParent = functionNode.getParent();
            if (!(fParent instanceof ObjectProperty)) {
                return false;
            }

            ObjectProperty property = (ObjectProperty) fParent;
            rootNodeId = property.getLeft().getString();
        } else {
            rootNodeId = functionName.getIdentifier();
        }

        foundNode = true;
        AudreyServer.LOG.info("Detected ReturnStatement: " + rootNodeId);
        search.forReturns()
            .rootNodeId(rootNodeId)
            .startLine(functionNode.getLineno())
            .endLine(functionNode.getEndLineno());

        return false;
    }

    public Set<Sample> getSamples() {
        if (!foundNode) {
            return new HashSet<>();
        }

        return search.apply().collect(Collectors.toSet());
    }
}
