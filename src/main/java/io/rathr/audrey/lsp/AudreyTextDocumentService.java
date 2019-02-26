package io.rathr.audrey.lsp;

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
import org.mozilla.javascript.IRFactory;
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
    private final Map<String, AstRoot> documents = new HashMap<>();

    @Override
    public CompletableFuture<Hover> hover(final TextDocumentPositionParams position) {
        List<Either<String, MarkedString>> contents = new ArrayList<>();
        contents.add(Either.forLeft("HI FROM AUDREY LSP!!"));

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
            env.setRecordingComments(true);
            final StringReader stringReader = new StringReader(source);
            final IRFactory factory = new IRFactory(env);
            final AstRoot ast = factory.parse(stringReader, uri, 0);

            documents.put(params.getTextDocument().getUri(), ast);
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

        documents.remove(uri);
    }

    @Override
    public void didSave(final DidSaveTextDocumentParams params) {
    }
}
