/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.lancelot.common.net.handler.backend.cmd;

import alchemystar.lancelot.common.net.proto.MySQLPacket;
import alchemystar.lancelot.common.net.proto.mysql.CommandPacket;

/**
 * 一些常用的不会更改的语句
 *
 * @Author lizhuyang
 */
public class CmdPacketEnum {

    public static final CommandPacket _READ_UNCOMMITTED = new CommandPacket();
    public static final CommandPacket _READ_COMMITTED = new CommandPacket();
    public static final CommandPacket _REPEATED_READ = new CommandPacket();
    public static final CommandPacket _SERIALIZABLE = new CommandPacket();
    public static final CommandPacket _AUTOCOMMIT_ON = new CommandPacket();
    public static final CommandPacket _AUTOCOMMIT_OFF = new CommandPacket();
    public static final CommandPacket _COMMIT = new CommandPacket();
    public static final CommandPacket _ROLLBACK = new CommandPacket();

    static {
        _READ_UNCOMMITTED.packetId = 0;
        _READ_UNCOMMITTED.command = MySQLPacket.COM_QUERY;
        _READ_UNCOMMITTED.arg = "SET SESSION TRANSACTION ISOLATION LEVEL READ UNCOMMITTED"
                .getBytes();
        _READ_COMMITTED.packetId = 0;
        _READ_COMMITTED.command = MySQLPacket.COM_QUERY;
        _READ_COMMITTED.arg = "SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED".getBytes();
        _REPEATED_READ.packetId = 0;
        _REPEATED_READ.command = MySQLPacket.COM_QUERY;
        _REPEATED_READ.arg = "SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ".getBytes();
        _SERIALIZABLE.packetId = 0;
        _SERIALIZABLE.command = MySQLPacket.COM_QUERY;
        _SERIALIZABLE.arg = "SET SESSION TRANSACTION ISOLATION LEVEL SERIALIZABLE".getBytes();
        _AUTOCOMMIT_ON.packetId = 0;
        _AUTOCOMMIT_ON.command = MySQLPacket.COM_QUERY;
        _AUTOCOMMIT_ON.arg = "SET autocommit=1".getBytes();
        _AUTOCOMMIT_OFF.packetId = 0;
        _AUTOCOMMIT_OFF.command = MySQLPacket.COM_QUERY;
        _AUTOCOMMIT_OFF.arg = "SET autocommit=0".getBytes();
        _COMMIT.packetId = 0;
        _COMMIT.command = MySQLPacket.COM_QUERY;
        _COMMIT.arg = "commit".getBytes();
        _ROLLBACK.packetId = 0;
        _ROLLBACK.command = MySQLPacket.COM_QUERY;
        _ROLLBACK.arg = "rollback".getBytes();
    }
}
