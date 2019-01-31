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
import java.util.Map;
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
    }

    @Test
    public void testSanity() {
        assertTrue(true);
    }

    @Test
    public void testSimpleEval() {
        context.eval("js", "function foo(a) { console.log(a) } foo('bar')");
        final InMemorySampleStorage storage = (InMemorySampleStorage) audrey.getStorage();
        final Map<String, Set<Sample>> sampleMap = storage.getSampleMap();
        assertEquals(1, sampleMap.size());
        final Set<Sample> foo = sampleMap.get("foo");
        assertNotNull(foo);
        assertFalse(foo.isEmpty());
        foo.removeIf((e) -> !e.getMetaObject().equals("string"));
        assertFalse(foo.isEmpty());
    }

    @Test
    public void testSimpleFunction() {
        // eval(makeSourceFromFile("add.js", "js"));
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
}
