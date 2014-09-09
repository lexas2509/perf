package org.lex.perf.api.index;

import java.math.BigDecimal;

/**
 * Интерфейс для индекса датчика. Вызывается подсистемой мониторинга.
 * Возвращает текущее значение датчика.
 */
public interface GaugeIndex extends Index {
    public BigDecimal[] getValues();

    String[] getItems();
}
