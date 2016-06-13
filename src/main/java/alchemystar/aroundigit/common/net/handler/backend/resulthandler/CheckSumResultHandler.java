/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.net.handler.backend.resulthandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.aroundigit.common.config.SystemConfig;
import alchemystar.aroundigit.common.net.handler.backend.BackendConnection;
import alchemystar.aroundigit.common.net.handler.backend.BinlogEventHandler;
import alchemystar.aroundigit.common.net.handler.backend.codec.DecoderConfig;
import alchemystar.aroundigit.common.net.proto.mysql.CommandPacket;
import alchemystar.aroundigit.common.net.proto.mysql.DumpBinaryLogCommand;
import alchemystar.aroundigit.common.net.proto.mysql.DumpBinaryLogGtidCommand;

/**
 * CheckSumResultHandler
 *
 * @Author lizhuyang
 */
public class CheckSumResultHandler extends ResultSetHandler {

    private static final Logger logger = LoggerFactory.getLogger(SetCheckSumCmdHandler.class);

    public CheckSumResultHandler(BackendConnection source) {
        super(source);
    }

    @Override
    public void doHandleResultSet(ResultSet resultSet) {
        String checksum = null;
        if(resultSet.getRows().isEmpty()) {
            // 低版本兼容
            checksum = "NONE";
        }else{
            checksum = resultSet.getRows().get(0)[1];
        }
        // 校验checksum
        if (ChecksumType.valueOf(checksum.toUpperCase()) == ChecksumType.NONE) {
            DecoderConfig.checksumType = ChecksumType.NONE;
            setBinlogEventHandler();
            // 发送dump命令
            if (source.getBinlogContext().getGtidSet() == null) {
                sendDumpBinaryLog();
            } else {
                sendDumpBinaryLogGtid();
            }
        } else if (ChecksumType.valueOf(checksum.toUpperCase()) == ChecksumType.CRC32) {
            DecoderConfig.checksumType = ChecksumType.CRC32;
            source.setResultSetHander(new SetCheckSumCmdHandler(source));
            // set binlog_checksum
            source.writeNoSelect(new CommandPacket("set @master_binlog_checksum= @@global.binlog_checksum"));
        } else {
            throw new RuntimeException("Unknow checksum type:" + ChecksumType.valueOf(checksum.toUpperCase()));
        }

    }

    // begin to parse binlog event
    public void setBinlogEventHandler() {
        source.getCtx().pipeline()
                .replace("BackendCommandHandler", "BinlogEventHandler", new BinlogEventHandler(source));
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
}
