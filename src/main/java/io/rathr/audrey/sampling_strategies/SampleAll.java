package io.rathr.audrey.sampling_strategies;

public class SampleAll implements SamplingStrategy {
    @Override
    public boolean skipExtraction() {
        return false;
    }
}
