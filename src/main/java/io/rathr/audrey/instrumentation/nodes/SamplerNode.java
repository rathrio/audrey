package io.rathr.audrey.instrumentation.nodes;

import com.oracle.truffle.api.instrumentation.EventContext;
import com.oracle.truffle.api.instrumentation.ExecutionEventNode;
import com.oracle.truffle.api.instrumentation.TruffleInstrument;
import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.Message;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.nodes.LanguageInfo;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.SourceSection;
import io.rathr.audrey.instrumentation.Audrey;
import io.rathr.audrey.instrumentation.InstrumentationContext;
import io.rathr.audrey.storage.Project;
import io.rathr.audrey.storage.SampleStorage;

public abstract class SamplerNode extends ExecutionEventNode {
    private static final Node READ_NODE = Message.READ.createNode();
    private static final Node KEYS_NODE = Message.KEYS.createNode();

    /**
     * Sample instead of extracting everything.
     */
    static boolean SAMPLING_ENABLED = false;
    static int SAMPLING_RATE = 10;
    static int MAX_EXTRACTIONS = 100;

    protected static final String[] IDENTIFIER_BLACKLIST = {"(self)", "rubytruffle_temp", "this"};

    protected final Audrey audrey;
    protected final EventContext context;
    protected final TruffleInstrument.Env env;
    protected final Project project;
    protected final SampleStorage storage;
    protected final InstrumentationContext instrumentationContext;
    protected final SourceSection sourceSection;
    protected final int sourceSectionId;
    protected final Node instrumentedNode;
    protected final String languageId;
    protected final String rootNodeId;
    protected final LanguageInfo languageInfo;

    /**
     * The amount of times we decided to start the extraction process for this sourceSection.
     */
    protected int entered;

    /**
     * The amount of times we actually extracted samples for this sourceSection.
     */
    protected int extractions;

    public SamplerNode(final Audrey audrey,
                       final EventContext context,
                       final TruffleInstrument.Env env,
                       final Project project,
                       final SampleStorage storage,
                       final InstrumentationContext instrumentationContext) {

        this.audrey = audrey;
        this.context = context;
        this.env = env;
        this.project = project;
        this.storage = storage;
        this.instrumentationContext = instrumentationContext;
        this.sourceSection = context.getInstrumentedSourceSection();
        this.sourceSectionId = this.sourceSection.hashCode();
        this.instrumentedNode = context.getInstrumentedNode();
        this.languageId = sourceSection.getSource().getLanguage();
        this.rootNodeId = extractRootName(this.instrumentedNode);
        this.languageInfo = getLanguageInfo(languageId);
    }

    protected String extractRootName(final Node instrumentedNode) {
        RootNode rootNode = instrumentedNode.getRootNode();

        if (rootNode != null) {
            if (rootNode.getName() == null) {
                return rootNode.toString();
            } else {
                return rootNode.getName();
            }
        } else {
            return "<Unknown>";
        }
    }

    protected LanguageInfo getLanguageInfo(String languageId) {
        return env.getLanguages().get(languageId);
    }

    protected int getSize(final TruffleObject keys) throws UnsupportedMessageException {
        return ((Number) ForeignAccess.sendGetSize(Message.GET_SIZE.createNode(), keys)).intValue();
    }

    protected TruffleObject getKeys(final TruffleObject variables) throws UnsupportedMessageException {
        return ForeignAccess.sendKeys(KEYS_NODE, variables);
    }

    protected Object read(final TruffleObject object, final Object identifier) throws UnsupportedMessageException,
        UnknownIdentifierException {
        return ForeignAccess.sendRead(READ_NODE, object, identifier);
    }

    /**
     * @return guest language string representation of object.
     */
    protected String getString(Object object) {
        if (isSimple(object)) {
            return object.toString();
        }

        return env.toString(languageInfo, object);
    }

    protected Object getMetaObject(Object object) {
        return env.findMetaObject(languageInfo, object);
    }

    protected boolean isSimple(Object object) {
        return object instanceof String
            || object instanceof Integer
            || object instanceof Double
            || object instanceof Boolean;
    }

    enum FirstStatementState {
        looking,
        isFirst,
        isNotFirst
    }
}
