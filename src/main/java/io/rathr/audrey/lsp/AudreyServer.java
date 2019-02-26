package io.rathr.audrey.lsp;

import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.util.concurrent.CompletableFuture;

public class AudreyServer implements LanguageServer, TextDocumentService, LanguageClientAware {
    private final TextDocumentService textDocumentService;
    private String workspaceRoot;
    private LanguageClient client;

    public AudreyServer() {
        this.textDocumentService = new AudreyTextDocumentService();
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(final InitializeParams params) {
        workspaceRoot = params.getRootUri();

        final ServerCapabilities capabilities = new ServerCapabilities();
        capabilities.setTextDocumentSync(TextDocumentSyncKind.None);
        capabilities.setHoverProvider(true);

        return CompletableFuture.completedFuture(new InitializeResult(capabilities));
    }

    @Override
    public CompletableFuture<Object> shutdown() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void exit() {

    }

    @Override
    public TextDocumentService getTextDocumentService() {
        return textDocumentService;
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        return null;
    }

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

    @Override
    public void connect(final LanguageClient client) {
        this.client = client;
    }
}
