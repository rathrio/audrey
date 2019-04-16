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
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RootNodeOnlyTest {
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
            .option("audrey.RootOnly", "true")
            .allowIO(true)
            .allowNativeAccess(true)
            .build();

        audrey = Audrey.find(context.getEngine());
        assertNotNull(audrey);

        storage = (InMemorySampleStorage) audrey.getStorage();
    }

    @Test
    public void testCollectsArgumentsAndReturnValuesInJS() {
        evalFile("add.js", "js");

        final Optional<Sample> arg1 = storage.newSearch()
            .forArguments()
            .rootNodeId("add")
            .identifier("x")
            .findFirst();

        assertTrue(arg1.isPresent());
        assertEquals("1", arg1.get().getValue());
        assertEquals("number", arg1.get().getMetaObject());
        assertEquals(0, arg1.get().getIdentifierIndex());

        final Optional<Sample> arg2 = storage.newSearch()
            .forArguments()
            .rootNodeId("add")
            .identifier("y")
            .findFirst();

        assertTrue(arg2.isPresent());
        assertEquals("2", arg2.get().getValue());
        assertEquals("number", arg2.get().getMetaObject());
        assertEquals(1, arg2.get().getIdentifierIndex());

        final Optional<Sample> returnSample = storage.newSearch()
            .forReturns()
            .rootNodeId("add")
            .findFirst();

        assertTrue(returnSample.isPresent());
        assertEquals("3", returnSample.get().getValue());
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
