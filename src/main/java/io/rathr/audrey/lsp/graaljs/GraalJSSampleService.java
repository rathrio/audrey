package io.rathr.audrey.lsp.graaljs;

import com.oracle.js.parser.ScriptEnvironment;
import io.rathr.audrey.lsp.SampleService;
import io.rathr.audrey.storage.Sample;

import java.util.Set;

public class GraalJSSampleService implements SampleService {
    private static final ScriptEnvironment ENV = ScriptEnvironment.builder()
        .strict(false)
        .ecmaScriptVersion(6)
        .earlyLvalueError(false)
        .emptyStatements(true)
        .syntaxExtensions(false)
        .scripting(false)
        .shebang(true)
        .constAsVar(true)
        .build();


    @Override
    public void didOpen(final String uri) {
    }

    @Override
    public void didClose(final String uri) {
    }

    @Override
    public Set<Sample> filterSamples(final Set<Sample> samples, final String uri, final int line, final int column) {
        return null;
    }
}
