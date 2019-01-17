package io.rathr.audrey.instrumentation;

import com.oracle.truffle.api.Option;
import org.graalvm.options.OptionCategory;
import org.graalvm.options.OptionKey;

@Option.Group(AudreyInstrument.ID)
public class AudreyCLI {

    static final String currentDir() {
        return System.getProperty("user.dir");
    }

    @Option(name = "", help = "Enable Audrey (default: false).", category = OptionCategory.USER)
    static final OptionKey<Boolean> ENABLED = new OptionKey<>(false);

    @Option(name = "Project", help = "Unique project ID.", category = OptionCategory.USER)
    static final OptionKey<String> PROJECT = new OptionKey<>("");

    @Option(name = "FilterPath", help = "Only instrument files that match this path.", category = OptionCategory.USER)
    static final OptionKey<String> FILTER_PATH = new OptionKey<>("");

    @Option(name = "Storage", help = "Use 'in_memory' or 'redis' as storage (default: redis).", category = OptionCategory.USER)
    static final OptionKey<String> STORAGE = new OptionKey<>("redis");

    @Option(name = "Sample", help = "Use 'random', 'temporal', 'all', or 'none' as a sampling strategy (default: all).", category = OptionCategory.USER)
    static final OptionKey<String> SAMPLE = new OptionKey<>("all");

    @Option(name = "RootPath", help = "Absolute project root path. (default: current directory)", category = OptionCategory.USER)
    static final OptionKey<String> ROOT_PATH = new OptionKey<>(currentDir());
}
