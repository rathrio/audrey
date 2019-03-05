package io.rathr.audrey.lsp;

import io.rathr.audrey.lsp.graaljs.GraalJSSampleService;
import io.rathr.audrey.lsp.ruby.RubySampleService;
import io.rathr.audrey.storage.Sample;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Delegates requests to the language-specific sample services.
 *
 * @see RubySampleService
 * @see GraalJSSampleService
 */
public class AudreyTextDocumentService implements TextDocumentService {
    private static final CompletableFuture<Hover> EMPTY_HOVER =
        CompletableFuture.completedFuture(new Hover(new ArrayList<>()));

    private final HoverReport report = new HoverReport();
    private Set<Sample> samples = new HashSet<>();

    private final Map<String, TextDocumentItem> documents = new HashMap<>();
    private final Map<String, SampleService> sampleServices = new HashMap<>();

    @Override
    public CompletableFuture<Hover> hover(final TextDocumentPositionParams position) {
        AudreyServer.LOG.info("hover " + position.toString());
        final String uri = position.getTextDocument().getUri();
        final int line = position.getPosition().getLine();
        final int column = position.getPosition().getCharacter();

        final String languageId = documents.get(uri).getLanguageId();
        final Set<Sample> filteredSamples = sampleService(languageId).filterSamples(samples, uri, line, column);

        if (filteredSamples.isEmpty()) {
            return EMPTY_HOVER;
        }

        final Hover hover = report.generate(filteredSamples, languageId);
        return CompletableFuture.completedFuture(hover);
    }

    @Override
    public void didOpen(final DidOpenTextDocumentParams params) {
        final TextDocumentItem document = params.getTextDocument();
        final String uri = document.getUri();
        documents.put(uri, document);

        final String languageId = document.getLanguageId();
        sampleService(languageId).didOpen(uri);
        AudreyServer.LOG.info("didOpen " + uri);
    }

    @Override
    public void didChange(final DidChangeTextDocumentParams params) {
    }

    @Override
    public void didClose(final DidCloseTextDocumentParams params) {
        final String uri = params.getTextDocument().getUri();
        final String languageId = documents.get(uri).getLanguageId();
        sampleService(languageId).didClose(uri);
        documents.remove(uri);
        AudreyServer.LOG.info("didClose " + uri);
    }

    @Override
    public void didSave(final DidSaveTextDocumentParams params) {
    }

    void setSamples(final Set<Sample> samples) {
        this.samples = samples;
    }

    private SampleService sampleService(final String languageId) {
        sampleServices.computeIfAbsent(languageId, s -> {
            switch (languageId) {
                case "javascript": return new GraalJSSampleService();
                case "ruby":       return new RubySampleService();
                default:           throw new Error("Unsupported language: " + languageId);
            }
        });

        return sampleServices.get(languageId);
    }
}
