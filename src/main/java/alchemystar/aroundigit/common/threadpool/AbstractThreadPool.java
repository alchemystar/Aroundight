/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.threadpool;

import java.util.concurrent.Executor;

import alchemystar.aroundigit.common.threadpool.config.ThreadPoolConfig;
import alchemystar.aroundigit.common.threadpool.config.ThreadPoolConfigFactory;

/**
 * @Author lizhuyang
 */
public abstract class AbstractThreadPool implements ThreadPool {

    /**
     * 默认的线程池配置
     * 核心16个 最大线程32个 排队数量128个 保活时间60s
     *
     * @return
     */
    public Executor getExecutor(String name) {
        return getExecutor(ThreadPoolConfigFactory.getDefaultConfig(name));
    }

    /**
     * 通过配置来获取线程池
     *
     * @param config
     *
     * @return
     */
    public abstract Executor getExecutor(ThreadPoolConfig config);
}
