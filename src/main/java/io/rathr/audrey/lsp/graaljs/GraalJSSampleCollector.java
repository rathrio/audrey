package io.rathr.audrey.lsp.graaljs;

import com.oracle.js.parser.ir.FunctionNode;
import com.oracle.js.parser.ir.LexicalContext;
import com.oracle.js.parser.ir.Node;
import com.oracle.js.parser.ir.ReturnNode;
import com.oracle.js.parser.ir.visitor.NodeVisitor;
import io.rathr.audrey.storage.Sample;
import io.rathr.audrey.storage.SampleFilter;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static io.rathr.audrey.lsp.AudreyServer.LOG;

public class GraalJSSampleCollector extends NodeVisitor<LexicalContext> {
    private final int column;
    private final int line;
    private final SampleFilter filter;

    /**
     * Is used to lookup the current function when looking at return statements. ReturnNodes don't have parent
     * references.
     */
    private FunctionNode currentFunctionNode;

    /**
     * Whether we actually encountered a relevant node during visiting.
     */
    private boolean foundNode;

    GraalJSSampleCollector(final Set<Sample> samples, final String uri, final int line, final int column) {
        super(new LexicalContext());

        this.line = line;
        this.column = column;
        this.filter = new SampleFilter(samples).source(uri);
        this.foundNode = false;
    }

    @Override
    protected boolean enterDefault(final Node node) {
        return true;
    }

    @Override
    public boolean enterFunctionNode(final FunctionNode functionNode) {
        currentFunctionNode = functionNode;
        if (functionNode.getLineNumber() - 1 != line) {
            return true;
        }

        final String functionName = functionNode.getName();
        if (functionName == null) {
            return true;
        }

        foundNode = true;
        LOG.info("Detected GraalJS FunctionNode: " + functionName);
        filter.rootNodeId(functionName)
            .startLine(functionNode.getLineNumber() - 1)
            .endLine(functionNode.getBody().getLastStatement().getLineNumber());

        return true;
    }

    @Override
    public boolean enterReturnNode(final ReturnNode returnNode) {
        if (returnNode.getLineNumber() - 1 != line) {
            return true;
        }

        final FunctionNode functionNode = currentFunctionNode;
        final String functionName = functionNode.getName();
        if (functionName == null) {
            return true;
        }

        foundNode = true;
        LOG.info("Detected GraalJS ReturnNode: " + functionName);
        filter.rootNodeId(functionName)
            .forReturns()
            .startLine(functionNode.getLineNumber() - 1)
            .endLine(functionNode.getBody().getLastStatement().getLineNumber());

        return true;
    }

    public Set<Sample> getSamples() {
        if (!foundNode) {
            return new HashSet<>();
        }

        return filter.apply().collect(Collectors.toSet());
    }
}
