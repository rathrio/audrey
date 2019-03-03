package io.rathr.audrey.lsp.graaljs;

import com.oracle.js.parser.ir.LexicalContext;
import com.oracle.js.parser.ir.visitor.NodeVisitor;
import io.rathr.audrey.storage.Sample;
import io.rathr.audrey.storage.SampleFilter;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class GraalJSSampleCollector extends NodeVisitor<LexicalContext> {
    private final int column;
    private final int line;
    private final SampleFilter filter;

    /**
     * Whether we actually encountered a relevant node during visiting.
     */
    private boolean foundNode;

    public GraalJSSampleCollector(final Set<Sample> samples, final String uri, final int line, final int column) {
        super(new LexicalContext());
        this.line = line;
        this.column = column;
        this.filter = new SampleFilter(samples).source(uri);
        this.foundNode = false;
    }

    public Set<Sample> getSamples() {
        if (!foundNode) {
            return new HashSet<>();
        }

        return filter.apply().collect(Collectors.toSet());
    }
}
