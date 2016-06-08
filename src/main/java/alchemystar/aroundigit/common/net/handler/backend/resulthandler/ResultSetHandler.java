/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.net.handler.backend.resulthandler;

import alchemystar.aroundigit.common.net.exception.ErrorPacketException;
import alchemystar.aroundigit.common.net.exception.UnknownPacketException;
import alchemystar.aroundigit.common.net.handler.backend.BackendConnection;
import alchemystar.aroundigit.common.net.proto.mysql.BinaryPacket;
import alchemystar.aroundigit.common.net.proto.mysql.ErrorPacket;
import alchemystar.aroundigit.common.net.proto.mysql.OkPacket;

/**
 * @Author lizhuyang
 */
public abstract class ResultSetHandler {

    protected BackendConnection source;

    public ResultSetHandler(BackendConnection source) {
        this.source = source;
    }

    public void handleResultSet(ResultSet resultSet) {
        doHandleResultSet(resultSet);
        resultSet.clear();
    }

    public void normalHandler(BinaryPacket bin) {
        switch (bin.data[0]) {
            case OkPacket.FIELD_COUNT:
                OkPacket okPacket = new OkPacket();
                okPacket.read(bin);
                doOkay(okPacket);
                break;
            case ErrorPacket.FIELD_COUNT:
                ErrorPacket err = new ErrorPacket();
                err.read(bin);
                doErr(err);
                break;
            default:
                throw new UnknownPacketException(bin.toString());
        }
    }

    public void doOkay(OkPacket okPacket) {
    }

    public void doErr(ErrorPacket err) {
        throw new ErrorPacketException(new String(err.message));
    }

    public void doHandleResultSet(ResultSet resultSet) {
    }

}
