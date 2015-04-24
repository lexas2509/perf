package org.lex.perf.impl;

import org.lex.perf.config.Config;
import org.lex.perf.util.JAXBUtil;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Created by Алексей on 20.04.2015.
 */
public class ConfigTest {
    @Test
    public void testSample001() {
        Config sample001 = JAXBUtil.getObject(IndexFactoryImpl.INDEX_FACTORY_CONFIG, "org/lex/perf/config/sample001.xml", Config.class);
        IndexFactoryImpl impl = new IndexFactoryImpl(sample001);

        int perfIdx = impl.getIndexSeriesIdx("PERF");
        int sqlIdx = impl.getIndexSeriesIdx("SQL");
        // SQL is first defined indexSeries and should have index 0
        assertEquals(sqlIdx, 0);

        // in this config  PERF is last. And should has max index (1)
        assertEquals(perfIdx, 1);

        int[][] mapsTo = impl.getNestedChildIndexes();
        // SQL indexSeries is first child in the PERF nested series.
        assertEquals(mapsTo[sqlIdx][perfIdx], 0);

        // PERF indexSeries is second child in the PERF nested series
        assertEquals(mapsTo[perfIdx][perfIdx], 1);

        // SQL isn't nested series in SQL
        assertEquals(mapsTo[sqlIdx][sqlIdx], -1);

        // PERF isnt' nested series in SQL
        assertEquals(mapsTo[perfIdx][sqlIdx], -1);
    }

    @Test
    public void testSample002() {
        Config sample002 = JAXBUtil.getObject(IndexFactoryImpl.INDEX_FACTORY_CONFIG, "org/lex/perf/config/sample002.xml", Config.class);
        IndexFactoryImpl impl = new IndexFactoryImpl(sample002);
        int sqlIdx = impl.getIndexSeriesIdx("SQL");
        int jmsIdx = impl.getIndexSeriesIdx("JMS");
        int perfIdx = impl.getIndexSeriesIdx("PERF");

        // SQL is first defined indexSeries and should have index 0
        assertEquals(sqlIdx, 0);

        // JMS is second defined indexSeries and should have index 1
        assertEquals(jmsIdx, 1);

        // in this config  PERF is last. And should have max index (2)
        assertEquals(perfIdx, 2);

        int[][] mapsTo = impl.getNestedChildIndexes();

        // SQL indexSeries is first child in the PERF nested series.
        assertEquals(mapsTo[sqlIdx][perfIdx], 0);

        // JMS indexSeries is second child in the PERF nested series.
        assertEquals(mapsTo[jmsIdx][perfIdx], 1);

        // PERF indexSeries is third child in the PERF nested series
        assertEquals(mapsTo[perfIdx][perfIdx], 2);

        // SQL isn't nested series in SQL
        assertEquals(mapsTo[sqlIdx][sqlIdx], -1);

        // JMS isn't nested series in JMS
        assertEquals(mapsTo[jmsIdx][jmsIdx], -1);

        // SQL isn't nested series in JMS
        assertEquals(mapsTo[sqlIdx][jmsIdx], -1);

        // JMS isn't nested series in SQL
        assertEquals(mapsTo[jmsIdx][sqlIdx], -1);

        // PERF isn't nested series in JMS
        assertEquals(mapsTo[perfIdx][jmsIdx], -1);

        // PERF isn't nested series in SQL
        assertEquals(mapsTo[perfIdx][sqlIdx], -1);
    }

    @Test
    public void testSample003() {
        Config sample002 = JAXBUtil.getObject(IndexFactoryImpl.INDEX_FACTORY_CONFIG, "org/lex/perf/config/sample003.xml", Config.class);
        IndexFactoryImpl impl = new IndexFactoryImpl(sample002);
        int sqlIdx = impl.getIndexSeriesIdx("SQL");
        int jmsIdx = impl.getIndexSeriesIdx("JMS");
        int perfIdx = impl.getIndexSeriesIdx("PERF");

        // SQL is first defined indexSeries and should have index 0
        assertEquals(sqlIdx, 0);

        // JMS is second defined indexSeries and should have index 1
        assertEquals(jmsIdx, 1);

        // in this config  PERF is last. And should have max index (2)
        assertEquals(perfIdx, 3);

        int[][] mapsTo = impl.getNestedChildIndexes();

        // SQL indexSeries is mapped to EXTERNAL wich is first child in the PERF nested series.
        assertEquals(mapsTo[sqlIdx][perfIdx], 0);

        // JMS indexSeries is mapped to EXTERNAL wich is first child in the PERF nested series.
        assertEquals(mapsTo[jmsIdx][perfIdx], 0);

        // PERF indexSeries is third child in the PERF nested series
        assertEquals(mapsTo[perfIdx][perfIdx], 1);

        // SQL isn't nested series in SQL
        assertEquals(mapsTo[sqlIdx][sqlIdx], -1);

        // JMS isn't nested series in JMS
        assertEquals(mapsTo[jmsIdx][jmsIdx], -1);

        // SQL isn't nested series in JMS
        assertEquals(mapsTo[sqlIdx][jmsIdx], -1);

        // JMS isn't nested series in SQL
        assertEquals(mapsTo[jmsIdx][sqlIdx], -1);

        // PERF isn't nested series in JMS
        assertEquals(mapsTo[perfIdx][jmsIdx], -1);

        // PERF isn't nested series in SQL
        assertEquals(mapsTo[perfIdx][sqlIdx], -1);
    }
}
