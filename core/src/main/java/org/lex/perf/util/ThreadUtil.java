package org.lex.perf.util;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * Created by Алексей on 18.09.2014.
 */
public class ThreadUtil {

    private static final ThreadMXBean THREAD_BEAN = ManagementFactory.getThreadMXBean();

    private static final boolean CPU_TIME_ENABLED = THREAD_BEAN.isThreadCpuTimeSupported()
            && THREAD_BEAN.isThreadCpuTimeEnabled();

    public static long getCurrentThreadCpuTime() {
        return getThreadCpuTime(Thread.currentThread().getId());
    }

    public static long getThreadCpuTime(long threadId) {
        if (CPU_TIME_ENABLED) {
            return THREAD_BEAN.getThreadCpuTime(threadId);
        }
        return 0;
    }

}
