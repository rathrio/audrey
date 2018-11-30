package io.rathr.audrey.storage;

import com.google.gson.Gson;
import com.oracle.truffle.api.source.SourceSection;

public final class Sample {
    private final String identifier;
    private final String metaObject;
    private final String value;
    private final String rootNodeId;
    private final Category category;

    private final String source;
    private final int sourceLine;
    private final int sourceIndex;
    private final int sourceLength;
    private final CharSequence sourceCharacters;

    private static final Gson GSON = new Gson();

    public Sample(final String identifier,
                  final String value,
                  final String metaObject,
                  final String category,
                  final SourceSection sourceSection,
                  final String rootNodeId) {

        this.identifier = identifier;
        this.value = value;
        this.metaObject = metaObject;
        this.category = Category.valueOf(category.trim().toUpperCase());
        this.rootNodeId = rootNodeId;

        this.source = sourceSection.getSource().getName();
        this.sourceLine = sourceSection.getStartLine();
        this.sourceIndex = sourceSection.getCharIndex();
        this.sourceLength = sourceSection.getCharLength();
        this.sourceCharacters = sourceSection.getCharacters();
    }

    public final String getRootNodeId() {
        return rootNodeId;
    }

    enum Category {
        RETURN,
        ARGUMENT,
        STATEMENT;
    }

    @Override
    public final String toString() {
        return toJson();
    }

    public final String toJson() {
        return GSON.toJson(this);
    }
}