package io.rathr.audrey.storage;

public final class Project {
    private final String id;
    private final String rootPath;

    public Project(final String id, final String rootPath) {
        this.id = id;
        this.rootPath = rootPath;
    }

    public String getId() {
        return id;
    }

    public String getRootPath() {
        return rootPath;
    }

    public boolean contains(final String path) {
        return path.startsWith(rootPath);
    }
}
