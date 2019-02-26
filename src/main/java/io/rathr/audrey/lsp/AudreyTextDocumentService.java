package io.rathr.audrey.lsp;

import io.rathr.audrey.lsp.javascript.SampleCollector;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.IRFactory;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AudreyTextDocumentService implements TextDocumentService {
    private static final CompletableFuture<Hover> EMPTY_HOVER =
        CompletableFuture.completedFuture(new Hover(new ArrayList<>()));

    private final Map<String, AstRoot> asts = new HashMap<>();

    @Override
    public CompletableFuture<Hover> hover(final TextDocumentPositionParams position) {
        final String uri = position.getTextDocument().getUri();
        final int line = position.getPosition().getLine();

        final AstNode firstNodeOnLine = findFirstNodeOnLine(line, uri);
        if (firstNodeOnLine == null) {
            return EMPTY_HOVER;
        }

        final SampleCollector sampleCollector = new SampleCollector();
        firstNodeOnLine.visit(sampleCollector);

        List<Either<String, MarkedString>> contents = new ArrayList<>();
        contents.add(Either.forLeft("Node: " + firstNodeOnLine.getClass().getName()));

        return CompletableFuture.completedFuture(new Hover(contents));
    }

    @Override
    public void didOpen(final DidOpenTextDocumentParams params) {
        final String uri = params.getTextDocument().getUri();
        AudreyServer.LOG.info("didOpen " + uri);

        try {
            final String source = new String(Files.readAllBytes(Paths.get(new URI(uri))));

            final CompilerEnvirons env = new CompilerEnvirons();
            env.setRecoverFromErrors(true);
            env.setGenerateDebugInfo(true);
            env.setLanguageVersion(Context.VERSION_ES6);

            final StringReader stringReader = new StringReader(source);
            final IRFactory factory = new IRFactory(env);
            final AstRoot ast = factory.parse(stringReader, uri, 0);

            asts.put(params.getTextDocument().getUri(), ast);
        } catch (IOException | URISyntaxException e) {
            AudreyServer.LOG.severe(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void didChange(final DidChangeTextDocumentParams params) {
    }

    @Override
    public void didClose(final DidCloseTextDocumentParams params) {
        final String uri = params.getTextDocument().getUri();
        AudreyServer.LOG.info("didClose " + uri);

        asts.remove(uri);
    }

    @Override
    public void didSave(final DidSaveTextDocumentParams params) {
    }

    private AstNode findFirstNodeOnLine(final int line, final String uri) {
        final AstRoot ast = asts.get(uri);
        if (ast == null) {
            return null;
        }

        for (final Node node : ast) {
            if (node.getLineno() == line) {
                return (AstNode) node;
            }
        }

        return null;
    }
}
