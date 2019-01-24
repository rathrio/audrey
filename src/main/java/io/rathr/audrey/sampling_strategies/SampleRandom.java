package io.rathr.audrey.sampling_strategies;

import java.util.Random;

public final class SampleRandom implements SamplingStrategy {
    private final Random rand = new Random();

    @Override
    public boolean skipExtraction() {
        return rand.nextInt(2) == 0;
    }
}
