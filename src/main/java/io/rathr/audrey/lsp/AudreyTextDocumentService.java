package io.rathr.audrey.lsp;

import io.rathr.audrey.lsp.javascript.SampleCollector;
import io.rathr.audrey.storage.Sample;
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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class AudreyTextDocumentService implements TextDocumentService {
    private static final CompletableFuture<Hover> EMPTY_HOVER =
        CompletableFuture.completedFuture(new Hover(new ArrayList<>()));

    private final Map<String, AstRoot> asts = new HashMap<>();

    @Override
    public CompletableFuture<Hover> hover(final TextDocumentPositionParams position) {
        final String uri = position.getTextDocument().getUri();
        final int line = position.getPosition().getLine();
        final int column = position.getPosition().getCharacter();

        final AstRoot ast = asts.get(uri);
        final SampleCollector sampleCollector = new SampleCollector(uri, line, column);
        ast.visit(sampleCollector);
        final Set<Sample> samples = sampleCollector.getSamples();
        if (samples.isEmpty()) {
            return EMPTY_HOVER;
        }

        return EMPTY_HOVER;
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
}
