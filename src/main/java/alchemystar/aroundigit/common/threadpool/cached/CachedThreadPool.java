/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.threadpool.cached;

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
 * 此线程池可伸缩，线程空闲一分钟后回收，新请求重新创建线程
 *
 * @Author lizhuyang
 */
public class CachedThreadPool extends AbstractThreadPool {

    @Override
    public Executor getExecutor(ThreadPoolConfig config) {
        return getExecutor(config.getName(), config.getCoreSize(), config.getMaxSize(), config.getQueues(),
                config.getAlive());
    }

    public Executor getExecutor(String name, int coreSize, int maxSize, int queues, int alive) {

        return new ThreadPoolExecutor(coreSize, maxSize, alive, TimeUnit.MILLISECONDS,
                queues == 0 ? new SynchronousQueue<Runnable>() :
                        (queues < 0 ? new LinkedBlockingQueue<Runnable>()
                                 : new LinkedBlockingQueue<Runnable>(queues)),
                new NamedThreadFactory(name, true), new AbortPolicyWithReport(name));
    }

}