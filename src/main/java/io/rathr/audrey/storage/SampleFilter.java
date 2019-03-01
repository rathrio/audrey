package io.rathr.audrey.storage;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Can filter a Set of {@link Sample}s, e.g.
 *
 * <pre>
 *     new Search(samples)
 *        .forArguments()
 *        .value("\"foobar\"")
 *        .apply() // => Stream of argument samples with value "foobar"
 * </pre>
 */
public class SampleFilter {
    private final Set<Sample> samples;
    private String category;
    private String rootNodeId;
    private String identifier;
    private Integer line;
    private String value;
    private String source;

    public SampleFilter(final Set<Sample> samples) {
        this.samples = samples;
    }

    public SampleFilter forArguments() {
        category = "ARGUMENT";
        return this;
    }
    
    public SampleFilter forReturns() {
        category = "RETURN";
        return this;
    }

    public SampleFilter rootNodeId(final String rootNodeId) {
        this.rootNodeId = rootNodeId;
        return this;
    }

    public SampleFilter identifier(final String identifier) {
        this.identifier = identifier;
        return this;
    }

    public SampleFilter line(final int line) {
        this.line = line;
        return this;
    }

    public SampleFilter value(final String value) {
        this.value = value;
        return this;
    }

    public SampleFilter source(final String source) {
        this.source = source;
        return this;
    }


    public Stream<Sample> apply() {
        Stream<Sample> stream = samples.stream();
        if (category != null) {
            stream = stream.filter(sample -> category.equals(sample.getCategory().name()));
        }

        if (rootNodeId != null) {
            stream = stream.filter(sample -> rootNodeId.equals(sample.getRootNodeId()));
        }

        if (identifier != null) {
            stream = stream.filter(sample -> identifier.equals(sample.getIdentifier()));
        }

        if (line != null) {
            stream = stream.filter(sample -> line == sample.getSourceLine());
        }

        if (value != null) {
            stream = stream.filter(sample -> value.equals(sample.getValue()));
        }

        if (source != null) {
            stream = stream.filter(sample -> source.endsWith(sample.getSource()));
        }

        return stream;
    }

    public Optional<Sample> findFirst() {
        return apply().findFirst();
    }
}
