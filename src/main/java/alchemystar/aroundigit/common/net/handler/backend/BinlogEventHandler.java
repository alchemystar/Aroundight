/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.net.handler.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.aroundigit.common.net.proto.mysql.BinaryPacket;
import alchemystar.aroundigit.common.net.proto.mysql.ErrorPacket;
import alchemystar.aroundigit.common.net.proto.mysql.binlog.Event;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * BinlogEventHandler
 *
 * @Author lizhuyang
 */
public class BinlogEventHandler extends ChannelHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(BinlogEventHandler.class);

    private BackendConnection source;

    public BinlogEventHandler(BackendConnection source) {
        this.source = source;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        BinaryPacket bin = (BinaryPacket) msg;
        switch (bin.data[0]) {
            case ErrorPacket.FIELD_COUNT:
                handleError(bin);
                break;
            case (byte) 0xFE:
                logger.info("complete shut down");
                break;
            default:
                handlerEvent(bin);
                break;
        }

    }

    private void handlerEvent(BinaryPacket bin) {
        // only commit/auto_commit can produce binlog
        Event event = new Event();
        event.read(bin);
    }

    private void handleError(BinaryPacket bin) {
        ErrorPacket error = new ErrorPacket();
        error.read(bin);
        logger.error("Get binlog Event failed,errMsg=" + error.message);
        throw new RuntimeException("Get binlog Event failed,errMsg=" + error.message);
    }
}
