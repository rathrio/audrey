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

    @Option(name = "EnableSampling", help = "Whether to sample instead of extracting everything (default: false)", category = OptionCategory.USER)
    static final OptionKey<Boolean> SAMPLING_ENABLED = new OptionKey<>(false);

    @Option(name = "SamplingStep", help = "How often to extract from a source section, e.g. 10 for every 10th time. Only considered when EnableSampling was passed (default: 10)", category = OptionCategory.USER)
    static final OptionKey<Integer> SAMPLING_STEP = new OptionKey<>(10);

    @Option(name = "EnableScheduling", help = "Enable temporal sharding scheduler (default: false)", category = OptionCategory.USER)
    static final OptionKey<Boolean> SCHEDULING_ENABLED = new OptionKey<>(false);

    @Option(name = "SchedulingInterval", help = "Interval in seconds after which the scheduler updates the source sections to instrument (default: 30)", category = OptionCategory.USER)
    static final OptionKey<Integer> SCHEDULING_INTERVAL = new OptionKey<>(30);

    @Option(name = "SchedulingBuckets", help = "How many buckets to distribute the nodes in for scheduling (default: 10)", category = OptionCategory.USER)
    static final OptionKey<Integer> SCHEDULING_BUCKETS = new OptionKey<>(10);

    @Option(name = "MaxExtractions", help = "After what amount of extractions to stop instrumenting a source section (default: 50)", category = OptionCategory.USER)
    static final OptionKey<Integer> MAX_EXTRACTIONS = new OptionKey<>(1000);

    @Option(name = "RootPath", help = "Absolute project root path. (default: current directory)", category = OptionCategory.USER)
    static final OptionKey<String> ROOT_PATH = new OptionKey<>(currentDir());

    @Option(name = "DumpFile", help = "Where to dump a JSON Array of the extracted samples.", category = OptionCategory.USER)
    static final OptionKey<String> DUMP_FILE = new OptionKey<>("");

    @Option(name = "RootOnly", help = "Try to extract samples from instrumenting root nodes only (default: false)", category = OptionCategory.USER)
    static final OptionKey<Boolean> ROOT_ONLY = new OptionKey<>(false);
}
