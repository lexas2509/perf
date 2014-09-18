package org.lex.perf.api.index;

/**
 * Created by Алексей on 18.09.2014.
 */
public interface CPUCounterIndex extends Index {
    /**
     * Регистрирует один "тик" счетчика, с длительностью duration
     *
     * @param duration - длительность "тика"
     */
    void addRequest(long requestTime, long duration, long cpuDuration);

}
