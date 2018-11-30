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
                  final SourceSection sourceSection,
                  final String rootNodeId) {

        this.identifier = identifier;
        this.value = value;
        this.metaObject = metaObject;
        this.category = Category.valueOf(category.trim().toUpperCase());
        this.sourceSection = sourceSection;
        this.rootNodeId = rootNodeId;
    }

    public SourceSection getSourceSection() {
        return sourceSection;
    }

    public String getRootNodeId() {
        return rootNodeId;
    }

    enum Category {
        RETURN,
        ARGUMENT,
        STATEMENT;
    }

    @Override
    public String toString() {
        // TODO: Use GSON instead of this poor man's approach.
        StringBuffer result = new StringBuffer("{ ");
        result.append("identifier: ");
        result.append("\"").append(identifier).append("\"");
        result.append(", ");

        result.append("value: ");
        result.append(value);
        result.append(", ");

        result.append("type: ");
        result.append("\"").append(metaObject).append("\"");
        result.append(", ");

        result.append("source: ");
        result.append("\"").append(sourceSection).append("\"");
        result.append(", ");

        result.append("rootId: ");
        result.append("\"").append(rootNodeId).append("\"");
        result.append(" }");

        return result.toString();
    }

    public String getId() {
        final String sourceName = sourceSection.getSource().getName();
        return "audrey:" + sourceName + ":\"" + rootNodeId + "\"";
    }
}