/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.threadpool.config;

/**
 * 线程配置工厂
 * @Author lizhuyang
 */
public class ThreadPoolConfigFactory {

    /**
     *  默认的线程配置
     *  核心16个 最大线程32个 排队数量128个 保活时间60s
     */
    public static ThreadPoolConfig getDefaultConfig(String name) {
        ThreadPoolConfig config = new ThreadPoolConfig();
        config.setName(name);
        config.setAlive(60);
        config.setCoreSize(16);
        config.setMaxSize(32);
        config.setQueues(128);
        return config;
    }
}
