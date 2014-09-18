package org.lex.perf.api.index;

/**
 * Интерфейса для индекса счетчика.
 */
public interface CounterIndex extends Index {
    /**
     * Регистрирует один "тик" счетчика, с длительностью duration
     *
     * @param durations - параметры, ассоциированные с данным "тиком"
     */
    void addRequest(long requestTime, long[] durations);
}
