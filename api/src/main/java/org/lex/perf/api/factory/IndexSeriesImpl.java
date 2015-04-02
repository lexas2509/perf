package org.lex.perf.api.factory;

/**
 */
public abstract class IndexSeriesImpl {

    public static final String[] DEFAULT = new String[]{"total"};

    protected final String name;

    protected IndexSeriesImpl(String name) {
        this.name = name;
    }

    public abstract void bindContext(String contextName);

    public abstract void unBindContext();
}
