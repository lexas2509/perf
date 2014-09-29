package org.lex.engine;

import org.lex.perf.engine.CounterTimeSlot;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 */
public class CounterTimeSlotTest {

    @Test
    public void testGistogramm() {
        CounterTimeSlot r = new CounterTimeSlot(10, 20, false, true, new String[]{});
        r.addHit(0, 0); // it' [0;1] - first slot
        assertEquals(r.getStatCount(0), 1);
        assertEquals(r.getStatCount(1), 0);

        r.addHit(1, 0); // it's [0;1] - first slot
        assertEquals(r.getStatCount(0), 2);
        assertEquals(r.getStatCount(1), 0);

        r.addHit(2, 0); // it's (1;2] - second slot
        assertEquals(r.getStatCount(0), 2);
        assertEquals(r.getStatCount(1), 1);

        r.addHit(3, 0); // it's (2;4] - third slot
        assertEquals(r.getStatCount(0), 2);
        assertEquals(r.getStatCount(1), 1);
        assertEquals(r.getStatCount(2), 1);

        r.addHit(4, 0); // it's (2;4] - third slot
        assertEquals(r.getStatCount(0), 2);
        assertEquals(r.getStatCount(1), 1);
        assertEquals(r.getStatCount(2), 2);

        r.addHit(5, 0); // it's (4;8] - fourth slot
        assertEquals(r.getStatCount(0), 2);
        assertEquals(r.getStatCount(1), 1);
        assertEquals(r.getStatCount(2), 2);
        assertEquals(r.getStatCount(3), 1);


    }
}
