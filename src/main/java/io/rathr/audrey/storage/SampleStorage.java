package io.rathr.audrey.storage;

public interface SampleStorage {
    void add(Sample sample);
    void registerProject(Project project);
    void clear();
}
