/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.net.handler.backend.resulthandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.aroundigit.common.config.SystemConfig;
import alchemystar.aroundigit.common.net.handler.backend.BackendConnection;
import alchemystar.aroundigit.common.net.handler.backend.BinlogEventHandler;
import alchemystar.aroundigit.common.net.proto.mysql.DumpBinaryLogCommand;
import alchemystar.aroundigit.common.net.proto.mysql.DumpBinaryLogGtidCommand;
import alchemystar.aroundigit.common.net.proto.mysql.OkPacket;

/**
 * SetCheckSumCmdHandler
 *
 * @Author lizhuyang
 */
public class SetCheckSumCmdHandler extends ResultSetHandler {
    private static final Logger logger = LoggerFactory.getLogger(SetCheckSumCmdHandler.class);

    public SetCheckSumCmdHandler(BackendConnection source) {
        super(source);
    }

    @Override
    public void doOkay(OkPacket okPacket) {
        source.getBinlogContext().setChecksumType(ChecksumType.NONE);
        setBinlogEventHandler();
        // 发送dump命令
        if (source.getBinlogContext().getGtidSet() == null) {
            sendDumpBinaryLog();
        } else {
            sendDumpBinaryLogGtid();
        }

    }

    private void sendDumpBinaryLog() {
        DumpBinaryLogCommand command =
                new DumpBinaryLogCommand(SystemConfig.ServerId, source.getBinlogContext().getBinLogFileName()
                        , source.getBinlogContext().getBinlogPosition());
        source.getCtx().writeAndFlush(command.getByteBuf(source.getCtx()));
        logger.debug("send dump command");
    }

    private void sendDumpBinaryLogGtid() {
        DumpBinaryLogGtidCommand command =
                new DumpBinaryLogGtidCommand(SystemConfig.ServerId, "", 4L, source.getBinlogContext().getGtidSet());
        source.getCtx().writeAndFlush(command.getByteBuf(source.getCtx()));
        logger.debug("send dump gtid command");
    }

    // begin to parse binlog event
    public void setBinlogEventHandler() {
        source.getCtx().pipeline()
                .replace("BackendCommandHandler", "BinlogEventHandler", new BinlogEventHandler(source));
    }
}
