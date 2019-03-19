package io.rathr.audrey.storage;

import com.oracle.truffle.api.source.SourceSection;

public final class Sample {
    public enum Category {
        RETURN,
        ARGUMENT,
        STATEMENT
    }

    private String identifier;
    private int identifierIndex;
    private String metaObject;
    private String value;
    private String rootNodeId;
    private Category category;

    private String source;
    /**
     * 1-based line number
     */
    private int sourceLine;
    private int sourceIndex;
    private int sourceLength;
    private CharSequence sourceCharacters;

    /**
     * Hash code of the frame where this sample was extracted from.
     */
    private int frameId;

    public Sample() {
    }

    public Sample(final String identifier,
                  final int identifierIndex,
                  final String value,
                  final String metaObject,
                  final String category,
                  final SourceSection sourceSection,
                  final String rootNodeId,
                  final int frameId) {

        this.identifier = identifier;
        this.identifierIndex = identifierIndex;
        this.value = value;
        this.metaObject = metaObject;
        this.category = Category.valueOf(category.trim().toUpperCase());
        this.rootNodeId = rootNodeId;

        this.source = sourceSection.getSource().getName();
        this.sourceLine = sourceSection.getStartLine();
        this.sourceIndex = sourceSection.getCharIndex();
        this.sourceLength = sourceSection.getCharLength();
        this.sourceCharacters = sourceSection.getCharacters();

        this.frameId = frameId;
    }

    public Sample(final String identifier,
                  final String value,
                  final String metaObject,
                  final String category,
                  final String source,
                  final int sourceLine,
                  final String rootNodeId) {

        this.identifier = identifier;
        this.value = value;
        this.metaObject = metaObject;
        this.category = Category.valueOf(category.trim().toUpperCase());
        this.rootNodeId = rootNodeId;

        this.source = source;
        this.sourceLine = sourceLine;
    }

    public boolean isArgument() {
        return category.equals(Category.ARGUMENT);
    }

    public boolean isReturn() {
        return category.equals(Category.RETURN);
    }

    public String getRootNodeId() {
        return rootNodeId;
    }

    public String getIdentifier() {
        return identifier;
    }

    public int getIdentifierIndex() {
        return identifierIndex;
    }

    public void setIdentifierIndex(final int identifierIndex) {
        this.identifierIndex = identifierIndex;
    }

    public int getFrameId() {
        return frameId;
    }

    public void setFrameId(final int frameId) {
        this.frameId = frameId;
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

    public void setIdentifier(final String identifier) {
        this.identifier = identifier;
    }

    public void setMetaObject(final String metaObject) {
        this.metaObject = metaObject;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public void setRootNodeId(final String rootNodeId) {
        this.rootNodeId = rootNodeId;
    }

    public void setCategory(final Category category) {
        this.category = category;
    }

    public void setSource(final String source) {
        this.source = source;
    }

    public void setSourceLine(final int sourceLine) {
        this.sourceLine = sourceLine;
    }

    public void setSourceIndex(final int sourceIndex) {
        this.sourceIndex = sourceIndex;
    }

    public void setSourceLength(final int sourceLength) {
        this.sourceLength = sourceLength;
    }

    public void setSourceCharacters(final CharSequence sourceCharacters) {
        this.sourceCharacters = sourceCharacters;
    }

    /**
     * Whether the value of this sample represents a missing value, e.g. nil in Ruby or undefined in JS.
     * <p>
     * Note that this is approach is not future-proof and currently only used for filtering purposes in the language
     * server.
     */
    public boolean isBlank() {
        return value.equals("undefined") || value.equals("nil");
    }

    public boolean isPresent() {
        return !isBlank();
    }
}
