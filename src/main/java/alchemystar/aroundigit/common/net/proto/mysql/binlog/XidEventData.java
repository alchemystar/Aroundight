/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.net.proto.mysql.binlog;

import alchemystar.aroundigit.common.net.proto.mysql.MySQLMessage;

/**
 * XidEventData
 *
 * @Author lizhuyang
 */
public class XidEventData implements EventData {

    private long xid;

    public void read(byte[] data) {
        MySQLMessage mm = new MySQLMessage(data);
        xid = mm.readLong(8);
    }

    public long getXid() {
        return xid;
    }

    public void setXid(long xid) {
        this.xid = xid;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("XidEventData");
        sb.append("{xid=").append(xid);
        sb.append('}');
        return sb.toString();
    }
}
