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
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
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
            .allowAllAccess(true)
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

        assertTrue(arg.isPresent());
        assertEquals("bar", arg.get().getValue());
    }

    @Test
    public void testCollectsArgumentsAndReturnValuesInJavaScript() {
        evalFile("add.js", "js");

        final Optional<Sample> arg1 = storage.newSearch()
            .forArguments()
            .rootNodeId("add")
            .identifier("x")
            .findFirst();

        assertTrue(arg1.isPresent());
        assertEquals("1", arg1.get().getValue());

        final Optional<Sample> arg2 = storage.newSearch()
            .forArguments()
            .rootNodeId("add")
            .identifier("y")
            .findFirst();

        assertTrue(arg2.isPresent());
        assertEquals("2", arg2.get().getValue());

        final Optional<Sample> returnSample = storage.newSearch()
            .forReturns()
            .rootNodeId("add")
            .findFirst();

        assertTrue(returnSample.isPresent());
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

        assertTrue(arg1.isPresent());
        assertEquals("1", arg1.get().getValue());

        final Optional<Sample> arg2 = storage.newSearch()
            .forArguments()
            .rootNodeId("Object#add")
            .identifier("y")
            .findFirst();

        assertTrue(arg2.isPresent());
        assertEquals("2", arg2.get().getValue());

        final Optional<Sample> returnSample = storage.newSearch()
            .forReturns()
            .rootNodeId("Object#add")
            .findFirst();

        assertTrue(returnSample.isPresent());
        assertEquals("3", returnSample.get().getValue());
    }

    @Test
    public void testCollectsArgumentsAndReturnValuesInR() {
        evalFile("add.R", "R");

        final Optional<Sample> arg1 = storage.newSearch()
            .forArguments()
            .rootNodeId("add")
            .identifier("x")
            .findFirst();

        assertTrue(arg1.isPresent());
        assertEquals("1", arg1.get().getValue());

        final Optional<Sample> arg2 = storage.newSearch()
            .forArguments()
            .rootNodeId("add")
            .identifier("y")
            .findFirst();

        assertTrue(arg2.isPresent());
        assertEquals("2", arg2.get().getValue());

        final Optional<Sample> returnSample = storage.newSearch()
            .forReturns()
            .rootNodeId("add")
            .findFirst();

        assertTrue(returnSample.isPresent());
        assertEquals("3.0", returnSample.get().getValue());
    }

    @Test
    public void testCollectsNonPrimitiveValuesInJavaScript() {
        evalFile("non_primitive.js", "js");

        final Optional<Sample> arg = storage.newSearch()
            .forArguments()
            .identifier("w")
            .rootNodeId("magnitude")
            .findFirst();

        assertTrue(arg.isPresent());
        assertEquals("{x: 34, y: 12, z: 6}", arg.get().getValue());

        final Optional<Sample> returnSample = storage.newSearch()
            .forReturns()
            .rootNodeId("magnitude")
            .findFirst();

        assertTrue(returnSample.isPresent());
        assertEquals("36.55133376499413", returnSample.get().getValue());
    }

    @Test
    public void testCollectsNonPrimitiveValuesInRuby() {
        evalFile("non_primitive.rb", "ruby");

        final Optional<Sample> arg = storage.newSearch()
            .forArguments()
            .identifier("w")
            .rootNodeId("Object#magnitude")
            .findFirst();

        assertTrue(arg.isPresent());
        assertEquals("#<struct Vector x=34, y=12, z=6>", arg.get().getValue());

        final Optional<Sample> returnSample = storage.newSearch()
            .forReturns()
            .rootNodeId("Object#magnitude")
            .findFirst();

        assertTrue(returnSample.isPresent());
        assertEquals("36.55133376499413", returnSample.get().getValue());
    }

    @Test
    public void testCanDifferentiateMethodsInJavascript() {
        // NOTE that we currently extract arguments from the first statement in the function body.
        evalFile("two_greets.js", "js");

        final Optional<Sample> arg = storage.newSearch()
            .forArguments()
            .rootNodeId("greet")
            .identifier("target")
            .line(3)
            .findFirst();

        assertTrue(arg.isPresent());
        assertEquals("Haidar", arg.get().getValue());

        final Optional<Sample> differentArg = storage.newSearch()
            .forArguments()
            .rootNodeId("greet")
            .identifier("target")
            .line(9)
            .findFirst();

        assertTrue(differentArg.isPresent());
        assertEquals("Spongebob", differentArg.get().getValue());
    }

    @Test
    public void testCanDifferentiateMethodsInRuby() {
        evalFile("two_greets.rb", "ruby");

        final Optional<Sample> arg = storage.newSearch()
            .forArguments()
            .rootNodeId("NicePerson.greet")
            .identifier("target")
            .findFirst();

        assertTrue(arg.isPresent());
        assertEquals("\"Haidar\"", arg.get().getValue());

        final Optional<Sample> differentArg = storage.newSearch()
            .forArguments()
            .rootNodeId("NotSoNicePerson.greet")
            .identifier("target")
            .findFirst();

        assertTrue(differentArg.isPresent());
        assertEquals("\"Spongebob\"", differentArg.get().getValue());
    }

    @Test
    public void testModuleFunctionInRuby() {
        evalFile("module_function.rb", "ruby");

        final List<Sample> args = storage.newSearch()
            .forArguments()
            .rootNodeId("Helpers#upcase")
            .identifier("str")
            .search().collect(Collectors.toList());

        assertEquals(2, args.size());

        final Optional<Sample> arg1 =
            args.stream().filter(sample -> sample.getValue().equals("\"foobar\"")).findFirst();

        final Optional<Sample> arg2 =
            args.stream().filter(sample -> sample.getValue().equals("\"chello\"")).findFirst();

        assertTrue(arg1.isPresent());
        assertTrue(arg2.isPresent());

        final Optional<Sample> ret1 = storage.newSearch()
            .forReturns()
            .rootNodeId("Helpers#upcase")
            .value("\"FOOBAR\"")
            .findFirst();

        assertTrue(ret1.isPresent());

        final Optional<Sample> ret2 = storage.newSearch()
            .forReturns()
            .rootNodeId("Helpers#upcase")
            .value("\"CHELLO\"")
            .findFirst();

        assertTrue(ret2.isPresent());
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
