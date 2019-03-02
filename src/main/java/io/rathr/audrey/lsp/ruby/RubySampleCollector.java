package io.rathr.audrey.lsp.ruby;

import io.rathr.audrey.lsp.AudreyServer;
import io.rathr.audrey.storage.Sample;
import io.rathr.audrey.storage.SampleFilter;
import org.jrubyparser.ast.ClassNode;
import org.jrubyparser.ast.DefnNode;
import org.jrubyparser.ast.DefsNode;
import org.jrubyparser.ast.ModuleNode;
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
    public Object visitDefnNode(final DefnNode iVisited) {
        if (iVisited.getPosition().getStartLine() != line) {
            return super.visitDefnNode(iVisited);
        }

        foundNode = true;
        final String methodName = "#" + iVisited.getName();
        final String rootNodeId = currentNesting() + methodName;
        AudreyServer.LOG.info("Detected Ruby instance method def: " + rootNodeId);

        filter.rootNodeId(rootNodeId)
            .startLine(iVisited.getPosition().getStartLine())
            .endLine(iVisited.getPosition().getEndLine());

        return null;
    }

    @Override
    public Object visitDefsNode(final DefsNode iVisited) {
        if (iVisited.getPosition().getStartLine() != line) {
            return super.visitDefsNode(iVisited);
        }

        foundNode = true;
        final String methodName = "." + iVisited.getName();
        final String rootNodeId = currentNesting() + methodName;
        AudreyServer.LOG.info("Detected Ruby singleton method def: " + rootNodeId);

        filter.rootNodeId(rootNodeId)
            .startLine(iVisited.getPosition().getStartLine())
            .endLine(iVisited.getPosition().getEndLine());

        return null;
    }

    public Set<Sample> getSamples() {
        if (!foundNode) {
            return new HashSet<>();
        }

        return filter.apply().collect(Collectors.toSet());
    }

    private String currentNesting() {
        return String.join("::", nesting);
    }
}
