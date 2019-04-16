package io.rathr.audrey.tmp;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.Scope;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.EventContext;
import com.oracle.truffle.api.instrumentation.ExecutionEventNode;
import com.oracle.truffle.api.instrumentation.SourceFilter;
import com.oracle.truffle.api.instrumentation.SourceSectionFilter;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.instrumentation.TruffleInstrument;
import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.Message;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Instrument;
import org.graalvm.polyglot.Source;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;

public class Main {
    static class DemoNode extends ExecutionEventNode {
        private final TruffleInstrument.Env env;
        private final EventContext context;

        public DemoNode(final TruffleInstrument.Env env, final EventContext context) {
            this.env = env;
            this.context = context;
        }

        @Override
        protected void onEnter(final VirtualFrame frame) {
            handleOnEnter(frame.materialize());
        }

        @CompilerDirectives.TruffleBoundary
        private void handleOnEnter(final MaterializedFrame frame) {
            final Iterator<Scope> scopeIterator = env.findLocalScopes(context.getInstrumentedNode(), frame).iterator();
            if (!scopeIterator.hasNext()) {
                return;
            }

            final Scope scope = scopeIterator.next();
            final TruffleObject arguments = (TruffleObject) scope.getArguments();

            try {
                ForeignAccess.sendKeys(Message.KEYS.createNode(), arguments);
            } catch (UnsupportedMessageException e) {
                e.printStackTrace();
            }
        }
    }

    static class Demo {
        private final TruffleInstrument.Env env;

        public Demo(final TruffleInstrument.Env env) {
            this.env = env;
        }

        public static Demo find(Engine engine) {
            return DemoInstrument.getDemo(engine);
        }

        public void enable() {
            final SourceFilter sourceFilter = SourceFilter.newBuilder().includeInternal(false).build();
            final SourceSectionFilter sourceSectionFilter = SourceSectionFilter.newBuilder()
                .sourceFilter(sourceFilter)
                .tagIs(StandardTags.RootTag.class)
                .build();

            env.getInstrumenter().attachExecutionEventFactory(
                sourceSectionFilter,
                context -> new DemoNode(env, context)
            );
        }
    }

    @TruffleInstrument.Registration(id = DemoInstrument.ID, name = "Demo Instrument", services = {Demo.class})
    public static class DemoInstrument extends TruffleInstrument {
        public static final String ID = "demo-instrument";
        private Demo demo;

        public static Demo getDemo(Engine engine) {
            Instrument instrument = engine.getInstruments().get(ID);
            if (instrument == null) {
                throw new IllegalStateException("Demo is not installed.");
            }

            final Demo demo = instrument.lookup(Demo.class);
            return demo;
        }

        @Override
        protected void onCreate(final Env env) {
            demo = new Demo(env);
            demo.enable();
            env.registerService(demo);
        }
    }

    public static void main(String[] args) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ByteArrayOutputStream err = new ByteArrayOutputStream();

        final Context context = Context.newBuilder()
            .in(System.in)
            .out(out)
            .err(err)
            .allowIO(true)
            .allowNativeAccess(true)
            .build();

        final Demo demo = Demo.find(context.getEngine());
        assert demo != null;

        final Source jsSource = makeSource("function add(x, y) { return x + y }", "js");
        context.eval(jsSource);

        final Source rubySource = makeSource("def add(x, y); x + y; end", "ruby");
        context.eval(rubySource);
    }

    private static Source makeSource(String source, String languageId) {
        return Source.newBuilder(languageId, source, "test").buildLiteral();
    }
}
