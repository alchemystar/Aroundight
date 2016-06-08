/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.lancelot.common.net.handler.backend;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.lancelot.common.net.exception.ErrorPacketException;
import alchemystar.lancelot.common.net.exception.UnknownPacketException;
import alchemystar.lancelot.common.net.handler.backend.cmd.CmdType;
import alchemystar.lancelot.common.net.handler.backend.cmd.Command;
import alchemystar.lancelot.common.net.handler.frontend.FrontendConnection;
import alchemystar.lancelot.common.net.handler.node.ResponseHandler;
import alchemystar.lancelot.common.net.proto.MySQLPacket;
import alchemystar.lancelot.common.net.proto.mysql.BinaryPacket;
import alchemystar.lancelot.common.net.proto.mysql.EOFPacket;
import alchemystar.lancelot.common.net.proto.mysql.ErrorPacket;
import alchemystar.lancelot.common.net.proto.mysql.OkPacket;
import alchemystar.lancelot.parser.ServerParse;
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
    // 是否在select
    private volatile boolean selecting;
    private volatile int selectState;
    // 保存fieldCount,field以及fieldEof信息
    private List<BinaryPacket> fieldList;

    public BackendCommandHandler(BackendConnection source) {
        this.source = source;
        selecting = false;
        selectState = BackendConnState.RESULT_SET_FIELD_COUNT;
        fieldList = new LinkedList<BinaryPacket>();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        BinaryPacket bin = (BinaryPacket) msg;
        if (processCmd(ctx, bin)) {
            // fire the next cmd
            source.fireCmd();
        }
    }

    private boolean processCmd(ChannelHandlerContext ctx, BinaryPacket bin) {
        Command cmd = source.peekCommand();
        if (cmd.getSqlType() == ServerParse.SELECT || cmd.getSqlType() == ServerParse.SHOW) {
            selecting = true;
        } else {
            selecting = false;
        }
        // if handle successful , the command can be remove
        if (handleResponse(bin, cmd.getType())) {
            source.pollCommand();
            return true;
        } else {
            return false;
        }
    }

    private boolean handleResponse(BinaryPacket bin, CmdType cmdType) {
        if (selecting) {
            return handleResultSet(bin, cmdType);
        } else {
            return handleNormalResult(bin, cmdType);
        }
    }

    private boolean handleResultSet(BinaryPacket bin, CmdType cmdType) {
        boolean result = false;
        int type = bin.data[0];
        switch (type) {
            case ErrorPacket.FIELD_COUNT:
                // 重置状态,且告诉上层当前select已经处理完毕
                resetSelect();
                result = true;
                ErrorPacket err = new ErrorPacket();
                err.read(bin);
                // write(bin,cmdType);
                getResponseHandler().errorResponse(bin);
                logger.error("handleResultSet errorMessage:" + new String(err.message));
                break;
            case EOFPacket.FIELD_COUNT:
                EOFPacket eof = new EOFPacket();
                eof.read(bin);
                if (selectState == BackendConnState.RESULT_SET_FIELDS) {
                    // logger.info("eof");
                    // 推进状态 需要步进两次状态,先到field_eof,再到row
                    selectStateStep();
                    selectStateStep();
                    // 给FieldList增加eof
                    addToFieldList(bin);
                    getResponseHandler().fieldListResponse(fieldList);
                } else {
                    if (eof.hasStatusFlag(MySQLPacket.SERVER_MORE_RESULTS_EXISTS)) {
                        // 重置为select的初始状态,但是还是处在select mode下
                        selectState = BackendConnState.RESULT_SET_FIELD_COUNT;
                    } else {
                        // 重置,且告诉上层当前select已经处理完毕
                        resetSelect();
                        result = true;
                    }
                    getResponseHandler().lastEofResponse(bin);
                }
                break;
            default:
                switch (selectState) {
                    case BackendConnState.RESULT_SET_FIELD_COUNT:
                        selectStateStep();
                        addToFieldList(bin);
                        break;
                    case BackendConnState.RESULT_SET_FIELDS:
                        addToFieldList(bin);
                        break;
                    case BackendConnState.RESULT_SET_ROW:
                        getResponseHandler().rowResponse(bin);
                        break;
                }
        }
        return result;
    }

    private void addToFieldList(BinaryPacket bin) {
        fieldList.add(bin);
    }

    private void resetSelect() {
        selecting = false;
        selectState = BackendConnState.RESULT_SET_FIELD_COUNT;
        fieldList.clear();
    }

    // select状态的推进
    private void selectStateStep() {
        selectState++;
        // last_eof和field_count合并为同一状态
        if (selectState == 6) {
            selectState = 2;
        }
    }

    private boolean handleNormalResult(BinaryPacket bin, CmdType cmdType) {
        switch (bin.data[0]) {
            case OkPacket.FIELD_COUNT:
                if (cmdType == CmdType.BACKEND_TYPE) {
                    // logger.debug("backend command okay");
                } else {
                    // logger.debug("frontend command okay");
                    getResponseHandler().okResponse(bin);
                }
                break;
            case ErrorPacket.FIELD_COUNT:
                ErrorPacket err = new ErrorPacket();
                err.read(bin);
                if (cmdType == CmdType.BACKEND_TYPE) {
                    throw new ErrorPacketException("Command errorMessage,message=" + new String(err.message));
                } else {
                    getResponseHandler().errorResponse(bin);
                }
                break;
            default:
                throw new UnknownPacketException(bin.toString());
        }
        return true;
    }

    private ResponseHandler getResponseHandler() {
        FrontendConnection frontendConnection = source.frontend;
        return frontendConnection.getResponseHandler();
    }

}
