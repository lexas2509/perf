package org.lex.perf.api.index;

/**
 * Интерфейса для индекса счетчика.
 */
public interface CounterIndex extends Index {
    /**
     * Регистрирует один "тик" счетчика, с длительностью duration
     *
     * @param duration - длительность "тика"
     */
    void addRequest(long requestTime, long duration);
}
