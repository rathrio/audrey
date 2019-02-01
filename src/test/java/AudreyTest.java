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
        final Map<String, Set<Sample>> sampleMap = storage.getSampleMap();
        assertEquals(1, sampleMap.size());
        final Set<Sample> foo = sampleMap.get("foo");
        assertNotNull(foo);
        assertFalse(foo.isEmpty());
        foo.removeIf((e) -> !e.getMetaObject().equals("string"));
        assertFalse(foo.isEmpty());
    }

    @Test
    public void testCollectsArgumentAndReturnValues() {
        evalFile("add.js", "js");

        final Optional<Sample> x = storage.findBy("x", "add", "ARGUMENT");
        assert(x.isPresent());
        assertEquals("1", x.get().getValue());

        final Optional<Sample> y = storage.findBy("y", "add", "ARGUMENT");
        assert(y.isPresent());
        assertEquals("2", y.get().getValue());

        final Optional<Sample> returnSample = storage.findBy(null, "add", "RETURN");
        assert(returnSample.isPresent());
        assertEquals("3", returnSample.get().getValue());
    }

    @Test
    public void testCollectsNonPrimitiveValues() {
        evalFile("non_primitive.js", "js");
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
