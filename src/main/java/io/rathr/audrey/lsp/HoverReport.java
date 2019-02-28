package io.rathr.audrey.lsp;

import io.rathr.audrey.storage.Sample;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class HoverReport {
    private static final String JAVASCRIPT_ID = "javascript";
    private static final String RUBY_ID = "ruby";

    Hover generate(final Set<Sample> samples) {
        List<Either<String, MarkedString>> contents = new ArrayList<>();

        List<Sample> arguments = new ArrayList<>();
        List<Sample> returns = new ArrayList<>();

        samples
            .stream()
            .filter(Sample::isPresent)
            .forEach(sample -> {
                if (sample.isArgument()) {
                    arguments.add(sample);
                } else if (sample.isReturn()) {
                    returns.add(sample);
                }
            });

        if (!arguments.isEmpty()) {
            Map<String, List<Sample>> argumentMap = new HashMap<>();
            arguments.forEach(argument -> {
                final String identifier = argument.getIdentifier();
                argumentMap.computeIfAbsent(identifier, k -> new ArrayList<>());
                argumentMap.get(identifier).add(argument);
            });

            argumentMap.forEach((identifier, args) -> {
                final Sample argSample = args.get(0);
                final String argMetaObject = argSample.getMetaObject();
                contents.add(Either.forLeft("(parameter) " + identifier + ": " + argMetaObject));
                contents.add(Either.forRight(new MarkedString(JAVASCRIPT_ID, argSample.getValue())));
            });
        }

        if (!returns.isEmpty()) {
            Collections.shuffle(returns);
            final Sample returnSample = returns.get(0);
            contents.add(Either.forLeft("=> " + returnSample.getMetaObject()));
            contents.add(Either.forRight(new MarkedString(JAVASCRIPT_ID, returnSample.getValue())));
        }

        return new Hover(contents);
    }
}
