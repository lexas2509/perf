package org.lex.perf.common;

import org.lex.perf.api.CategoryType;
import org.lex.perf.api.MonitorCategory;
import org.lex.perf.engine.MonitorCategoryImpl;

/**
 * Created with IntelliJ IDEA.
 * User: lexas
 * Date: 26.06.14
 * Time: 20:31
 * To change this template use File | Settings | File Templates.
 */
public class StandardCategory {
    public static final MonitorCategory HTTP = new MonitorCategoryImpl("HTTP", CategoryType.COUNTER);
    public static final MonitorCategory JVM = new MonitorCategoryImpl("JVM", CategoryType.GAUGE);
    public static final MonitorCategory GLOBAL = new MonitorCategoryImpl("GLOBAL", CategoryType.COUNTER);
    public static final MonitorCategory SQL = new MonitorCategoryImpl("SQL", CategoryType.COUNTER);
}
