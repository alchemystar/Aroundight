/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.net.proto.mysql.binlog;

import alchemystar.aroundigit.common.net.proto.mysql.MySQLMessage;

/**
 * RowsQueryEventData
 *
 * @Author lizhuyang
 */
public class RowsQueryEventData implements EventData {

    private String query;

    public void read(byte[] data) {
        MySQLMessage mm = new MySQLMessage(data);
        int len = mm.read();
        query = new String(mm.readBytes(len));
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("RowsQueryEventData");
        sb.append("{query='").append(query).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
