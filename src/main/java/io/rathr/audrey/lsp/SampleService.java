package io.rathr.audrey.lsp;

import io.rathr.audrey.storage.Sample;

import java.util.Set;

/**
 * A language specific document service.
 */
public interface SampleService {
    /**
     * Called when a document was opened. Can be used to parse and store a language specific AST for later use.
     *
     * @param uri of the opened file, e.g. "file:///Users/spongebob/foo/bar.js"
     */
    void didOpen(final String uri);

    /**
     * Called when a document was closed. Can be used for cleanup, e.g. discarding the AST of the document.
     *
     * @param uri of the closed file, e.g. "file:///Users/spongebob/foo/bar.js"
     */
    void didClose(final String uri);

    /**
     * Called on a hover request. Should be used to return relevant samples for the given location. Those
     * samples could then be used to generate relevant Hover contents for clients.
     */
    Set<Sample> filterSamples(final Set<Sample> samples, final String uri, final int line, final int column);
}
