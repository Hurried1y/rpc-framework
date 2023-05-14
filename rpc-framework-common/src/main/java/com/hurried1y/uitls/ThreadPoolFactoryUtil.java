package com.hurried1y.uitls;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.hurried1y.config.CustomThreadPoolConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;

/**
 * User：Hurried1y
 * Date：2023/4/19
 */
@Slf4j
public class ThreadPoolFactoryUtil {
    /*
    * 通过 threadNamePrefix 来区分不同的线程池，把相同的 threadNamePrefix 的线程池放看做是同一业务场景服务
    * key：threadNamePrefix
    * value：线程池
    * */
    private static final Map<String, ExecutorService> THREAD_POOLS = new ConcurrentHashMap<>();

    private ThreadPoolFactoryUtil() {
    }

    public static ExecutorService createCustomThreadPoolIfAbsent(String threadNamePrefix){
        CustomThreadPoolConfig customThreadPoolConfig = new CustomThreadPoolConfig();
        return createCustomThreadPoolIfAbsent(customThreadPoolConfig, threadNamePrefix, false);
    }

    public static ExecutorService createCustomThreadPoolIfAbsent(String threadNamePrefix, CustomThreadPoolConfig customThreadPoolConfig) {
        return createCustomThreadPoolIfAbsent(customThreadPoolConfig, threadNamePrefix, false);
    }

    public static ExecutorService createCustomThreadPoolIfAbsent(CustomThreadPoolConfig customThreadPoolConfig, String threadNamePrefix, Boolean daemon) {
        ExecutorService threadPool = THREAD_POOLS.computeIfAbsent(threadNamePrefix, k -> createThreadPool(customThreadPoolConfig, threadNamePrefix, daemon));
        //如果线程池已经关闭，则重新创建一个线程池
        if(threadPool.isShutdown() || threadPool.isTerminated()){
            THREAD_POOLS.remove(threadNamePrefix);
            threadPool = THREAD_POOLS.computeIfAbsent(threadNamePrefix, k -> createThreadPool(customThreadPoolConfig, threadNamePrefix, daemon));
        }
        return threadPool;
    }

    /**
     * 根据自定义的线程池配置创建线程池
     * @param customThreadPoolConfig 自定义的线程池配置
     * @param threadNamePrefix 线程名称前缀
     * @param daemon 是否为守护线程
     * @return 线程池
     */
    public static ExecutorService createThreadPool(CustomThreadPoolConfig customThreadPoolConfig, String threadNamePrefix, Boolean daemon) {
        ThreadFactory threadFactory = createThreadFactory(threadNamePrefix, daemon);
        return new ThreadPoolExecutor(customThreadPoolConfig.getCorePoolSize(),
                customThreadPoolConfig.getMaximumPoolSize(),
                customThreadPoolConfig.getKeepAliveTime(),
                customThreadPoolConfig.getTimeUnit(),
                customThreadPoolConfig.getWorkQueue(),
                threadFactory);
    }

    /**
     * 创建线程工厂 ThreadFactory
     * 如果 threadNamePrefix 不为空，则使用自建 treadFactory ，否则使用默认的线程工厂
     * @param threadNamePrefix 作为创建的线程名称的前缀
     * @param daemon 是否为守护线程
     * @return threadFactory
     */
    public static ThreadFactory createThreadFactory(String threadNamePrefix, Boolean daemon) {
        if(threadNamePrefix != null && !threadNamePrefix.isEmpty()){
            if(daemon != null){
                return new ThreadFactoryBuilder()
                        .setNameFormat(threadNamePrefix + "-%d")
                        .setDaemon(daemon).build();
            } else {
                return new ThreadFactoryBuilder()
                        .setNameFormat(threadNamePrefix + "-%d")
                        .build();
            }
        }
        return Executors.defaultThreadFactory();
    }

    public static void shutdownThreadPool() {
        log.info("call shutdownAllThreadPool method");
        THREAD_POOLS.forEach((k, v) -> {
            v.shutdown();
            log.info("shutdown thread pool [{}] [{}]", k, v.isTerminated());
            try {
                v.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("shutdown thread pool [{}] error", k, e);
                v.shutdownNow();
            }
        });
    }

    /**
     * 打印线程池的状态
     * @param threadPool 线程池对象
     */
    public static void printThreadPoolStatus(ThreadPoolExecutor threadPool) {
        ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1, createThreadFactory("print-thread-pool-status", false));
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            log.info("============ThreadPool Status=============");
            log.info("ThreadPool Size: [{}]", threadPool.getPoolSize());
            log.info("Active Threads: [{}]", threadPool.getActiveCount());
            log.info("Number of Tasks : [{}]", threadPool.getCompletedTaskCount());
            log.info("Number of Tasks in Queue: {}", threadPool.getQueue().size());
            log.info("===========================================");
        }, 0, 1, TimeUnit.SECONDS);
    }
}
