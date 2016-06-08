/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.net.handler.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.aroundigit.common.net.exception.UnknownPacketException;
import alchemystar.aroundigit.common.net.handler.backend.resulthandler.ResultSet;
import alchemystar.aroundigit.common.net.proto.MySQLPacket;
import alchemystar.aroundigit.common.net.proto.mysql.BinaryPacket;
import alchemystar.aroundigit.common.net.proto.mysql.EOFPacket;
import alchemystar.aroundigit.common.net.proto.mysql.ErrorPacket;
import alchemystar.aroundigit.common.net.proto.mysql.FieldPacket;
import alchemystar.aroundigit.common.net.proto.mysql.OkPacket;
import alchemystar.aroundigit.common.net.proto.mysql.RowDataPacket;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * CommandHandler
 *
 * @Author lizhuyang
 */
public class BackendCommandHandler extends ChannelHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(BackendCommandHandler.class);

    private BackendConnection source;

    private volatile int selectState;

    private ResultSet resultSet;

    private int nowFieldCount;

    public BackendCommandHandler(BackendConnection source) {
        this.source = source;
        selectState = BackendConnState.RESULT_SET_FIELD_COUNT;
        resultSet = new ResultSet();
        nowFieldCount = 0;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        BinaryPacket bin = (BinaryPacket) msg;
        handleResponse(bin);
    }

    private boolean handleResponse(BinaryPacket bin) {
        if (source.isSelecting()) {
            return handleResultSet(bin);
        } else {
            return handleNormalResult(bin);
        }
    }

    private boolean handleResultSet(BinaryPacket bin) {
        boolean result = false;
        int type = bin.data[0];
        switch (type) {
            case ErrorPacket.FIELD_COUNT:
                // 重置状态,且告诉上层当前select已经处理完毕
                resetSelect();
                resultSet.clear();
                logger.info("error");
                result = true;
                ErrorPacket err = new ErrorPacket();
                err.read(bin);
                logger.error("handleResultSet errorMessage:" + new String(err.message));
                break;
            case EOFPacket.FIELD_COUNT:
                EOFPacket eof = new EOFPacket();
                eof.read(bin);
                if (selectState == BackendConnState.RESULT_SET_FIELDS) {
                    resultSet.setFieldCount(nowFieldCount);
                    // 推进状态 需要步进两次状态,先到field_eof,再到row
                    selectStateStep();
                    selectStateStep();
                } else {
                    if (eof.hasStatusFlag(MySQLPacket.SERVER_MORE_RESULTS_EXISTS)) {
                        // 重置为select的初始状态,但是还是处在select mode下
                        selectState = BackendConnState.RESULT_SET_FIELD_COUNT;
                    } else {
                        // 重置,且告诉上层当前select已经处理完毕
                        resetSelect();
                        // 顺序不可变
                        source.getResultSetHander().handleResultSet(resultSet);
                        result = true;
                    }
                }
                break;
            default:
                switch (selectState) {
                    case BackendConnState.RESULT_SET_FIELD_COUNT:
                        selectStateStep();
                        break;
                    case BackendConnState.RESULT_SET_FIELDS:
                        FieldPacket fieldPacket = new FieldPacket();
                        fieldPacket.read(bin);
                        resultSet.addField(new String(fieldPacket.name));
                        // 累积的FieldCount
                        nowFieldCount++;
                        break;
                    case BackendConnState.RESULT_SET_ROW:
                        RowDataPacket rowDataPacket = new RowDataPacket(nowFieldCount);
                        rowDataPacket.read(bin);
                        resultSet.addRow(rowDataPacket.fieldStrings.toArray(new String[nowFieldCount]));
                        break;
                }
        }
        return result;
    }

    private void resetSelect() {
        source.setSelecting(false);
        selectState = BackendConnState.RESULT_SET_FIELD_COUNT;
        nowFieldCount = 0;
        // resultSet的清理下放到ResultSetHandler执行
    }

    // select状态的推进
    private void selectStateStep() {
        selectState++;
        // last_eof和field_count合并为同一状态
        if (selectState == 6) {
            selectState = 2;
        }
    }

    private boolean handleNormalResult(BinaryPacket bin) {
        source.getResultSetHander().normalHandler(bin);
        return true;
    }
}
