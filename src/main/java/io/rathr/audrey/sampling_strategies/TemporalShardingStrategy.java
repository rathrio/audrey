package io.rathr.audrey.sampling_strategies;

public class TemporalShardingStrategy implements SamplingStrategy {
    @Override
    public boolean skipExtraction() {
        return false;
    }
}
