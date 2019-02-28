package io.rathr.audrey.lsp.ruby;

import io.rathr.audrey.lsp.AudreyServer;
import io.rathr.audrey.lsp.SampleService;
import io.rathr.audrey.storage.Sample;
import org.jrubyparser.CompatVersion;
import org.jrubyparser.Parser;
import org.jrubyparser.ast.Node;
import org.jrubyparser.parser.ParserConfiguration;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RubySampleService implements SampleService {
    private final static ParserConfiguration CONFIG = new ParserConfiguration(0, CompatVersion.RUBY2_3);
    private Map<String, Node> asts = new HashMap<>();

    @Override
    public void didOpen(final String uri) {
        try {
            final String source = new String(Files.readAllBytes(Paths.get(new URI(uri))));
            Parser rubyParser = new Parser();
            final StringReader stringReader = new StringReader(source);
            final Node ast = rubyParser.parse(uri, stringReader, CONFIG);

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
        final Node ast = asts.get(uri);
        final RubySampleCollector sampleCollector = new RubySampleCollector(samples, uri, line, column);
        ast.accept(sampleCollector);

        return sampleCollector.getSamples();
    }
}
