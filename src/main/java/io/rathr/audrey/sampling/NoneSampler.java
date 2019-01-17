package io.rathr.audrey.sampling;

public class NoneSampler implements Sampler {
    @Override
    public boolean skipExtraction() {
        return true;
    }
}
