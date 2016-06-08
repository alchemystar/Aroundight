/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.lancelot.common.net.handler.node;

import java.util.List;

import alchemystar.lancelot.common.net.handler.backend.BackendConnection;
import alchemystar.lancelot.common.net.handler.backend.cmd.Command;
import alchemystar.lancelot.common.net.handler.session.FrontendSession;
import alchemystar.lancelot.common.net.proto.mysql.BinaryPacket;
import alchemystar.lancelot.common.net.proto.mysql.OkPacket;
import alchemystar.lancelot.common.net.proto.util.ErrorCode;
import alchemystar.lancelot.common.net.route.RouteResultset;
import alchemystar.lancelot.common.net.route.RouteResultsetNode;

/**
 * SingleNodeExecutor
 *
 * @Author lizhuyang
 */
public class SingleNodeExecutor implements ResponseHandler {

    private FrontendSession session;

    public SingleNodeExecutor(FrontendSession session) {
        this.session = session;
    }

    public void execute(RouteResultset rrs) {
        if (rrs.getNodes() == null || rrs.getNodes().length == 0) {
            session.writeErrMessage(ErrorCode.ERR_SINGLE_EXECUTE_NODES, "SingleNode executes no nodes");
            return;
        }
        if (rrs.getNodes().length > 1) {
            session.writeErrMessage(ErrorCode.ERR_SINGLE_EXECUTE_NODES, "SingleNode executes too many nodes");
            return;
        }
        // 当前RouteResultset对应的Backend
        BackendConnection backend = getBackend(rrs);
        RouteResultsetNode node = rrs.getNodes()[0];
        Command command = session.getSource().getFrontendCommand(node.getStatement(), node.getSqlType());
        backend.postCommand(command);
        // fire it
        backend.fireCmd();
    }

    public void fieldListResponse(List<BinaryPacket> fieldList) {
        writeFiledList(fieldList);
    }

    private void writeFiledList(List<BinaryPacket> fieldList) {
        for (BinaryPacket bin : fieldList) {
            bin.write(session.getCtx());
        }
        fieldList.clear();
    }

    public void errorResponse(BinaryPacket bin) {
        bin.write(session.getCtx());
        if (session.getSource().isAutocommit()) {
            session.release();
        }
    }

    public void okResponse(BinaryPacket bin) {
        OkPacket ok = new OkPacket();
        session.getSource().setLastInsertId(ok.insertId);
        bin.write(session.getCtx());
        if (session.getSource().isAutocommit()) {
            session.release();
        }
    }

    public void rowResponse(BinaryPacket bin) {
        bin.write(session.getCtx());
    }

    public void lastEofResponse(BinaryPacket bin) {
        bin.write(session.getCtx());
        if (session.getSource().isAutocommit()) {
            session.release();
        }
    }

    private BackendConnection getBackend(RouteResultset rrs) {
        return session.getTarget(rrs.getNodes()[0]);
    }

}
