package org.lex.perf.engine;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * Created by Алексей on 10.05.2015.
 */
@State(Scope.Benchmark)
@Fork(1)
@BenchmarkMode({Mode.AverageTime, Mode.Throughput})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Threads(4)
@Warmup(iterations = 8)
@Measurement(iterations = 5)
public class BenchmarkTimeSlotTest {

    EngineImpl engine = new EngineImpl();

    final RrdCounter r = new RrdCounter(engine, "HTTP", true, true, new String[]{}, "http-req");

    @Benchmark
    @OperationsPerInvocation(1000)
    public void testGetSlotAndAddHit() {
        for (int i = 0; i < 1000; i++) {
            CounterTimeSlot ts = r.getTimeSlot(System.currentTimeMillis());
            ts.addHit(10);
        }
    }

    @Benchmark
    @OperationsPerInvocation(1000)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void testCTM() {
        for (int i = 0; i < 1000; i++) {
            System.currentTimeMillis();
        }
    }

/*
Benchmark                       Mode  Cnt   Score   Error  Units
BenchmarkTimeSlotTest.testNano  avgt    5  34,511 ? 1,049  ns/op
 */

    @Benchmark
    @OperationsPerInvocation(1000)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void testNano() {
        for (int i = 0; i < 1000; i++) {
            System.nanoTime();
        }
    }


    @Benchmark
    @OperationsPerInvocation(1000)
    public void testAddHit() {
        CounterTimeSlot ts = (CounterTimeSlot) r.getTimeSlot(System.currentTimeMillis());
        for (int i = 0; i < 1000; i++) {
            ts.addHit(10);
        }
    }

    @TearDown
    public void tearDown() {
        engine.shutdown();
    }


}


/*
Benchmark                                   Mode  Cnt    Score   Error  Units
BenchmarkTimeSlotTest.testAddHit            avgt    5  118,153 ? 1,422  ns/op
BenchmarkTimeSlotTest.testAssignment        avgt    5    0,966 ? 0,024  ns/op
BenchmarkTimeSlotTest.testCTM               avgt    5   10,296 ? 0,139  ns/op
BenchmarkTimeSlotTest.testGetSlotAndAddHit  avgt    5  183,894 ? 1,259  ns/op
BenchmarkTimeSlotTest.testNano              avgt    5   33,939 ? 0,487  ns/op




32 threads
Benchmark                        Mode  Cnt    Score   Error   Units
BenchmarkTimeSlotTest.testNano  thrpt    5  221,497 ? 8,845  ops/us
BenchmarkTimeSlotTest.testNano   avgt    5    0,140 ? 0,006   us/op

16 threads
Benchmark                        Mode  Cnt    Score   Error   Units
BenchmarkTimeSlotTest.testNano  thrpt    5  215,184 ? 9,895  ops/us
BenchmarkTimeSlotTest.testNano   avgt    5    0,077 ? 0,003   us/op


8threads
Benchmark                        Mode  Cnt    Score   Error   Units
BenchmarkTimeSlotTest.testNano  thrpt    5  196,993 ? 4,770  ops/us
BenchmarkTimeSlotTest.testNano   avgt    5    0,039 ? 0,001   us/op

4threads
Benchmark                        Mode  Cnt    Score    Error   Units
BenchmarkTimeSlotTest.testNano  thrpt    5  124,534 ?  0,723  ops/us
BenchmarkTimeSlotTest.testNano   avgt    5    0,027 ?  0,001   us/op


1 thread
Benchmark                        Mode  Cnt   Score    Error   Units
BenchmarkTimeSlotTest.testNano  thrpt    5  33,539 ?  0,056  ops/us
BenchmarkTimeSlotTest.testNano   avgt    5   0,028 ?  0,001   us/op
 */