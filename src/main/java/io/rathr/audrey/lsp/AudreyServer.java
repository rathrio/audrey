package io.rathr.audrey.lsp;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class AudreyServer implements LanguageServer, LanguageClientAware {
    static {
        System.setProperty(
            "java.util.logging.SimpleFormatter.format",
            "[%1$tF %1$tT] [AUDREY] [%4$-7s] %5$s %n"
        );
    }

    public static final Logger LOG = Logger.getLogger(AudreyServer.class.getName());

    final static int DEFAULT_PORT = 8123;

    private final TextDocumentService textDocumentService;
    private String workspaceRoot;
    private LanguageClient client;

    public static void main(String[] args) {
        try {
            LOG.info("Listening on port " + DEFAULT_PORT);
            final ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT);

            LOG.info("Waiting for client to connect");
            final Socket socket = serverSocket.accept();

            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            AudreyServer server = new AudreyServer();
            final Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(server, in, out);

            LanguageClient client = launcher.getRemoteProxy();
            server.connect(client);

            launcher.startListening();
            LOG.info("Client connected");
        } catch (IOException e) {
            LOG.severe(e.getMessage());
            e.printStackTrace();
        }
    }

    public AudreyServer() {
        this.textDocumentService = new AudreyTextDocumentService();
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(final InitializeParams params) {
        workspaceRoot = params.getRootUri();

        final ServerCapabilities capabilities = new ServerCapabilities();
        capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
        capabilities.setCodeActionProvider(false);
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
        return new WorkspaceService() {
            @Override
            public void didChangeConfiguration(final DidChangeConfigurationParams params) {
            }

            @Override
            public void didChangeWatchedFiles(final DidChangeWatchedFilesParams params) {
            }
        };
    }

    @Override
    public void connect(final LanguageClient client) {
        this.client = client;
    }
}