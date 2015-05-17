package org.lex.perf.engine;

import com.lmax.disruptor.EventHandler;
import org.lex.perf.api.factory.IndexType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 */
public class EngineImpl implements Engine {

    private final static Logger LOGGER = LoggerFactory.getLogger(EngineImpl.class);

    public static final int SAMPLE_DURATION = 15 * 1000; // 15 sec in ms

    public static final int MINUTE = 60;

    public static final int HOUR = 60 * MINUTE;

    public static final int DAY = 24 * HOUR;

    public static final int WEEK = 7 * DAY;


    private ScheduledThreadPoolExecutor timer = new ScheduledThreadPoolExecutor(2); // Timer to gather data (samples) from state index

    private String workingDirectory = "e:/mondata/";

    private HashMap<String, HashMap<String, String>> indexFileNames = new HashMap<String, HashMap<String, String>>();

    private boolean changed = false;

    private int slotDuration = SAMPLE_DURATION;

    public EngineImpl() {
        // start timer
        timer.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    long currentTime = System.currentTimeMillis();
                    List<RrdIndex> rrdIndexList;
                    synchronized (rrdIndexes) {
                        rrdIndexList = new ArrayList<RrdIndex>(rrdIndexes);
                    }
                    for (RrdIndex s : rrdIndexList) {
                        try {
                            s.doSample(currentTime);
                        } catch (Exception t) {
                            LOGGER.error("error ", t);
                        }
                    }
                    saveIndexFileNames();
                } catch (Exception t) {
                    LOGGER.error("error ", t);
                }

            }
        }, SAMPLE_DURATION - 1, SAMPLE_DURATION, TimeUnit.MILLISECONDS);

        readIndexFileNames();
    }

    public void shutdown() {
        timer.shutdown();
    }

    private static Map<String, IndexType> indexTypePrefixes = new HashMap<String, IndexType>();
    private static Map<IndexType, String> indexTypePrefixesReverse = new HashMap<IndexType, String>();

    {
        indexTypePrefixes.put("G", IndexType.GAUGE);
        indexTypePrefixes.put("I", IndexType.INSPECTION);
        indexTypePrefixes.put("C", IndexType.COUNTER);

        indexTypePrefixesReverse.put(IndexType.GAUGE, "G");
        indexTypePrefixesReverse.put(IndexType.INSPECTION, "I");
        indexTypePrefixesReverse.put(IndexType.COUNTER, "C");
    }

    public Map<String, IndexType> loadIndexes(String categoryName) {
        Map<String, IndexType> loadedIndexes = new HashMap<String, IndexType>();
        File[] files = new File(workingDirectory).listFiles();
        if (files != null) {
            for (File file : files) {


                String name = file.getName();

                String prefix = name.substring(0, 1);
                if (!indexTypePrefixes.containsKey(prefix)) {
                    continue;
                }

                IndexType indexType = indexTypePrefixes.get(prefix);

                String fileNamePart1 = name.substring(prefix.length());


                int idx = fileNamePart1.indexOf(".rrd");
                if (idx == -1) {
                    continue;
                }


                String indexNamePart = fileNamePart1.substring(1, idx);
                String indexName = getFileIndexName(categoryName, indexNamePart);
                if (indexName != null) {
                    loadedIndexes.put(indexName, indexType);
                }
            }
        }
        return loadedIndexes;
    }


    private synchronized void saveIndexFileNames() {
        if (changed) {
            try {
                FileOutputStream fos = new FileOutputStream(workingDirectory + "/" + "indexes");
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(indexFileNames);
                oos.close();
                fos.close();
            } catch (IOException e) {
                LOGGER.warn("", e);
            }
            changed = false;
        }
    }

    private synchronized void readIndexFileNames() {
        try {
            FileInputStream fis = new FileInputStream(workingDirectory + "/" + "indexes");
            try {
                ObjectInputStream ois = new ObjectInputStream(fis);
                indexFileNames = (HashMap) ois.readObject();
                ois.close();
            } finally {
                fis.close();
            }
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("", e);
            }
            LOGGER.warn(e.getMessage());
        }
        changed = false;
    }


    public synchronized String getFileIndexName(String categoryName, String fileName) {
        HashMap<String, String> categoryIndexes = indexFileNames.get(categoryName);
        if (categoryIndexes == null) {
            return null;
        }
        for (Map.Entry<String, String> entry : categoryIndexes.entrySet()) {
            if (entry.getValue().equals(fileName)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private final List<RrdIndex> rrdIndexes = new ArrayList<RrdIndex>();

    public Object getWorkingDirectory() {
        return workingDirectory;
    }

    public void addIndex(RrdIndex rrdIndex) {
        synchronized (rrdIndexes) {
            rrdIndexes.add(rrdIndex);
        }
    }

    public EventHandler<IndexEvent> getHandler() {
        return new EventHandler<IndexEvent>() {
            public void onEvent(final IndexEvent event, final long sequence, final boolean endOfBatch) throws Exception {
                if (event.engineIndex instanceof RrdCounter) {
                    RrdCounter rrdCounter = (RrdCounter) event.engineIndex;
                    CounterTimeSlot timeSlot = rrdCounter.getTimeSlot(event.requestTime);
                    timeSlot.addHitWithChild(event.own, event.childsDurations);
                } else if (event.engineIndex instanceof RrdGauge) {
                    RrdGauge rrdGauge = (RrdGauge) event.engineIndex;
                    GaugeTimeSlot timeSlot = rrdGauge.getTimeSlot(event.requestTime);
                    timeSlot.setValue(event.value.doubleValue());
                }
            }
        };
    }

    @Override
    public EngineIndex getCounter(String indexName, String indexSeriesName, IndexType indexType, boolean supportCPU, boolean supportHistogramm, String[] childSeries) {
        String fileName = getFileName(indexName, indexType, indexSeriesName);
        RrdCounter rrdCounter = new RrdCounter(this, indexName, supportCPU, supportHistogramm, childSeries, fileName);
        rrdCounter.init();
        return rrdCounter;
    }

    @Override
    public EngineIndex getGauge(String indexName, String indexSeriesName) {
        String fileName = getFileName(indexName, IndexType.GAUGE, indexSeriesName);
        RrdGauge rrdGauge = new RrdGauge(this, indexName, fileName);
        rrdGauge.init();
        return rrdGauge;
    }

    protected String getFileName(String indexName, IndexType indexType, String indexSeriesName) {
        return getIndexTypePrefix(indexType) + "-" + getIndexFileName(indexSeriesName, indexName);
    }


    public synchronized String getIndexFileName(String categoryName, String indexName) {
        HashMap<String, String> categoryIndexes = indexFileNames.get(categoryName);
        if (categoryIndexes == null) {
            categoryIndexes = new HashMap<String, String>();
            indexFileNames.put(categoryName, categoryIndexes);
            changed = true;
        }
        String fileName = categoryIndexes.get(indexName);
        if (fileName == null) {
            fileName = UUID.randomUUID().toString();
            categoryIndexes.put(indexName, fileName);
            changed = true;
        }
        return fileName;
    }

    private String getIndexTypePrefix(IndexType indexType) {
        return indexTypePrefixesReverse.get(indexType);
    }

    public int getSlotDuration() {
        return slotDuration;
    }

}