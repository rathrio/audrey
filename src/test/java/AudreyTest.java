import io.rathr.audrey.instrumentation.Audrey;
import io.rathr.audrey.storage.InMemorySampleStorage;
import io.rathr.audrey.storage.Sample;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AudreyTest {
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    private Context context;
    private Audrey audrey;
    private InMemorySampleStorage storage;

    @Before
    public void setupAudrey() {
        context = Context.newBuilder()
            .in(System.in)
            .out(out)
            .err(err)
            .option("audrey", "true")
            .option("audrey.Project", "tests")
            .option("audrey.Storage", "in_memory")
            .build();

        audrey = Audrey.find(context.getEngine());
        assertNotNull(audrey);

        storage = (InMemorySampleStorage) audrey.getStorage();
    }

    @Test
    public void testSanity() {
        assertTrue(true);
    }

    @Test
    public void testSimpleEval() {
        context.eval("js", "function foo(a) { console.log(a) } foo('bar')");

        final Optional<Sample> arg = storage.newSearch()
            .forArguments()
            .rootNodeId("foo")
            .identifier("a")
            .findFirst();

        assert(arg.isPresent());
        assertEquals("bar", arg.get().getValue());
    }

    @Test
    public void testCollectsArgumentAndReturnValuesInJavaScript() {
        evalFile("add.js", "js");

        final Optional<Sample> arg1 = storage.newSearch()
            .forArguments()
            .rootNodeId("add")
            .identifier("x")
            .findFirst();

        assert(arg1.isPresent());
        assertEquals("1", arg1.get().getValue());

        final Optional<Sample> arg2 = storage.newSearch()
            .forArguments()
            .rootNodeId("add")
            .identifier("y")
            .findFirst();

        assert(arg2.isPresent());
        assertEquals("2", arg2.get().getValue());

        final Optional<Sample> returnSample = storage.newSearch()
            .forReturns()
            .rootNodeId("add")
            .findFirst();

        assert(returnSample.isPresent());
        assertEquals("3", returnSample.get().getValue());
    }

    @Test
    public void testCollectsArgumentsAndReturnValuesInRuby() {
        evalFile("add.rb", "ruby");

        final Optional<Sample> arg1 = storage.newSearch()
            .forArguments()
            .rootNodeId("Object#add")
            .identifier("x")
            .findFirst();

        assert(arg1.isPresent());
        assertEquals("1", arg1.get().getValue());

        final Optional<Sample> arg2 = storage.newSearch()
            .forArguments()
            .rootNodeId("Object#add")
            .identifier("y")
            .findFirst();

        assert(arg2.isPresent());
        assertEquals("2", arg2.get().getValue());

        final Optional<Sample> returnSample = storage.newSearch()
            .forReturns()
            .rootNodeId("Object#add")
            .findFirst();

        assert(returnSample.isPresent());
        assertEquals("3", returnSample.get().getValue());
    }

    @Test
    public void testCollectsNonPrimitiveValuesInJavaScript() {
        evalFile("non_primitive.js", "js");

        final Optional<Sample> arg = storage.newSearch()
            .forArguments()
            .identifier("w")
            .rootNodeId("magnitude")
            .findFirst();

        assert(arg.isPresent());
        assertEquals("{x: 34, y: 12, z: 6}", arg.get().getValue());

        final Optional<Sample> returnSample = storage.newSearch()
            .forReturns()
            .rootNodeId("magnitude")
            .findFirst();

        assert(returnSample.isPresent());
        assertEquals("36.55133376499413", returnSample.get().getValue());
    }

    @Test
    public void testCollectsFromMethodsInJavascript() {
        // NOTE that we currently extract arguments from the first statement in the function body.
        evalFile("methods.js", "js");

        final Optional<Sample> arg = storage.newSearch()
            .forArguments()
            .rootNodeId("greet")
            .identifier("target")
            .line(3)
            .search()
            .findFirst();

        assert(arg.isPresent());
        assertEquals("Haidar", arg.get().getValue());

        final Optional<Sample> differentArg = storage.newSearch()
            .forArguments()
            .rootNodeId("greet")
            .identifier("target")
            .line(9)
            .search()
            .findFirst();

        assert(differentArg.isPresent());
        assertEquals("Spongebob", differentArg.get().getValue());
    }

    private Source makeSourceFromFile(String filename, String languageId) {
        return makeSource(readSourceString(filename), languageId);
    }

    private Source makeSource(String source, String languageId) {
        return Source.newBuilder(languageId, source, "test").buildLiteral();
    }

    private String readSourceString(String filename) {
        String contents = null;

        try {
            contents = new String(Files.readAllBytes(Paths.get("src/test/test_sources", filename)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return contents;
    }

    private void eval(Source source) {
        context.eval(source);
    }

    private void evalFile(String filename, String languageId) {
        eval(makeSourceFromFile(filename, languageId));
    }
}
