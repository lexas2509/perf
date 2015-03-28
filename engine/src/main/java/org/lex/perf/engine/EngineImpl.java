package org.lex.perf.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 */
public class EngineImpl implements Engine {

    private final static Logger LOGGER = LoggerFactory.getLogger(EngineImpl.class);

    public static final int SAMPLE_DURATION = 15 * 1000; // 15 sec in ms

    public static final int MINUTE = 60;

    public static final int HOUR = 60 * MINUTE;

    public static final int DAY = 24 * HOUR;

    public static final int WEEK = 7 * DAY;

    private Timer timer = new Timer(); // Timer to gather data (samples) from state index

    private String workingDirectory = "e:/mondata/";

    private HashMap<String, HashMap<String, String>> indexFileNames = new HashMap<String, HashMap<String, String>>();

    private boolean changed = false;

    public EngineImpl() {
        // start timer
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    long currentTime = System.currentTimeMillis();
                    List<Index> indexList;
                    synchronized (indexes) {
                        indexList = new ArrayList<Index>(indexes);
                    }
                    for (Index s : indexList) {
                        try {
                            s.doSample(currentTime);
                        } catch (Throwable t) {
                            LOGGER.error("error ", t);
                        }
                    }
                    saveIndexFileNames();
                } catch (Throwable t) {
                    LOGGER.error("error ", t);
                }

            }
        }, SAMPLE_DURATION - 1, SAMPLE_DURATION);
        readIndexFileNames();
    }

    public List<String> loadIndexesFromDisk(String categoryPrefix, String categoryName) {
        List<String> loadedIndexes = new ArrayList<String>();
        File[] files = new File(workingDirectory).listFiles();
        if (files != null) {
            for (File file : files) {
                String name = file.getName();

                if (!name.startsWith(categoryPrefix)) {
                    continue;
                }

                String fileNamePart1 = name.substring(categoryPrefix.length());


                int idx = fileNamePart1.indexOf(".rrd");
                if (idx == -1) {
                    continue;
                }


                String indexNamePart = fileNamePart1.substring(1, idx);
                String indexName = getFileIndexName(categoryName, indexNamePart);
                if (indexName != null) {
                    loadedIndexes.add(indexName);
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

    private final List<Index> indexes = new ArrayList<Index>();

    public Object getWorkingDirectory() {
        return workingDirectory;
    }

    public void addIndex(Index index) {
        synchronized (indexes) {
            indexes.add(index);
        }
    }
}

