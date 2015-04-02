package org.lex.perf.engine;

import java.math.BigDecimal;

/**
* Created by Алексей on 29.03.2015.
*/
public class IndexEvent {

    public EngineIndex engineIndex;

    public long requestTime;

    public BigDecimal value;

    public final Duration own = new Duration();

    public final Duration[] childsDurations;

    public IndexEvent(int childsCount) {
        this.childsDurations = new Duration[childsCount];
        for (int i = 0; i < childsCount; i++) {
            childsDurations[i] = new Duration();
        }
    }
}
