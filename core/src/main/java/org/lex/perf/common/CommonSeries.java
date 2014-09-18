package org.lex.perf.common;

import org.lex.perf.api.factory.IndexFactory;
import org.lex.perf.api.factory.IndexSeries;
import org.lex.perf.api.factory.IndexType;

/**
 * Стандартные серии индексов.
 */
public class CommonSeries {
    /**
     * Серия индексов, отображающих потоковые данные по обращения к сервлетам
     */
    public static final IndexSeries HTTP = IndexFactory.registerIndexSeries("HTTP", IndexType.COUNTER);


    /**
     * Серия индексов, отображащая запросы SQL
     */
    public static final IndexSeries SQL = IndexFactory.registerIndexSeries("SQL", IndexType.COUNTER);

    public static final IndexSeries GLOBAL = IndexFactory.registerIndexSeries("GLOBAL", IndexType.COUNTER);
}
