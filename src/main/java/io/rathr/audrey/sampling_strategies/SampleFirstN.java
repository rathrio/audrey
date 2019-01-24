package io.rathr.audrey.sampling_strategies;

public class SampleFirstN implements SamplingStrategy {
    private final int N = 10;

    @Override
    public boolean skipExtraction() {
        return false;
    }
}
