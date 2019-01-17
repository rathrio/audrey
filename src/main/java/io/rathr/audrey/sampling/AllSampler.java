package io.rathr.audrey.sampling;

public class AllSampler implements Sampler {
    @Override
    public boolean skipExtraction() {
        return false;
    }
}
