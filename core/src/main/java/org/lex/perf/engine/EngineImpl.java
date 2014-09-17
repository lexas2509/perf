package org.lex.perf.engine;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.lex.perf.api.factory.IndexFactory;
import org.lex.perf.api.factory.IndexSeries;
import org.lex.perf.engine.event.MonitoringEvent;
import org.lex.perf.engine.event.MonitoringValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 */
public class EngineImpl implements Engine {

    private final static Logger LOGGER = LoggerFactory.getLogger(EngineImpl.class);


    public static final int SAMPLE_DURATION = 5 * 1000; // 5 sec in ms

    public static final int MINUTE = 60;

    public static final int HOUR = 60 * MINUTE;

    public static final int DAY = 24 * HOUR;

    public static final int WEEK = 7 * DAY;

    private Timer timer = new Timer(); // Timer to gather samples from index

    private String workingDirectory = "e:/mondata/";

    public EngineImpl() {
        // start timer
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    long currentTime = System.currentTimeMillis();
                    for (Map<String, Index> entry : indexes.values()) {
                        for (Index s : entry.values()) {
                            s.doSample(currentTime);
                        }
                    }
                } catch (Throwable t) {
                    LOGGER.error("error ", t);
                }
            }
        }, SAMPLE_DURATION - 1, SAMPLE_DURATION); // 10 sec

        //load indexes from disk
        File[] files = new File(workingDirectory).listFiles();
        if (files != null) {
            for (File file : files) {
                String[] names = file.getName().split("-");

                if (names.length != 2) {
                    continue;
                }

                IndexSeries monitoringCategory = IndexFactory.getIndexSeries(names[0]);
                if (monitoringCategory == null) {
                    continue;
                }

                int idx = names[1].indexOf(".rrd");
                if (idx == -1) {
                    continue;
                }

                String indexName = names[1].substring(0, idx);
                indexName = decodeIndexName(indexName);
                getIndex(monitoringCategory, indexName);
            }
        }
    }

    public static String encodeIndexName(String indexName) {
        String encode = Base64.encode(indexName.getBytes(Const.UTF8));
        encode = encode.replaceAll("==", "--");
        return encode;
    }

    public static String decodeIndexName(String fileName) {
        String partFileName = fileName.replaceAll("--", "==");
        String indexName = new String(Base64.decode(partFileName), Const.UTF8);
        return indexName;
    }

    public void putEvent(MonitoringEvent event) {
        Counter counter = (Counter) (getIndex(event.category, event.item));
        CounterTimeSlot timeSlot = counter.getTimeSlot(event.eventTime);
        timeSlot.addHit(event.duration / 1000 / 1000);
    }

    private final Map<IndexSeries, Map<String, Index>> indexes = new ConcurrentHashMap<IndexSeries, Map<String, Index>>();

    public Index getIndex(IndexSeries category, String indexName) {
        Map<String, Index> categoryIndexes = indexes.get(category);
        if (categoryIndexes == null) {
            categoryIndexes = new ConcurrentHashMap<String, Index>();
            indexes.put(category, categoryIndexes);
        }
        Index result = categoryIndexes.get(indexName);
        if (result == null) {
            switch (category.getIndexType()) {
                case GAUGE:
                    result = new Gauge(this, category, indexName);
                    break;
                case COUNTER:
                    result = new Counter(this, category, indexName);
                    break;
                default:
                    break;
            }
            categoryIndexes.put(indexName, result);
        }
        return result;
    }


    public void putSensorValue(MonitoringValue event) {
        Gauge gauge = (Gauge) getIndex(event.category, event.item);
        GaugeTimeSlot timeSlot = gauge.getTimeSlot(event.eventTime);
        timeSlot.setValue(event.value);
    }

    public List<Index> getIndexes(IndexSeries category) {
        Map<String, Index> indexes = this.indexes.get(category);
        if (indexes == null) {
            return Collections.emptyList();
        }
        return new ArrayList<Index>(indexes.values());
    }

    public Object getWorkingDirectory() {
        return workingDirectory;
    }
}
