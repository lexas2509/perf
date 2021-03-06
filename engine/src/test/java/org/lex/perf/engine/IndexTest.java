package org.lex.perf.engine;

import org.lex.perf.api.factory.IndexFactory;
import org.lex.perf.api.factory.IndexType;
import org.testng.annotations.Test;

import static org.testng.Assert.fail;

/**
 */
public class IndexTest {
    /**
     * Серия индексов, отображающих потоковые данные по обращения к сервлетам
     */
    EngineImpl engine = new EngineImpl();

    @Test
    public void testGetTimeSlot() throws Exception {
        RrdCounter r = new RrdCounter(engine, "HTTP", true, true,  new String[]{}, "http-req");
        long start = System.currentTimeMillis();
        for (long i = start; i < start + 1000000; i++) {
            CounterTimeSlot ts = r.getTimeSlot(i);
            if (ts.getEndTime() > i && ts.getStartTime() <= i) {
                ts.addHit(9);
            } else {
                fail(Long.toString(i - start));
            }
        }
    }

    @Test
    public void testGetTimeSlot10000() throws Exception {
        RrdCounter r = new RrdCounter(engine, "HTTP", true, true, new String[]{}, "http-req");
        long start = System.currentTimeMillis();
        CounterTimeSlot ts1 = r.getTimeSlot(start);
        CounterTimeSlot ts = r.getTimeSlot(start + 10000);
        if (ts.getEndTime() > start + 10000 && ts.getStartTime() <= start + 10000) {

        } else {
            fail(Integer.toString(10000));
        }
    }

}
