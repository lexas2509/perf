package org.lex.perf.common;

import org.lex.perf.api.factory.IndexSeries;
import org.lex.perf.api.factory.IndexType;

/**
 * Стандартные серии индексов.
 */
public class CommonSeries {
    /**
     * Серия индексов, отображающих потоковые данные по обращения к сервлетам
     */
    public static final IndexSeries HTTP = new IndexSeries("HTTP", IndexType.COUNTER);


    /**
     * Серия индексов, отображащая запросы SQL
     */
    public static final IndexSeries SQL = new IndexSeries("SQL", IndexType.COUNTER);

    public static final IndexSeries GLOBAL = new IndexSeries("GLOBAL", IndexType.COUNTER);
}
