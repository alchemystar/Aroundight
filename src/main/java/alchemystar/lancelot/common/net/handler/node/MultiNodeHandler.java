/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.lancelot.common.net.handler.node;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import alchemystar.lancelot.common.net.proto.mysql.ErrorPacket;

/**
 * 多节点执行handler
 *
 * @Author lizhuyang
 */
public abstract class MultiNodeHandler implements ResponseHandler {

    // 执行节点数量
    private int nodeCount;
    // 并发请求,执行节点锁
    protected final ReentrantLock lock = new ReentrantLock();
    // MySql packetId
    protected byte packetId;
    // errno
    protected int errno;
    // errorMessage
    protected String errorMessage;
    // 是否已经失败了,用于还有失败后,还有节点执行的情况
    protected AtomicBoolean isFailed = new AtomicBoolean(false);

    // 立马down成0
    // todo 这里需要考虑kill 后端连接
    protected void decrementCountToZero() {
        lock.lock();
        try {
            nodeCount = 0;
        } finally {
            lock.unlock();
        }
    }

    // nodeCount--
    // todo 考虑callBack,此处去掉lock,因为大部分都是在lock下调用
    protected boolean decrementCountBy() {
        boolean zeroReached = false;
        if (zeroReached = --nodeCount == 0) {
            zeroReached = true;
        }
        return zeroReached;
    }

    // 重置MutliNodeHandler
    protected void restet() {
        reset(0);
    }

    protected void reset(int initCount) {
        nodeCount = initCount;
        isFailed.set(false);
        errorMessage = null;
        packetId = 0;
    }



    public void setFailed(String errorMessage,int errno) {
        isFailed.set(true);
        this.errorMessage = errorMessage;
    }


}
