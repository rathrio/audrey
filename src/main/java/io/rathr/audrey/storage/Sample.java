package io.rathr.audrey.storage;

import com.oracle.truffle.api.source.SourceSection;

public final class Sample {
    private final String identifier;
    private final String metaObject;
    private final String value;
    private final String rootNodeId;
    private final Category category;
    private final SourceSection sourceSection;

    public Sample(final String identifier,
                  final String value,
                  final String metaObject,
                  final String category,
                  final SourceSection sourceSection) {

        this.identifier = identifier;
        this.value = value;
        this.metaObject = metaObject;
        this.category = Category.valueOf(category.trim().toUpperCase());
        this.sourceSection = sourceSection;
        this.rootNodeId = "";
    }

    public SourceSection getSourceSection() {
        return sourceSection;
    }

    enum Category {
        RETURN,
        ARGUMENT,
        STATEMENT;
    }
}