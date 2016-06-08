/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.threadpool.limited;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import alchemystar.aroundigit.common.threadpool.AbstractThreadPool;
import alchemystar.aroundigit.common.threadpool.AbortPolicyWithReport;
import alchemystar.aroundigit.common.threadpool.NamedThreadFactory;
import alchemystar.aroundigit.common.threadpool.config.ThreadPoolConfig;

/**
 * 此线程池一直增长，直到上限，增长后不收缩
 *
 * @Author lizhuyang
 */
public class LimitedThreadPool extends AbstractThreadPool {

    @Override
    public Executor getExecutor(ThreadPoolConfig threadPoolConfig) {
        return getExecutor(threadPoolConfig.getName(), threadPoolConfig.getCoreSize(), threadPoolConfig.getMaxSize(),
                threadPoolConfig.getQueues());
    }

    /**
     * @param name     线程前缀
     * @param coreSize 核心线程数量
     * @param maxSize  最大线程数量
     * @param queues   最大排队数量
     *
     * @return
     */
    public Executor getExecutor(String name, int coreSize, int maxSize, int queues) {

        return new ThreadPoolExecutor(coreSize, maxSize, Long.MAX_VALUE, TimeUnit.MILLISECONDS,
                queues == 0 ? new SynchronousQueue<Runnable>() :
                        (queues < 0 ? new LinkedBlockingQueue<Runnable>()
                                 : new LinkedBlockingQueue<Runnable>(queues)),
                new NamedThreadFactory(name, true), new AbortPolicyWithReport(name));
    }
}
