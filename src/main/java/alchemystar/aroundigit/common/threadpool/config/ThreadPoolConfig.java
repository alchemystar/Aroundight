/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.threadpool.config;

/**
 * 线程池配置类
 *
 * @Author lizhuyang
 */
public class ThreadPoolConfig {
    /**
     * 线程名称前缀
     */
    private String name;
    /**
     * 核心线程数量
     */
    private int coreSize;
    /**
     * 最大线程数量
     */
    private int maxSize;
    /**
     * 任务排队最大个数
     */
    private int queues;
    /**
     * 线程保活时间
     */
    int alive;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCoreSize() {
        return coreSize;
    }

    public void setCoreSize(int coreSize) {
        this.coreSize = coreSize;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public int getQueues() {
        return queues;
    }

    public void setQueues(int queues) {
        this.queues = queues;
    }

    public int getAlive() {
        return alive;
    }

    public void setAlive(int alive) {
        this.alive = alive;
    }
}
