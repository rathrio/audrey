import io.rathr.audrey.instrumentation.Audrey;
import org.graalvm.polyglot.Context;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AudreyTest {
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    private Context context = Context.newBuilder().in(System.in).out(out).err(err).build();
    private Audrey sampler;

    @Before
    public void setupAudrey() {
        sampler = Audrey.find(context.getEngine());
        assertNotNull(sampler);
    }

    @Test
    public void testSanity() {
        assertEquals(true, true);
    }
}
