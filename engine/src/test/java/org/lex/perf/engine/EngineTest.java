package org.lex.perf.engine;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/**
 * Created by Алексей on 13.05.2015.
 */
public class EngineTest {

    EngineImpl engine = new EngineImpl();


    @Test
    public void testTime() {
    }

    @AfterClass
    public void tearDown() {
        engine.shutdown();
    }
}
