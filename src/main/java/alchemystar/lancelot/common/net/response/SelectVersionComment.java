/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.lancelot.common.net.response;

import alchemystar.lancelot.common.net.handler.frontend.FrontendConnection;
import alchemystar.lancelot.common.net.proto.mysql.EOFPacket;
import alchemystar.lancelot.common.net.proto.mysql.FieldPacket;
import alchemystar.lancelot.common.net.proto.mysql.ResultSetHeaderPacket;
import alchemystar.lancelot.common.net.proto.mysql.RowDataPacket;
import alchemystar.lancelot.common.net.proto.util.Fields;
import alchemystar.lancelot.common.net.proto.util.PacketUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * SelectVersionComment
 *
 * @Author lizhuyang
 */
public class SelectVersionComment {

    private static final byte[] VERSION_COMMENT =
            "LanceLot Server-1.0 author:alchemystar@163.com".getBytes();
    private static final int FIELD_COUNT = 1;
    private static final ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
    private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
    private static final EOFPacket eof = new EOFPacket();

    static {
        int i = 0;
        byte packetId = 0;
        header.packetId = ++packetId;
        fields[i] = PacketUtil.getField("@@VERSION_COMMENT", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;
        eof.packetId = ++packetId;
    }

    public static void response(FrontendConnection c) {
        ChannelHandlerContext ctx = c.getCtx();
        // todo 精确的buffer申请
        ByteBuf buffer = ctx.alloc().buffer();
        // write header
        buffer = header.writeBuf(buffer, ctx);

        // write fields
        for (FieldPacket field : fields) {
            buffer = field.writeBuf(buffer, ctx);
        }

        // write eof
        buffer = eof.writeBuf(buffer, ctx);

        // write rows
        byte packetId = eof.packetId;
        RowDataPacket row = new RowDataPacket(FIELD_COUNT);
        row.add(VERSION_COMMENT);
        row.packetId = ++packetId;
        buffer = row.writeBuf(buffer, ctx);

        // write last eof
        EOFPacket lastEof = new EOFPacket();
        lastEof.packetId = ++packetId;
        buffer = lastEof.writeBuf(buffer, ctx);

        // post write
        ctx.writeAndFlush(buffer);
    }
}
