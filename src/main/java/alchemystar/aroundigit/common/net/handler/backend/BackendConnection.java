/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.net.handler.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.aroundigit.common.net.handler.backend.resulthandler.ResultSetHandler;
import alchemystar.aroundigit.common.net.proto.MySQLPacket;
import alchemystar.aroundigit.common.net.proto.mysql.CommandPacket;
import alchemystar.aroundigit.common.net.proto.util.CharsetUtil;
import io.netty.channel.ChannelHandlerContext;

/**
 * 与MySql连接
 *
 * @Author lizhuyang
 */
public class BackendConnection {

    private static final Logger logger = LoggerFactory.getLogger(BackendConnection.class);

    public int charsetIndex;
    public String charset;
    private long threadId;
    // init in BackendAuthenticator
    private ChannelHandlerContext ctx;
    // 是否在select
    private volatile boolean selecting;

    private BinlogContext binlogContext;

    private ResultSetHandler resultSetHander;

    public BackendConnection() {
        binlogContext = new BinlogContext();
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

    public boolean isAlive() {
        return ctx.channel().isActive();
    }

    public void close() {
        // send quit
        ctx.close();
    }

    public void write(CommandPacket cmp) {
        selecting = true;
        ctx.writeAndFlush(cmp.getByteBuf(ctx));
    }

    public void writeNoSelect(CommandPacket cmp){
        selecting = false;
        ctx.writeAndFlush(cmp.getByteBuf(ctx));
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

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public boolean isSelecting() {
        return selecting;
    }

    public void setSelecting(boolean selecting) {
        this.selecting = selecting;
    }

    public ResultSetHandler getResultSetHander() {
        return resultSetHander;
    }

    public void setResultSetHander(
            ResultSetHandler resultSetHander) {
        this.resultSetHander = resultSetHander;
    }

    public BinlogContext getBinlogContext() {
        return binlogContext;
    }

    public void setBinlogContext(BinlogContext binlogContext) {
        this.binlogContext = binlogContext;
    }
}
