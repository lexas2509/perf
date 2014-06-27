package org.lex.perf.engine;

import org.lex.perf.api.CategoryType;
import org.lex.perf.api.MonitorCategory;
import org.lex.perf.api.MonitorFactory;

/**
 */
public class MonitorCategoryImpl implements MonitorCategory {

    private final String name;

    private final CategoryType categoryType;

    private final MonitorFactoryImpl mf;

    public MonitorCategoryImpl(String name, CategoryType categoryType) {
        this.name = name;
        this.categoryType = categoryType;
        mf = (MonitorFactoryImpl) MonitorFactory.getFactory();
        mf.registerMonitorCategory(name, this);
    }

    public String getName() {
        return name;
    }

    public CategoryType getCategoryType() {
        return categoryType;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void addRequest(String indexName, long duration) {
        org.lex.perf.api.Index index = mf.getIndex(this, indexName);
        index.addRequest(duration);


    }

    @Override
    public void bindContext(String contextName) {
        // not implemented yet. Currently it doesn't support contexts.
    }
}
