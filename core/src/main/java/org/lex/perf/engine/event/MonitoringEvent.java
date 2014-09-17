package org.lex.perf.engine.event;

import org.lex.perf.api.factory.IndexSeries;

/**
 */
public class MonitoringEvent {
    public IndexSeries category;
    public String item;
    public long eventTime;
    public long duration;
}

