package io.rathr.audrey.lsp;

import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.util.concurrent.CompletableFuture;

public class AudreyTextDocumentService implements TextDocumentService {
    @Override
    public CompletableFuture<Hover> hover(final TextDocumentPositionParams position) {
        return null;
    }

    @Override
    public void didOpen(final DidOpenTextDocumentParams params) {

    }

    @Override
    public void didChange(final DidChangeTextDocumentParams params) {

    }

    @Override
    public void didClose(final DidCloseTextDocumentParams params) {

    }

    @Override
    public void didSave(final DidSaveTextDocumentParams params) {

    }
}
