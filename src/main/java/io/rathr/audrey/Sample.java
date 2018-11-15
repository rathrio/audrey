package io.rathr.audrey;

public final class Sample {
    final String identifier = "";
    final String metaObject;
    final String value;
    final Category category;

    public Sample(String value, String metaObject, String category) {
        this.value = value;
        this.metaObject = metaObject;
        this.category = Category.valueOf(category.trim().toUpperCase());
    }

    enum Category {
        RETURN,
        ARGUMENT;
    }
}