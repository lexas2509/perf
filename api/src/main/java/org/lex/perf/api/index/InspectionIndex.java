package org.lex.perf.api.index;

/**
 * Created by Алексей on 02.09.2014.
 */
public interface InspectionIndex extends Index {
    void bindContext();

    void unBindContext();
}
