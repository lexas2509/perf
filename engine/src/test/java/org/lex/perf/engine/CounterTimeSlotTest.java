package org.lex.perf.engine;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 */
public class CounterTimeSlotTest {

    @Test
    public void testHistogramm() {
        CounterTimeSlot r = new CounterTimeSlot(10, 20, false, true, new String[]{});
        r.addHit(0); // it' [0;1] - first slot
        assertEquals(r.getStatCount(0), 1);
        assertEquals(r.getStatCount(1), 0);

        r.addHit(1); // it's [0;1] - first slot
        assertEquals(r.getStatCount(0), 2);
        assertEquals(r.getStatCount(1), 0);

        r.addHit(2); // it's (1;2] - second slot
        assertEquals(r.getStatCount(0), 2);
        assertEquals(r.getStatCount(1), 1);

        r.addHit(3); // it's (2;4] - third slot
        assertEquals(r.getStatCount(0), 2);
        assertEquals(r.getStatCount(1), 1);
        assertEquals(r.getStatCount(2), 1);

        r.addHit(4); // it's (2;4] - third slot
        assertEquals(r.getStatCount(0), 2);
        assertEquals(r.getStatCount(1), 1);
        assertEquals(r.getStatCount(2), 2);

        r.addHit(5); // it's (4;8] - fourth slot
        assertEquals(r.getStatCount(0), 2);
        assertEquals(r.getStatCount(1), 1);
        assertEquals(r.getStatCount(2), 2);
        assertEquals(r.getStatCount(3), 1);


    }
}
