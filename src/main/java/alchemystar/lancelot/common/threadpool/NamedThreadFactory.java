/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.lancelot.common.threadpool;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 可命名的线程工厂
 *
 * @Author lizhuyang
 */
public class NamedThreadFactory implements ThreadFactory {
    /**
     * Factory总数量
     */
    private static final AtomicInteger POOL_SEQ = new AtomicInteger(1);

    /**
     * 当前Factory创建的线程总数量
     */
    private final AtomicInteger mThreadNum = new AtomicInteger(1);

    /**
     * 线程前缀
     */
    private final String mPrefix;

    /**
     * 是否守护线程
     */
    private final boolean mDaemo;

    private final ThreadGroup mGroup;

    public NamedThreadFactory()
    {
        this("LanceLot-pool-" + POOL_SEQ.getAndIncrement(),false);
    }

    public NamedThreadFactory(String prefix)
    {
        this(prefix,false);
    }

    public NamedThreadFactory(String prefix,boolean daemo)
    {
        mPrefix = prefix + "-thread-";
        mDaemo = daemo;
        SecurityManager s = System.getSecurityManager();
        mGroup = ( s == null ) ? Thread.currentThread().getThreadGroup() : s.getThreadGroup();
    }

    public Thread newThread(Runnable runnable)
    {
        String name = mPrefix + mThreadNum.getAndIncrement();
        Thread ret = new Thread(mGroup,runnable,name,0);
        ret.setDaemon(mDaemo);
        return ret;
    }

    public ThreadGroup getThreadGroup()
    {
        return mGroup;
    }
}
