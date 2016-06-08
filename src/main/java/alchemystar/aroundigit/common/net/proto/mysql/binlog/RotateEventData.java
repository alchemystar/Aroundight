/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.net.proto.mysql.binlog;

import alchemystar.aroundigit.common.net.proto.mysql.MySQLMessage;

/**
 * RotateEventData
 *
 * @Author lizhuyang
 */
public class RotateEventData implements EventData {

    private long binlogPosition;
    private String binlogFilename;

    public void read(byte[] data){
        MySQLMessage mm = new MySQLMessage(data);
        binlogPosition = mm.readLong();
        binlogFilename = mm.readString();
    }

    public String getBinlogFilename() {
        return binlogFilename;
    }

    public void setBinlogFilename(String binlogFilename) {
        this.binlogFilename = binlogFilename;
    }

    public long getBinlogPosition() {
        return binlogPosition;
    }

    public void setBinlogPosition(long binlogPosition) {
        this.binlogPosition = binlogPosition;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("RotateEventData");
        sb.append("{binlogFilename='").append(binlogFilename).append('\'');
        sb.append(", binlogPosition=").append(binlogPosition);
        sb.append('}');
        return sb.toString();
    }
}