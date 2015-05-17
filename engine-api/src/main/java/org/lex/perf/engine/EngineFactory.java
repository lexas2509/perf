package org.lex.perf.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Алексей on 29.03.2015.
 */
public class EngineFactory {
    private final static Logger LOGGER = LoggerFactory.getLogger(EngineFactory.class);

    private static Engine engineImpl;

    private static void initFactory() {
        Class cl;
        try {
            cl = EngineFactory.class.getClassLoader().loadClass("org.lex.perf.engine.EngineImpl");
            engineImpl = (Engine) cl.newInstance();
        } catch (Exception e) {
            LOGGER.error("Can't instantiate IndexFactory", e);
            throw new RuntimeException(e);
        }
    }

    static {
        initFactory();
    }

    public static Engine getEngine() {
        return engineImpl;
    }
}
