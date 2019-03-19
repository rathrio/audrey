package io.rathr.audrey.lsp;

import io.rathr.audrey.storage.Sample;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Generates {@link Hover} contents given a set of {@link Sample}s, e.g. a list of parameters annotated with their
 * types and example values.
 * <p>
 * Usage:
 *
 * <pre>
 *     new HoverReport().generate(samples, "javascript") // => Hover contents with code treated as JS.
 * </pre>
 */
class HoverReport {
    /**
     * @param samples
     * @param languageId that can be used as an identifier for fenced code blocks in Markdown, e.g. "ruby" or
     *                   "javascript".
     * @return a {@link Hover} that
     * {@link TextDocumentService#hover} can provide to a client.
     */
    Hover generate(final Set<Sample> samples, final String languageId) {
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

            argumentMap
                .entrySet()
                .stream()
                .sorted(Comparator.comparingInt(e -> e.getValue().get(0).getIdentifierIndex()))
                .forEach(entry -> {
                    final String identifier = entry.getKey();
                    final List<Sample> args = entry.getValue();
                    final Sample argSample = args.get(0);
                    final String argMetaObject = argSample.getMetaObject();

                    contents.add(Either.forLeft("(parameter) " + identifier + ": `" + argMetaObject + "`"));
                    contents.add(Either.forRight(new MarkedString(languageId, argSample.getValue())));
                });
        }

        if (!returns.isEmpty()) {
            Collections.shuffle(returns);
            final Sample returnSample = returns.get(0);
            contents.add(Either.forLeft("Returns: `" + returnSample.getMetaObject() + "`"));
            contents.add(Either.forRight(new MarkedString(languageId, returnSample.getValue())));

        }

        return new Hover(contents);
    }
}
