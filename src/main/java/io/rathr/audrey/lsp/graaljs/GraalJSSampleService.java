package io.rathr.audrey.lsp.graaljs;

import com.oracle.js.parser.ErrorManager;
import com.oracle.js.parser.Parser;
import com.oracle.js.parser.ScriptEnvironment;
import com.oracle.js.parser.Source;
import com.oracle.js.parser.ir.FunctionNode;
import io.rathr.audrey.lsp.SampleService;
import io.rathr.audrey.storage.Sample;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static io.rathr.audrey.lsp.AudreyServer.LOG;

public class GraalJSSampleService implements SampleService {
    // Support ES9 and try to be as lenient as possible.
    private static final ScriptEnvironment SCRIPT_ENV = ScriptEnvironment.builder()
        .strict(false)
        .ecmaScriptVersion(9)
        .earlyLvalueError(false)
        .emptyStatements(true)
        .syntaxExtensions(false)
        .scripting(false)
        .shebang(true)
        .constAsVar(true)
        .build();

    private static final ErrorManager ERROR_MANAGER = new ErrorManager.ThrowErrorManager();

    private final Map<String, FunctionNode> asts = new HashMap<>();

    @Override
    public void didOpen(final String uri) {
        String contents = null;
        try {
            contents = new String(Files.readAllBytes(Paths.get(new URI(uri))));
        } catch (IOException | URISyntaxException e) {
            LOG.severe(e.getMessage());
            e.printStackTrace();
        }

        final Source source = Source.sourceFor(uri, contents);
        final Parser parser = new Parser(SCRIPT_ENV, source, ERROR_MANAGER);
        final FunctionNode ast = parser.parse();

        asts.put(uri, ast);
    }

    @Override
    public void didClose(final String uri) {
        asts.remove(uri);
    }

    @Override
    public Set<Sample> filterSamples(final Set<Sample> samples, final String uri, final int line, final int column) {
        return null;
    }
}
