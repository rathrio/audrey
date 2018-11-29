package io.rathr.audrey;

public final class Sample {
    final String identifier;
    final String metaObject;
    final String value;
    final String rootNodeIdentifier;
    final Category category;

    public Sample(String identifier, String value, String metaObject, String category) {
        this.identifier = identifier;
        this.value = value;
        this.metaObject = metaObject;
        this.category = Category.valueOf(category.trim().toUpperCase());
        this.rootNodeIdentifier = "";
    }

    enum Category {
        RETURN,
        ARGUMENT,
        STATEMENT;
    }
}