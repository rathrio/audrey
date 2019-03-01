package io.rathr.audrey.lsp.ruby;

import io.rathr.audrey.storage.Sample;
import io.rathr.audrey.storage.SampleFilter;
import org.jrubyparser.ast.DefnNode;
import org.jrubyparser.ast.DefsNode;
import org.jrubyparser.util.NoopVisitor;

import java.util.HashSet;
import java.util.Set;

public class RubySampleCollector extends NoopVisitor {
    private final int column;
    private final int line;
    private final SampleFilter search;

    /**
     * Whether we actually encountered a relevant node during visiting.
     */
    private boolean foundNode;

    RubySampleCollector(final Set<Sample> samples, final String uri, final int line, final int column) {
        this.line = line;
        this.column = column;
        this.search = new SampleFilter(samples).source(uri);
        this.foundNode = false;
    }

    @Override
    public Object visitDefnNode(final DefnNode iVisited) {
        return super.visitDefnNode(iVisited);
    }

    @Override
    public Object visitDefsNode(final DefsNode iVisited) {
        return super.visitDefsNode(iVisited);
    }

    public Set<Sample> getSamples() {
        if (!foundNode) {
            return new HashSet<>();
        }

        return null;
    }
}
