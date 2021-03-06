package io.rathr.audrey.lsp;

import io.rathr.audrey.instrumentation.Audrey;
import io.rathr.audrey.storage.Project;
import io.rathr.audrey.storage.RedisSampleStorage;
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
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * A <a href="https://microsoft.github.io/language-server-protocol/">language server</a>
 * that provides parameter and return value types and examples for text document hover requests.
 *
 * @see AudreyTextDocumentService
 */
public class AudreyServer implements LanguageServer, LanguageClientAware {
    static {
        System.setProperty(
            "java.util.logging.SimpleFormatter.format",
            "[%1$tF %1$tT] [AUDREY] [%4$-7s] %5$s %n"
        );
    }

    public static FileHandler FILE_HANDLER;
    public static final Logger LOG = Logger.getLogger(AudreyServer.class.getName());

    private final static int DEFAULT_PORT = 8123;

    private final AudreyTextDocumentService textDocumentService;
    private LanguageClient client;
    private Project project;

    public static void main(String[] args) {
        try {
            InputStream in;
            OutputStream out;

            FILE_HANDLER = new FileHandler(
                System.getenv("HOME") + "/audrey.log",
                true
            );

            final SimpleFormatter formatter = new SimpleFormatter();
            FILE_HANDLER.setFormatter(formatter);
            LOG.addHandler(FILE_HANDLER);

            if (args.length > 0 && args[0].equals("--stdio")) {
                LOG.info("Listening on STDIN");
                in = System.in;
                out = System.out;
            } else {
                LOG.info("Listening on port " + DEFAULT_PORT);
                final ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT);

                LOG.info("Waiting for client to connect");
                final Socket socket = serverSocket.accept();
                LOG.info("Client connected");

                in = socket.getInputStream();
                out = socket.getOutputStream();
            }

            AudreyServer server = new AudreyServer();
            final Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(server, in, out);

            LanguageClient client = launcher.getRemoteProxy();
            server.connect(client);

            launcher.startListening();
        } catch (IOException e) {
            LOG.severe(e.getMessage());
            e.printStackTrace();
        }
    }

    private AudreyServer() {
        this.textDocumentService = new AudreyTextDocumentService();
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(final InitializeParams params) {
        String projectId = System.getenv("AUDREY_PROJECT_ID");
        if (projectId == null || projectId.isEmpty()) {
            projectId = Audrey.DEFAULT_PROJECT_ID;
        }
        project = new Project(projectId, params.getRootUri());

        // Load all samples into memory and let the document service know about them. Pass "registerProject: false" to
        // disable any Redis writes. We just want to read the data here.
        final RedisSampleStorage storage = new RedisSampleStorage(project, false);
        textDocumentService.setSamples(storage.getSamples());

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
        LOG.info("Exit");
        FILE_HANDLER.close();
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
