package io.rathr.audrey.lsp.ruby;

import io.rathr.audrey.lsp.AudreyServer;
import io.rathr.audrey.storage.Sample;
import io.rathr.audrey.storage.SampleFilter;
import org.jrubyparser.SourcePosition;
import org.jrubyparser.ast.ArgumentNode;
import org.jrubyparser.ast.ClassNode;
import org.jrubyparser.ast.DefnNode;
import org.jrubyparser.ast.DefsNode;
import org.jrubyparser.ast.IScope;
import org.jrubyparser.ast.ModuleNode;
import org.jrubyparser.ast.ReturnNode;
import org.jrubyparser.ast.SClassNode;
import org.jrubyparser.util.NoopVisitor;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

public class RubySampleCollector extends NoopVisitor {
    private final int column;
    private final int line;
    private final SampleFilter filter;
    private final Stack<String> nesting;

    /**
     * Whether we are dealing with singleton methods in a class << self definition.
     */
    private boolean inSingletonClassDef = false;

    /**
     * Whether we actually encountered a relevant node during visiting.
     */
    private boolean foundNode;

    RubySampleCollector(final Set<Sample> samples, final String uri, final int line, final int column) {
        this.line = line;
        this.column = column;
        this.filter = new SampleFilter(samples).source(uri);
        this.foundNode = false;
        this.nesting = new Stack<>();
    }

    @Override
    public Object visitModuleNode(final ModuleNode iVisited) {
        nesting.push(iVisited.getCPath().getLexicalName());
        final Object result = super.visitModuleNode(iVisited);
        nesting.pop();

        return result;
    }

    @Override
    public Object visitClassNode(final ClassNode iVisited) {
        nesting.push(iVisited.getCPath().getLexicalName());
        final Object result = super.visitClassNode(iVisited);
        nesting.pop();

        return result;
    }

    @Override
    public Object visitSClassNode(final SClassNode iVisited) {
        final boolean prev = inSingletonClassDef;
        inSingletonClassDef = true;
        final Object result = super.visitSClassNode(iVisited);
        inSingletonClassDef = prev;

        return result;
    }

    @Override
    public Object visitDefnNode(final DefnNode iVisited) {
        super.visitDefnNode(iVisited);

        if (iVisited.getPosition().getStartLine() != line) {
            return null;
        }

        updateFilter(iVisited);
        return null;
    }

    @Override
    public Object visitDefsNode(final DefsNode iVisited) {
        super.visitDefsNode(iVisited);

        if (iVisited.getPosition().getStartLine() != line) {
            return null;
        }

        updateFilter(iVisited);
        return null;
    }

    @Override
    public Object visitReturnNode(final ReturnNode iVisited) {
        if (iVisited.getPosition().getStartLine() != line) {
            return null;
        }

        filter.forReturns();
        updateFilterForScope(iVisited.getClosestILocalScope());
        return null;
    }

    @Override
    public Object visitArgumentNode(final ArgumentNode iVisited) {
        final SourcePosition position = iVisited.getPosition();
        if (position.getStartLine() != line) {
            return null;
        }

        if (columnWithin(position.getStartOffset(), position.getEndOffset())) {
            filter.forArguments();
            filter.identifier(iVisited.getName());
        }

        return null;
    }

    public Set<Sample> getSamples() {
        if (!foundNode) {
            return new HashSet<>();
        }

        return filter.apply().collect(Collectors.toSet());
    }

    private String currentNesting() {
        if (nesting.isEmpty()) {
            return "Object";
        }

        return String.join("::", nesting);
    }

    private void updateFilter(final DefsNode iVisited) {
        foundNode = true;
        final String methodName = "." + iVisited.getName();
        final String rootNodeId = currentNesting() + methodName;
        AudreyServer.LOG.info("Detected Ruby singleton method def: " + rootNodeId);

        filter.rootNodeId(rootNodeId)
            .startLine(iVisited.getPosition().getStartLine())
            .endLine(iVisited.getPosition().getEndLine());
    }

    private void updateFilterForScope(final IScope definedScope) {
        if (definedScope instanceof DefsNode) {
            updateFilter((DefsNode) definedScope);
        } else if (definedScope instanceof DefnNode) {
            updateFilter((DefnNode) definedScope);
        }
    }

    private void updateFilter(final DefnNode iVisited) {
        foundNode = true;
        final String delimiter = inSingletonClassDef ? "." : "#";
        final String methodName = delimiter + iVisited.getName();
        final String rootNodeId = currentNesting() + methodName;
        AudreyServer.LOG.info("Detected Ruby method def: " + rootNodeId);

        filter.rootNodeId(rootNodeId)
            .startLine(iVisited.getPosition().getStartLine())
            .endLine(iVisited.getPosition().getEndLine());
    }

    private boolean columnWithin(final int startOffset, final int endOffset) {
        // Currently broken for anything other than the first line, because startOffset and endOffset are not
        // relative to the current line, but column is.
        return column >= startOffset && column <= endOffset;
    }
}
