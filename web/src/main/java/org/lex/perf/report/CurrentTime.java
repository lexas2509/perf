package org.lex.perf.report;

import org.lex.perf.engine.Const;
import org.lex.perf.web.ResourceHttpItem;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 */
public class CurrentTime extends ResourceHttpItem {

    @Override
    protected InputStream getContent() {
        long time = System.currentTimeMillis() / 1000;
        String content = Long.toString(time);
        return new ByteArrayInputStream(content.getBytes(Const.UTF8));
    }
}
