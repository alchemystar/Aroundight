/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.lancelot.common.net.handler.backend;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.lancelot.common.net.handler.backend.cmd.Command;
import alchemystar.lancelot.common.net.handler.backend.pool.MySqlDataPool;
import alchemystar.lancelot.common.net.handler.frontend.FrontendConnection;
import alchemystar.lancelot.common.net.proto.util.CharsetUtil;
import io.netty.channel.ChannelHandlerContext;

/**
 * 后端连接
 *
 * @Author lizhuyang
 */
public class BackendConnection {

    private static final Logger logger = LoggerFactory.getLogger(BackendConnection.class);

    public int charsetIndex;
    public String charset;
    private long id;
    // init in BackendAuthenticator
    private ChannelHandlerContext ctx;
    // 当前连接所属的连接池
    public MySqlDataPool mySqlDataPool;
    // 后端连接同步latch
    public CountDownLatch syncLatch;
    // FrontendConnection
    public FrontendConnection frontend;
    // 当前后端连接堆积的command,通过队列来实现线程间的无锁化
    private ConcurrentLinkedQueue<Command> cmdQue;

    public BackendConnection(MySqlDataPool mySqlDataPool) {
        this.mySqlDataPool = mySqlDataPool;
        syncLatch = new CountDownLatch(1);
        cmdQue = new ConcurrentLinkedQueue<Command>();
    }

    public void fireCmd() {
        Command command = peekCommand();
        if (command != null) {
            ctx.writeAndFlush(command.getCmdByteBuf(ctx));
        }
    }

    public boolean setCharset(String charset) {
        int ci = CharsetUtil.getIndex(charset);
        if (ci > 0) {
            this.charset = charset;
            this.charsetIndex = ci;
            return true;
        } else {
            return false;
        }
    }

    public ChannelHandlerContext getFrontCtx() {
        return frontend.getCtx();
    }

    public void postCommand(Command command) {
        cmdQue.offer(command);
    }

    public Command peekCommand() {
        return cmdQue.peek();
    }

    public Command pollCommand() {
        return cmdQue.poll();
    }

    public boolean isAlive() {
        return ctx.channel().isActive();
    }

    public void close() {
        // send quit
        ctx.close();
    }

    // 连接回收
    public void recycle() {
        logger.info("backendConnection has been recycled");
        mySqlDataPool.putBackend(this);
    }

    public void discard(){
        mySqlDataPool.discard(this);
        close();
    }


    public void countDown() {
        if (!mySqlDataPool.isInited()) {
            mySqlDataPool.countDown();
        }
        syncLatch.countDown();
        // for gc
        syncLatch = null;
    }

    public int getCharsetIndex() {
        return charsetIndex;
    }

    public void setCharsetIndex(int charsetIndex) {
        this.charsetIndex = charsetIndex;
    }

    public String getCharset() {
        return charset;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public FrontendConnection getFrontend() {
        return frontend;
    }

    public void setFrontend(FrontendConnection frontend) {
        this.frontend = frontend;
    }


}
