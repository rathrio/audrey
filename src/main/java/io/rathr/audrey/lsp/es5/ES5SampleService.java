package io.rathr.audrey.lsp.es5;

import io.rathr.audrey.lsp.AudreyServer;
import io.rathr.audrey.lsp.SampleService;
import io.rathr.audrey.storage.Sample;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.IRFactory;
import org.mozilla.javascript.ast.AstRoot;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ES5SampleService implements SampleService {
    private final Map<String, AstRoot> asts = new HashMap<>();

    @Override
    public void didOpen(final String uri) {
        try {
            final String source = new String(Files.readAllBytes(Paths.get(new URI(uri))));

            final CompilerEnvirons env = new CompilerEnvirons();
            env.setRecoverFromErrors(true);
            env.setGenerateDebugInfo(true);
            env.setLanguageVersion(Context.VERSION_ES6);

            final StringReader stringReader = new StringReader(source);
            final IRFactory factory = new IRFactory(env);
            final AstRoot ast = factory.parse(stringReader, uri, 0);

            asts.put(uri, ast);
        } catch (IOException | URISyntaxException e) {
            AudreyServer.LOG.severe(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void didClose(final String uri) {
        asts.remove(uri);
    }

    @Override
    public Set<Sample> filterSamples(final Set<Sample> samples, final String uri, final int line, final int column) {
        final AstRoot ast = asts.get(uri);
        final ES5SampleCollector sampleCollector = new ES5SampleCollector(samples, uri, line, column);
        ast.visit(sampleCollector);

        return sampleCollector.getSamples();
    }
}
