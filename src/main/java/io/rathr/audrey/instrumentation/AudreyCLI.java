package io.rathr.audrey.instrumentation;

import com.oracle.truffle.api.Option;
import org.graalvm.options.OptionCategory;
import org.graalvm.options.OptionKey;

@Option.Group(AudreyInstrument.ID)
public class AudreyCLI {

    @Option(name = "", help = "Enable Audrey (default: false).", category = OptionCategory.USER)
    static final OptionKey<Boolean> ENABLED = new OptionKey<>(false);

    @Option(name = "FilterPath", help = "Only instrument files that match this path.", category = OptionCategory.USER)
    static final OptionKey<String> FILTER_PATH = new OptionKey<>("");

    @Option(name = "OutputPath", help = "Where to output samples (default: .audrey in the current directory).", category = OptionCategory.USER)
    static final OptionKey<String> OUTPUT_PATH = new OptionKey<>(".audrey");
}
