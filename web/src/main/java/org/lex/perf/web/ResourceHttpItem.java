package org.lex.perf.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 */
public abstract class ResourceHttpItem implements HttpItem {

    private static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int len = in.read(buffer);
        while (len != -1) {
            out.write(buffer, 0, len);
            len = in.read(buffer);
        }
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse response) throws IOException {
        try {
            response.setStatus(HttpServletResponse.SC_OK);
            OutputStream os = response.getOutputStream();
            InputStream resourceStream = getContent();
            copyStream(resourceStream, os);
            os.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract InputStream getContent();
}
