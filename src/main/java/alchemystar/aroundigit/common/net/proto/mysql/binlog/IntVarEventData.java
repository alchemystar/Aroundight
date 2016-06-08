/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.net.proto.mysql.binlog;

import alchemystar.aroundigit.common.net.proto.mysql.MySQLMessage;

/**
 * IntVarEventData
 *
 * @Author lizhuyang
 */
public class IntVarEventData implements EventData {

    /**
     * Type indicating whether the value is meant to be used for the LAST_INSERT_ID() invocation (should be equal 1) or
     * AUTO_INCREMENT column (should be equal 2).
     */
    private int type;
    private long value;

    public void read(byte[] data) {
        MySQLMessage mm = new MySQLMessage(data);
        type = mm.read();
        value = mm.readLong();
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("IntVarEventData");
        sb.append("{type=").append(type);
        sb.append(", value=").append(value);
        sb.append('}');
        return sb.toString();
    }

}