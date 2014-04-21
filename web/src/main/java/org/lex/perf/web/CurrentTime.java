package org.lex.perf.web;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 */
public class CurrentTime extends ResourceHttpItem {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    @Override
    protected InputStream getContent() {
        long time = System.currentTimeMillis() / 1000;
        String content = Long.toString(time);
        return new ByteArrayInputStream(content.getBytes(UTF8));
    }
}
