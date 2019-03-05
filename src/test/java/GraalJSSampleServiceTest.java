import io.rathr.audrey.lsp.graaljs.GraalJSSampleService;
import io.rathr.audrey.storage.Sample;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GraalJSSampleServiceTest {
    private final GraalJSSampleService service = new GraalJSSampleService();

    @Test
    public void testSimple() {
        final Path path = path("simple.js");
        final String uri = path.toUri().toString();
        final HashSet<Sample> samples = new HashSet<>();

        final Sample relevantArg = new Sample(
            "a",
            "bar",
            "string",
            "ARGUMENT",
            path.toString(),
            1,
            "foo"
        );
        samples.add(relevantArg);

        final Sample irrelevantArg = new Sample(
            "a",
            "bar",
            "String",
            "ARGUMENT",
            path.toString(),
            12,
            "bar"
        );
        samples.add(irrelevantArg);

        final Sample relevantReturn = new Sample(
            "",
            "42",
            "number",
            "RETURN",
            path.toString(),
            1,
            "foo"
        );
        samples.add(relevantReturn);

        final Sample irrelevantReturn = new Sample(
            "",
            "42",
            "number",
            "RETURN",
            path.toString(),
            12,
            "bar"
        );
        samples.add(irrelevantReturn);

        service.didOpen(uri);

        final Set<Sample> results = service.filterSamples(samples, uri, 0, 0);
        assertTrue(results.contains(relevantArg));
        assertTrue(results.contains(relevantReturn));
        assertFalse(results.contains(irrelevantArg));
        assertFalse(results.contains(irrelevantReturn));

        service.didClose(uri);
    }

    @Test
    public void testCanDifferentiateMethods() {
        final Path path = path("two_greets.js");
        final String uri = path.toUri().toString();
        final HashSet<Sample> samples = new HashSet<>();

        final Sample greet1Arg = new Sample(
            "target",
            "Haidar",
            "string",
            "ARGUMENT",
            path.toString(),
            2,
            "greet"
        );
        samples.add(greet1Arg);

        final Sample greet1Return = new Sample(
            "",
            "Hi Haidar",
            "string",
            "RETURN",
            path.toString(),
            2,
            "greet"
        );
        samples.add(greet1Return);

        final Sample greet2Arg = new Sample(
            "target",
            "Spongebob",
            "string",
            "ARGUMENT",
            path.toString(),
            8,
            "greet"
        );
        samples.add(greet2Arg);

        final Sample greet2Return = new Sample(
            "",
            "Move along, Spongebob",
            "string",
            "RETURN",
            path.toString(),
            8,
            "greet"
        );
        samples.add(greet2Return);

        service.didOpen(uri);

        final Set<Sample> greet1Results = service.filterSamples(samples, uri, 1, 10);
        assertTrue(greet1Results.contains(greet1Arg));
        assertTrue(greet1Results.contains(greet1Return));
        assertFalse(greet1Results.contains(greet2Arg));
        assertFalse(greet1Results.contains(greet2Return));

        final Set<Sample> greet1ReturnResults = service.filterSamples(samples, uri, 2, 15);
        assertTrue(greet1ReturnResults.contains(greet1Return));
        assertFalse(greet1ReturnResults.contains(greet1Arg));
        assertFalse(greet1ReturnResults.contains(greet2Arg));
        assertFalse(greet1ReturnResults.contains(greet2Return));

        final Set<Sample> greet2Results = service.filterSamples(samples, uri, 7, 10);
        assertTrue(greet2Results.contains(greet2Arg));
        assertTrue(greet2Results.contains(greet2Return));
        assertFalse(greet2Results.contains(greet1Arg));
        assertFalse(greet2Results.contains(greet1Return));

        final Set<Sample> greet2ReturnResults = service.filterSamples(samples, uri, 8, 15);
        assertTrue(greet2ReturnResults.contains(greet2Return));
        assertFalse(greet2ReturnResults.contains(greet2Arg));
        assertFalse(greet2ReturnResults.contains(greet1Arg));
        assertFalse(greet2ReturnResults.contains(greet1Return));

        service.didClose(uri);
    }

    @Test
    public void testNestedFunctions() {
        final Path path = path("nested_functions.js");
        final String uri = path.toUri().toString();
        final HashSet<Sample> samples = new HashSet<>();

        final Sample outerReturn = new Sample(
            "",
            "{name: \"Boris\", age: 18}",
            "object",
            "RETURN",
            path.toString(),
            1,
            "outer"
        );
        samples.add(outerReturn);

        final Sample innerReturn = new Sample(
            "",
            "{name: \"Boris\", age: 18}",
            "object",
            "RETURN",
            path.toString(),
            2,
            "inner"
        );
        samples.add(innerReturn);

        final Sample innerArg = new Sample(
            "person",
            "{name: \"Boris\", age: 17}",
            "object",
            "ARGUMENT",
            path.toString(),
            2,
            "inner"
        );
        samples.add(innerArg);

        service.didOpen(uri);

        final Set<Sample> outerResults = service.filterSamples(samples, uri, 0, 0);
        assertTrue(outerResults.contains(outerReturn));
        assertFalse(outerResults.contains(innerReturn));
        assertFalse(outerResults.contains(innerArg));

        final Set<Sample> innerResults = service.filterSamples(samples, uri, 1, 15);
        assertTrue(innerResults.contains(innerArg));
        assertTrue(innerResults.contains(innerReturn));
        assertFalse(innerResults.contains(outerReturn));

        service.didClose(uri);
    }

    private Path path(String filename) {
        return Paths.get("src/test/test_sources", filename).toAbsolutePath();
    }
}
