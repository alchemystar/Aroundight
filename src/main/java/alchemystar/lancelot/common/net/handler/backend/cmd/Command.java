/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.lancelot.common.net.handler.backend.cmd;

import alchemystar.lancelot.common.net.proto.mysql.CommandPacket;
import alchemystar.lancelot.common.net.proto.util.BufferUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * MySql Command包装类
 *
 * @Author lizhuyang
 */
public class Command {
    // command的比特buffer
    private CommandPacket cmdPacket;
    // command的Type
    private CmdType type;
    // sqlType,select|update|set|delete
    private int sqlType;

    public Command() {
    }

    public Command(CommandPacket cmdPacket, CmdType type, int sqlType) {
        this.cmdPacket = cmdPacket;
        this.type = type;
        this.sqlType = sqlType;
    }

    public ByteBuf getCmdByteBuf(ChannelHandlerContext ctx) {
        return cmdPacket.getByteBuf(ctx);
    }

    public CommandPacket getCmdPacket() {
        return cmdPacket;
    }

    public void setCmdPacket(CommandPacket cmdPacket) {
        this.cmdPacket = cmdPacket;
    }

    public CmdType getType() {
        return type;
    }

    public void setType(CmdType type) {
        this.type = type;
    }

    public int getSqlType() {
        return sqlType;
    }

    public void setSqlType(int sqlType) {
        this.sqlType = sqlType;
    }

}
