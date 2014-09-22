package org.lex.perf.engine;

import org.apache.commons.codec.binary.Base64;
import org.lex.perf.api.factory.IndexFactory;
import org.lex.perf.api.factory.IndexSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
                    for (Index s : indexes) {
                        s.doSample(currentTime);
                    }
                } catch (Throwable t) {
                    LOGGER.error("error ", t);
                }
            }
        }, SAMPLE_DURATION - 1, SAMPLE_DURATION); // 10 sec

    }

    public void loadIndexesFromDisk(IndexSeries indexSeries) {
        String categoryPrefix = getCategoryPrefix(indexSeries);

        File[] files = new File(workingDirectory).listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.getName().startsWith(categoryPrefix)) {
                    continue;
                }

                String fileNamePart1 = file.getName().substring(categoryPrefix.length());


                int idx = fileNamePart1.indexOf(".rrd");
                if (idx == -1) {
                    continue;
                }


                String indexNamePart = fileNamePart1.substring(1, idx);
                String indexName = decodeIndexName(indexNamePart);
                IndexFactory.getFactory().getIndex(indexSeries, indexName);
            }
        }
    }

    public static String encodeIndexName(String indexName) {
        String encode = Base64.encodeBase64String(indexName.getBytes(Const.UTF8));
        encode = encode.replaceAll("==", "--");
        return encode;
    }

    public static String getCategoryPrefix(IndexSeries category) {
        return category.getIndexType().name().substring(0, 1) + "-" + category.getName();
    }

    public static String decodeIndexName(String fileName) {
        String partFileName = fileName.replaceAll("--", "==");
        String indexName = new String(Base64.decodeBase64(partFileName), Const.UTF8);
        return indexName;
    }

    private final List<Index> indexes = new ArrayList<Index>();

    public Object getWorkingDirectory() {
        return workingDirectory;
    }

    public void addIndex(Index index) {
        indexes.add(index);
    }
}
