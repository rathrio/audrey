package io.rathr.audrey.lsp;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class AudreyServerLauncher {
    final static int DEFAULT_PORT = 8123;

    public static void main(String[] args) {
        try {
            AudreyLogger.info("Listening on port " + DEFAULT_PORT);
            final ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT);

            AudreyLogger.info("Waiting on client connection");
            final Socket socket = serverSocket.accept();
            AudreyLogger.info("Client connected");

            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            AudreyServer server = new AudreyServer();
            final Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(server, in, out);

            LanguageClient client = launcher.getRemoteProxy();
            server.connect(client);

            launcher.startListening();
        } catch (IOException e) {
            AudreyLogger.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
