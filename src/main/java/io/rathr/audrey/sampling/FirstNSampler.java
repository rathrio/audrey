package io.rathr.audrey.sampling;

public class FirstNSampler implements Sampler {
    private final int N = 10;

    @Override
    public boolean skipExtraction() {
        return false;
    }
}
