/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.lancelot.common.net.handler.frontend;

import alchemystar.lancelot.common.net.exception.UnknownTxIsolationException;
import alchemystar.lancelot.common.net.handler.backend.cmd.CmdPacketEnum;
import alchemystar.lancelot.common.net.handler.backend.cmd.CmdType;
import alchemystar.lancelot.common.net.handler.backend.cmd.Command;
import alchemystar.lancelot.common.net.proto.MySQLPacket;
import alchemystar.lancelot.common.net.proto.mysql.CommandPacket;
import alchemystar.lancelot.common.net.proto.util.CharsetUtil;
import alchemystar.lancelot.common.net.proto.util.Isolations;
import alchemystar.lancelot.parser.ServerParse;
import io.netty.channel.ChannelHandlerContext;

/**
 * Command跨线程传递不能用ByteBuf,因为ByteBuf的PooledBuffer
 * 是threadLocalCache里的,而以byte[]传递是堆的形式,所以靠gc=>Okay
 * 事实上只要保证ByteBuf的分配和write在同一个方法里面就行了
 * @Author lizhuyang
 */
public class AbstractFrontendConnection {

    protected ChannelHandlerContext ctx;

    public Command getFrontendCommand(String sql, int type){
        CommandPacket packet = new CommandPacket();
        packet.packetId = 0;
        packet.command = MySQLPacket.COM_QUERY;
        packet.arg = sql.getBytes();
        Command cmd = new Command(packet, CmdType.FRONTEND_TYPE,type);
        return cmd;
    }

    public Command getCommitCommand(){
        CommandPacket packet = CmdPacketEnum._COMMIT;
        return new Command(packet,CmdType.FRONTEND_TYPE,ServerParse.COMMIT);
    }

    public Command getRollBackCommand(){
        CommandPacket packet = CmdPacketEnum._ROLLBACK;
        return new Command(packet,CmdType.FRONTEND_TYPE,ServerParse.ROLLBACK);
    }



    public Command getAutoCommitOnCmd() {
        CommandPacket packet = CmdPacketEnum._AUTOCOMMIT_ON;
        return new Command(packet, CmdType.BACKEND_TYPE, ServerParse.SET);

    }

    public Command getAutoCommitOffCmd() {
        CommandPacket packet = CmdPacketEnum._AUTOCOMMIT_OFF;
        return  new Command(packet, CmdType.BACKEND_TYPE, ServerParse.SET);
    }


    public Command getBackendCommand(CommandPacket packet, int sqlType) {
        Command command = new Command();
        command.setCmdPacket(packet);
        command.setSqlType(sqlType);
        command.setType(CmdType.BACKEND_TYPE);
        return command;
    }

    public Command getTxIsolationCommand(int txIsolation) {
        CommandPacket packet = getTxIsolationPacket(txIsolation);
        return getBackendCommand(packet, ServerParse.SET);
    }

    public Command getCharsetCommand(int ci) {
        CommandPacket packet = getCharsetPacket(ci);
        return getBackendCommand(packet, ServerParse.SET);
    }

    public Command getUseSchemaCommand(String schema) {
        CommandPacket packet = getUseSchemaPacket(schema);
        return getBackendCommand(packet, ServerParse.USE);
    }

    private CommandPacket getUseSchemaPacket(String schema) {
        StringBuilder s = new StringBuilder();
        s.append("USE ").append(schema);
        CommandPacket cmd = new CommandPacket();
        cmd.packetId = 0;
        cmd.command = MySQLPacket.COM_QUERY;
        cmd.arg = s.toString().getBytes();
        return cmd;
    }

    private CommandPacket getCharsetPacket(int ci) {
        String charset = CharsetUtil.getCharset(ci);
        StringBuilder s = new StringBuilder();
        s.append("SET names ").append(charset);
        CommandPacket cmd = new CommandPacket();
        cmd.packetId = 0;
        cmd.command = MySQLPacket.COM_QUERY;
        cmd.arg = s.toString().getBytes();
        return cmd;
    }

    private CommandPacket getTxIsolationPacket(int txIsolation) {
        switch (txIsolation) {
            case Isolations.READ_UNCOMMITTED:
                return CmdPacketEnum._READ_UNCOMMITTED;
            case Isolations.READ_COMMITTED:
                return CmdPacketEnum._READ_COMMITTED;
            case Isolations.REPEATED_READ:
                return CmdPacketEnum._REPEATED_READ;
            case Isolations.SERIALIZABLE:
                return CmdPacketEnum._SERIALIZABLE;
            default:
                throw new UnknownTxIsolationException("txIsolation:" + txIsolation);
        }
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }
}
