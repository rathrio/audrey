package io.rathr.audrey.sampling;

public class SampleAll implements SamplingStrategy {
    @Override
    public boolean skipExtraction() {
        return false;
    }
}
