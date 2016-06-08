/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.net.proto.mysql;

import alchemystar.aroundigit.common.net.proto.MySQLPacket;
import alchemystar.aroundigit.common.net.proto.util.BufferUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * DumpBinaryLogCommand
 *
 * @Author lizhuyang
 */
public class DumpBinaryLogCommand extends MySQLPacket {

    private long serverId;
    private String binlogFilename;
    private long binlogPosition;

    public DumpBinaryLogCommand(long serverId, String binlogFilename, long binlogPosition) {
        packetId = 0;
        this.serverId = serverId;
        this.binlogFilename = binlogFilename;
        this.binlogPosition = binlogPosition;
    }


    public ByteBuf getByteBuf(ChannelHandlerContext ctx){
        ByteBuf buffer = ctx.alloc().buffer();
        BufferUtil.writeUB3(buffer, calcPacketSize());
        buffer.writeByte(packetId);
        BufferUtil.writeInteger(buffer,MySQLPacket.COM_BINLOG_DUMP,1);
        BufferUtil.writeUB4(buffer,binlogPosition);
        // 00 flag
        buffer.writeByte(0);
        buffer.writeByte(0);
        BufferUtil.writeUB4(buffer,serverId);
        buffer.writeBytes(binlogFilename.getBytes());
        return buffer;
    }

    @Override
    protected String getPacketInfo() {
        return "DumpBinaryLogCommand";
    }

    @Override
    public int calcPacketSize() {
        return  1+4+2+4+binlogFilename.length();
    }
}
