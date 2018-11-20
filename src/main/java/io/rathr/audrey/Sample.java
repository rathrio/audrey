package io.rathr.audrey;

public final class Sample {
    final String identifier = "";
    final String metaObject;
    final String value;
    final String rootNodeIdentifier;
    final Category category;

    public Sample(String value, String metaObject, String category, String rootNodeIdentifier) {
        this.value = value;
        this.metaObject = metaObject;
        this.category = Category.valueOf(category.trim().toUpperCase());
        this.rootNodeIdentifier = rootNodeIdentifier;
    }

    enum Category {
        RETURN,
        ARGUMENT;
    }
}