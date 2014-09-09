package org.lex.perf.api.index;

import org.lex.perf.api.factory.IndexSeries;
import org.lex.perf.api.factory.IndexType;

/**
 * Общий интерфейс для индексов
 */
public interface Index {
    /**
     * Возвращает активен ли датчик
     */
    boolean isActive();

    /**
     * Возвращает тип датчика
     */
    IndexType getIndexType();

    IndexSeries getIndexSeries();
}
