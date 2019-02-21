package io.rathr.audrey.instrumentation;

public final class InstrumentationContext {
    private boolean lookingForFirstStatement = false;

    public boolean isLookingForFirstStatement() {
        return lookingForFirstStatement;
    }

    public void setLookingForFirstStatement(final boolean flag) {
        this.lookingForFirstStatement = flag;
    }
}
