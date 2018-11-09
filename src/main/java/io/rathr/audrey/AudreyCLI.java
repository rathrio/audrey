package io.rathr.audrey;

import com.oracle.truffle.api.Option;
import org.graalvm.options.OptionCategory;
import org.graalvm.options.OptionKey;

@Option.Group(AudreyInstrument.ID)
public class AudreyCLI {

    @Option(help = "Enable Audrey (default: false).", category = OptionCategory.USER)
    static final OptionKey<Boolean> ENABLED = new OptionKey<>(false);

}
