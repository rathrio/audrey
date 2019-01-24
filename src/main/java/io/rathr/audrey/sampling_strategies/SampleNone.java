package io.rathr.audrey.sampling_strategies;

public class SampleNone implements SamplingStrategy {
    @Override
    public boolean skipExtraction() {
        return true;
    }
}
