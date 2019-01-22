package io.rathr.audrey.sampling;

public class TemporalShardingStrategy implements SamplingStrategy {
    @Override
    public boolean skipExtraction() {
        return false;
    }
}
