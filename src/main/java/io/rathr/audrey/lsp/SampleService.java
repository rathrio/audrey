package io.rathr.audrey.lsp;

import io.rathr.audrey.storage.Sample;

import java.util.Set;

public interface SampleService {
    void didOpen(final String uri);

    void didClose(final String uri);

    Set<Sample> filterSamples(final Set<Sample> samples, final String uri, final int line, final int column);
}
