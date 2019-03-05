import io.rathr.audrey.lsp.ruby.RubySampleService;
import io.rathr.audrey.storage.Sample;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RubySampleServiceTest {
    private final RubySampleService service = new RubySampleService();

    @Test
    public void testSimple() {
        final Path path = path("simple.rb");
        final String uri = path.toUri().toString();
        final HashSet<Sample> samples = new HashSet<>();
        final Sample relevantArg = new Sample(
            "a",
            "\"bar\"",
            "String",
            "ARGUMENT",
            path.toString(),
            1,
            "Object#foo"
        );
        samples.add(relevantArg);

        final Sample irrelevantArg = new Sample(
            "a",
            "\"bar\"",
            "String",
            "ARGUMENT",
            path.toString(),
            42,
            "Object#bar"
        );
        samples.add(irrelevantArg);

        final Sample relevantReturn = new Sample(
            "",
            "42",
            "Integer",
            "RETURN",
            path.toString(),
            1,
            "Object#foo"
        );
        samples.add(relevantReturn);

        final Sample irrelevantReturn = new Sample(
            "",
            "42",
            "Integer",
            "RETURN",
            path.toString(),
            1,
            "Object#bar"
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
    public void testModuleFunction() {
        final Path path = path("module_function.rb");
        final String uri = path.toUri().toString();
        final HashSet<Sample> samples = new HashSet<>();

        final Sample upcaseArg = new Sample(
            "str",
            "\"foobar\"",
            "String",
            "ARGUMENT",
            path.toString(),
            5,
            "Helpers#upcase"
        );
        samples.add(upcaseArg);

        final Sample publicUpcaseArg = new Sample(
            "str",
            "\"foobar\"",
            "String",
            "ARGUMENT",
            path.toString(),
            12,
            "SomeClass#public_upcase"
        );
        samples.add(publicUpcaseArg);

        service.didOpen(uri);

        final Set<Sample> upcaseResults = service.filterSamples(samples, uri, 3, 10);
        assertTrue(upcaseResults.contains(upcaseArg));
        assertFalse(upcaseResults.contains(publicUpcaseArg));

        final Set<Sample> publicUpcaseResults = service.filterSamples(samples, uri, 11, 10);
        assertFalse(publicUpcaseResults.contains(upcaseArg));
        assertTrue(publicUpcaseResults.contains(publicUpcaseArg));

        service.didClose(uri);
    }

    @Test
    public void testNesting() {
        final Path path = path("nesting.rb");
        final String uri = path.toUri().toString();
        final HashSet<Sample> samples = new HashSet<>();

        final Sample methodInAArg = new Sample(
            "x",
            "\"hi\"",
            "String",
            "ARGUMENT",
            path.toString(),
            8,
            "A.method_in_a"
        );
        samples.add(methodInAArg);

        final Sample methodInBArg = new Sample(
            "x",
            "\"there\"",
            "String",
            "ARGUMENT",
            path.toString(),
            3,
            "A::B#method_in_b"
        );
        samples.add(methodInBArg);

        service.didOpen(uri);

        final Set<Sample> methodInAResults = service.filterSamples(samples, uri, 7, 10);
        assertTrue(methodInAResults.contains(methodInAArg));
        assertFalse(methodInAResults.contains(methodInBArg));


        final Set<Sample> methodInBResults = service.filterSamples(samples, uri, 2, 10);
        assertFalse(methodInBResults.contains(methodInAArg));
        assertTrue(methodInBResults.contains(methodInBArg));

        service.didClose(uri);
    }

    @Test
    public void testClassSelf() {
        final Path path = path("class_self.rb");
        final String uri = path.toUri().toString();
        final HashSet<Sample> samples = new HashSet<>();

        final Sample arg = new Sample(
            "baz",
            "[1, 2, 3, 4, 5]",
            "Array",
            "ARGUMENT",
            path.toString(),
            3,
            "Dog.foobar"
        );
        samples.add(arg);

        final Sample ret = new Sample(
            "",
            "[1, 3, 5]",
            "Array",
            "RETURN",
            path.toString(),
            3,
            "Dog.foobar"
        );
        samples.add(ret);

        service.didOpen(uri);
        final Set<Sample> results = service.filterSamples(samples, uri, 2, 10);
        assertTrue(results.contains(arg));
        assertTrue(results.contains(ret));
        service.didClose(uri);
    }

    private Path path(String filename) {
        return Paths.get("src/test/test_sources", filename).toAbsolutePath();
    }
}
