package io.rathr.audrey.storage;

import com.google.gson.Gson;
import com.oracle.truffle.api.source.SourceSection;

public final class Sample {
    public enum Category {
        RETURN,
        ARGUMENT,
        STATEMENT
    }

    private final String identifier;
    private final String metaObject;
    private final String value;
    private final String rootNodeId;
    private final Category category;

    // Extracted from the source section and stored in primitive fields here so that we don't have to write a custom
    // GSON adapter for source sections, i.e. GSON.toJson(this) just works™.
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

    public static Sample fromJson(final String json) {
        return GSON.fromJson(json, Sample.class);
    }

    public final String getRootNodeId() {
        return rootNodeId;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getValue() {
        return value;
    }

    public Category getCategory() {
        return category;
    }

    public String getSource() {
        return source;
    }

    public int getSourceLine() {
        return sourceLine;
    }

    public int getSourceIndex() {
        return sourceIndex;
    }

    public int getSourceLength() {
        return sourceLength;
    }

    public CharSequence getSourceCharacters() {
        return sourceCharacters;
    }

    public String getMetaObject() {
        return metaObject;
    }

    @Override
    public final String toString() {
        return toJson();
    }

    public final String toJson() {
        return GSON.toJson(this);
    }
}
