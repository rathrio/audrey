package io.rathr.audrey.sampling;

public class SampleNone implements SamplingStrategy {
    @Override
    public boolean skipExtraction() {
        return true;
    }
}
