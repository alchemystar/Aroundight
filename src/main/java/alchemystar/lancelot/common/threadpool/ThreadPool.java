/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.lancelot.common.threadpool;

import java.util.concurrent.Executor;

import alchemystar.lancelot.common.threadpool.config.ThreadPoolConfig;

/**
 * @Author lizhuyang
 */
public interface ThreadPool {

    /**
     * 采用默认的线程池配置
     * @param name
     * @return
     */
    public Executor getExecutor(String name);

    /**
     * 使用ThreadPoolConfig配置线程池
     * @param config
     * @return
     */
    public Executor getExecutor(ThreadPoolConfig config);
}
