/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.net.handler.backend.resulthandler;

import alchemystar.aroundigit.common.net.handler.backend.BackendConnection;
import alchemystar.aroundigit.common.net.proto.mysql.CommandPacket;

/**
 * PositionResultHandler
 *
 * @Author lizhuyang
 */
public class PositionResultHandler extends ResultSetHandler {

    public PositionResultHandler(BackendConnection source) {
        super(source);
    }

    public void doHandleResultSet(ResultSet resultSet) {
        source.getBinlogContext().handlePositionResult(resultSet);
        // the next handler
        source.setResultSetHander(new GitModeResultHandler(source));
        // then post a new command to decide gtid use
        source.write(new CommandPacket("show global variables like 'gtid_mode'"));
    }
}
