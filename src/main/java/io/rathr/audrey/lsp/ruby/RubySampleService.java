package io.rathr.audrey.lsp.ruby;

import io.rathr.audrey.lsp.SampleService;
import io.rathr.audrey.storage.Sample;

import java.util.Set;

public class RubySampleService implements SampleService {
    @Override
    public void didOpen(final String uri) {

    }

    @Override
    public void didClose(final String uri) {

    }

    @Override
    public Set<Sample> filterSamples(final Set<Sample> samples, final String uri, final int line, final int column) {
        return null;
    }
}
