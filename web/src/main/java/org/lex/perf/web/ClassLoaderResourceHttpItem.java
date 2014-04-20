package org.lex.perf.web;

import java.io.InputStream;

/**
 */
public class ClassLoaderResourceHttpItem extends ResourceHttpItem {

    protected final String resource;


    public ClassLoader classLoader;

    public ClassLoaderResourceHttpItem(String resource) {
        super();
        this.resource = resource;
        this.classLoader = this.getClass().getClassLoader();
    }

    public ClassLoaderResourceHttpItem(String resource, ClassLoader cl) {
        super();
        this.resource = resource;
        this.classLoader = cl;
    }

    @Override
    protected InputStream getContent() {
        return classLoader.getResourceAsStream(resource);
    }
}
