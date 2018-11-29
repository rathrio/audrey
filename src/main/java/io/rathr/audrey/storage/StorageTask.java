package io.rathr.audrey.storage;

public class StorageTask implements Runnable {
    private final SampleStorage storage;
    private final Sample sample;

    public StorageTask(final SampleStorage storage, final Sample sample) {
        this.storage = storage;
        this.sample = sample;
    }

    @Override
    public void run() {
        storage.add(sample);
    }
}
