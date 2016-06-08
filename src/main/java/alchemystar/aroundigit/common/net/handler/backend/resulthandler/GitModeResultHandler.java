/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.net.handler.backend.resulthandler;

import alchemystar.aroundigit.common.net.handler.backend.BackendConnection;
import alchemystar.aroundigit.common.net.proto.mysql.CommandPacket;

/**
 * GitModeResultHandler
 *
 * @Author lizhuyang
 */
public class GitModeResultHandler extends ResultSetHandler {

    public GitModeResultHandler(BackendConnection source) {
        super(source);
    }

    @Override
    public void doHandleResultSet(ResultSet resultSet) {
        String[] row = resultSet.getRows().get(0);
        if ("ON".equalsIgnoreCase(row[0])) {
            source.getBinlogContext().useGtidSet();
        }
        source.setResultSetHander(new CheckSumResultHandler(source));
        source.write(new CommandPacket("show global variables like 'binlog_checksum'"));
    }
}
