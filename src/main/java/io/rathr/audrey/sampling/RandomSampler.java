package io.rathr.audrey.sampling;

import java.util.Random;

public final class RandomSampler implements Sampler {
    private final Random rand = new Random();

    @Override
    public boolean skipExtraction() {
        return rand.nextInt(2) == 0;
    }
}
