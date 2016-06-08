/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.threadpool.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.aroundigit.common.threadpool.AbstractThreadPool;
import alchemystar.aroundigit.common.threadpool.ThreadPool;
import alchemystar.aroundigit.common.threadpool.cached.CachedThreadPool;

/**
 * ThreadPool测试类
 *
 * @Author lizhuyang
 */
public class ThreadPoolTest {

    protected static final Logger logger = LoggerFactory.getLogger(ThreadPoolTest.class);

    public static void main(String[] args) {

        ThreadPool threadPool = new CachedThreadPool();
        ((AbstractThreadPool)threadPool).getExecutor("ThreadPoolTest");
        logger.info("this is for log test");
    }
}
