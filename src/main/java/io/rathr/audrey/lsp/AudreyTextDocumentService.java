package io.rathr.audrey.lsp;

import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AudreyTextDocumentService implements TextDocumentService {
    private final Map<String, TextDocumentItem> documents = new HashMap<>();

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

        documents.put(params.getTextDocument().getUri(), params.getTextDocument());
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
