import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Java 压测接口，用于开发过程中对接口进行快速压测
 * <p>
 * - version 1.0.0
 */
public class Tester {

    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    /**
     * 构造一个新的配置器
     *
     * @return 新的配置器实例
     */
    public static Configurer newConfigurer() {
        return new Configurer();
    }


    /**
     * 执行配置声明的测试内容
     *
     * @param configurer 本次测试的具体配置信息
     * @param runnable   本次测试测试的内容
     */
    public static void execute(Configurer configurer, Runnable runnable) {
        long start = System.currentTimeMillis();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            configurer.writeReport(String.format("Threads: %d, Loops: %d, Duration: %dms.", configurer.threads,
                    configurer.lastCounts, System.currentTimeMillis() - start));
            for (Map.Entry<Integer, AtomicLong> entry : configurer.rtts.entrySet()) {
                double rate = configurer.lastCounts == 0 || entry.getValue().get() == 0
                        ? 0.00
                        : (configurer.lastCounts - entry.getValue().get()) / (double) configurer.lastCounts * 100.00;
                configurer.writeReport(String.format("Request ratio less than %d ms : %09d - %02.02f%%", entry.getKey(),
                        configurer.lastCounts - entry.getValue().get(), rate));
            }
            configurer.writeReport(String.format("Max Time-consuming: %dus", configurer.maxTimeConsuming));
            configurer.writeReport(String.format("Min Time-consuming: %dus", configurer.minTimeConsuming));
            configurer.writeReport("Test stopped......");
        }));
        reports(configurer);
        ExecutorService service = Executors.newFixedThreadPool(configurer.threads);
        CountDownLatch countDownLatch = new CountDownLatch(configurer.threads);
        for (int i = 0; i < configurer.threads; i++) {
            service.execute(() -> Tester.doStatistics(configurer, runnable, countDownLatch));
            if ((i + 1) % configurer.eachNumber == 0 && configurer.delaySeconds != 0) {
                try {
                    Thread.sleep(configurer.delaySeconds * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                configurer.writeReport(
                        String.format("Thread Launching -- total[%d] run[%d] residue[%d] launch %d thread pre %d s.",
                                configurer.threads, i + 1, (configurer.threads - i - 1), configurer.eachNumber,
                                configurer.delaySeconds));
            }
        }
        try {
            countDownLatch.await();
            service.shutdown();
            Thread.sleep(configurer.collectionFrequencySeconds * 2000);
            executor.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void doStatistics(Configurer configurer, Runnable runnable, CountDownLatch countDownLatch) {
        long loops = configurer.loops == -1 ? 2 : configurer.loops;
        long startLaunched = Clock.clock.getSeconds();
        for (long i = 0; i < loops; i++) {
            try {
                doAnalyzeRTT(configurer, runnable);
                configurer.successCounts.incrementAndGet();
            } catch (Exception e) {
                configurer.failedCounts.incrementAndGet();
            }
            configurer.requestCounts.incrementAndGet();

            // 不限制循环次数，或者指定了测试的时间段
            if (configurer.loops == -1 || configurer.durationSeconds > 0) {
                i = 0;
            }

            // 指定了测试时间段的情况下，超时跳出测试
            if (configurer.durationSeconds > 0 &&
                Clock.clock.getSeconds() - startLaunched > configurer.durationSeconds) {
                break;
            }
        }
        countDownLatch.countDown();
    }

    /**
     * 分析请求的 RTT 分布
     *
     * @param configurer 测试配置
     * @param runnable   待运行的测试代码
     */
    private static void doAnalyzeRTT(Configurer configurer, Runnable runnable) {
        long start = Clock.clock.getMicrosecond();
        runnable.run();
        long timeConsuming = Clock.clock.getMicrosecond() - start;
        configurer.runtimeTimeConsuming.addAndGet(timeConsuming);
        for (Map.Entry<Integer, AtomicLong> entry : configurer.rtts.entrySet()) {
            if (timeConsuming > entry.getKey() * 1000) {
                entry.getValue().incrementAndGet();
            }
        }
        if (timeConsuming < configurer.minTimeConsuming) {
            configurer.minTimeConsuming = timeConsuming;
        }
        if (timeConsuming > configurer.maxTimeConsuming) {
            configurer.maxTimeConsuming = timeConsuming;
        }
    }

    /**
     * 以固定的频率报告测试结果
     *
     * @param configurer 测试配置
     */
    private static void reports(Configurer configurer) {
        final String format = "success: %09d  failed: %06d  TPS: %06d  RTT: %03.03fms";
        executor.scheduleWithFixedDelay(() -> {
            try {
                long success = configurer.successCounts.get();
                long failed = configurer.failedCounts.get();
                long timeConsuming = configurer.runtimeTimeConsuming.get();
                long tps = success + failed - configurer.lastCounts;
                float rtt = (float) (tps == 0 ? 0.00 : (timeConsuming - configurer.lastTimeConsuming) / 1000.00 / tps);
                configurer.lastCounts = success + failed;
                configurer.lastTimeConsuming = timeConsuming;
                configurer.writeReport(String.format(format, success, failed, tps, rtt));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, configurer.collectionFrequencySeconds, TimeUnit.SECONDS);
    }


    public static class Configurer {

        /**
         * 执行的线程数
         */
        private int threads = 1;

        /**
         * 每个线程的循环次数
         */
        private long loops = 1;

        /**
         * 测试的持续时间
         */
        private long durationSeconds = -1;

        /**
         * 统计结果输出的流
         */
        private OutputStream stream = System.out;

        /**
         * 数据统计的频率
         */
        private int collectionFrequencySeconds = 1; // unit s

        /**
         * 通过分批启动线程实现客户端预热，防止同时启动过多线程带来的报错
         */
        private int eachNumber = 1;

        /**
         * 分批启动线程的时间间隔
         */
        private int delaySeconds = 0; // unit s

        /**
         * 统计响应时间百分比的集合，Key 是最大时间（如配置为 2 ms，那么 AtomicLong 计数超过 2ms 的请求 ），VALUE 是计数器
         */
        private final Map<Integer, AtomicLong> rtts = new HashMap<>();

        /**
         * 记录所有请求的总数
         */
        private final AtomicLong requestCounts = new AtomicLong(0);

        /**
         * 记录所有成功请求的总数
         */
        private final AtomicLong successCounts = new AtomicLong(0);
        private long lastCounts = 0;

        /**
         * 记录失败请求的总数
         */
        private final AtomicLong failedCounts = new AtomicLong(0);

        /**
         * 请求的时间之和，单位微秒
         */
        private final AtomicLong runtimeTimeConsuming = new AtomicLong(0);
        private long lastTimeConsuming = 0;

        /**
         * 最小耗时，单位微秒
         */
        private long maxTimeConsuming = Integer.MIN_VALUE;

        /**
         * 最大耗时，单位微秒
         */
        private long minTimeConsuming = Integer.MAX_VALUE;

        /**
         * 配置测试的线程数，注意当配置的线程数目比较大的时候，
         * 应该使用 {@link Configurer#withThreadsWarmingUp(int, int)} 进行线程预热
         *
         * @param threads 线程数
         * @return 测试配置实例
         */
        public Configurer withThreads(int threads) {
            this.threads = threads;
            return this;
        }

        /**
         * 每个线程的循环次数，该循环次数的优先级低于测试时长{@link Configurer#withDurationSeconds(long)}的配置
         *
         * @return 测试配置实例
         */
        public Configurer withLoops(long loops) {
            this.loops = loops;
            return this;
        }

        /**
         * 每个线程的测试时长，该时长配置的优先级高于循环次数{@link Configurer#withLoops(long)}的配置
         *
         * @return 测试配置实例
         */
        public Configurer withDurationSeconds(long durationSeconds) {
            this.durationSeconds = durationSeconds;
            return this;
        }

        /**
         * 测试报告信息输出流，默认使用 {@link System#out}
         *
         * @return 测试配置实例
         */
        public Configurer withOutputStream(OutputStream stream) {
            this.stream = stream;
            return this;
        }

        /**
         * 采集时间间隔，默认为 1s
         *
         * @return 测试配置实例
         */
        public Configurer withCollectionFrequencySeconds(int collectionFrequencySeconds) {
            this.collectionFrequencySeconds = collectionFrequencySeconds;
            return this;
        }

        /**
         * 线程预热配置
         *
         * @param eachNumber   每次启动的线程数
         * @param delaySeconds 每次启动间的时间间隔
         * @return 测试配置实例
         */
        public Configurer withThreadsWarmingUp(int eachNumber, int delaySeconds) {
            this.eachNumber = eachNumber;
            this.delaySeconds = delaySeconds;
            return this;
        }

        /**
         * 添加响应时间统计拦截器
         *
         * @param ms 拦截响应时间小于多少毫秒
         * @return 测试配置
         */
        public Configurer addRTTInterception(int ms) {
            rtts.put(ms, new AtomicLong(0));
            return this;
        }

        /**
         * 向指定的输出流里面写入测试报告
         *
         * @param reports 报告数据
         */
        private void writeReport(String reports) {
            try {
                stream.write(reports.getBytes());
                stream.write("\n".getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 并发下的高速计时器
     */
    private static class Clock {
        private static final Clock clock = new Clock();

        private volatile long now;

        private Clock() {
            now = System.nanoTime();
            new ScheduledThreadPoolExecutor(1, runnable -> {
                Thread thread = new Thread(runnable, "current-time-millis");
                thread.setDaemon(true);
                return thread;
            }).scheduleAtFixedRate(() -> now = System.nanoTime(), 1, 1, TimeUnit.MICROSECONDS);
        }

        public long getSeconds() {
            return now / 1000000000;
        }

        public long getMillisecond() {
            return now / 1000000;
        }

        public long getMicrosecond() {
            return now / 1000;
        }
    }
}

