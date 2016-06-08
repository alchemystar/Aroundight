/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.net.proto.mysql.binlog;

import alchemystar.aroundigit.common.net.proto.mysql.MySQLMessage;

/**
 * GtidEventData
 *
 * @Author lizhuyang
 */
public class GtidEventData implements EventData {

    public static final byte COMMIT_FLAG = 1;

    private String gtid;
    private byte flags;

    public void read(byte[] data) {
        MySQLMessage mm = new MySQLMessage(data);
        flags = mm.read();
        byte[] sid = mm.readBytes(16);
        long gno = mm.readLong();
        gtid = byteArrayToHex(sid, 0, 4) + "-" +
                byteArrayToHex(sid, 4, 2) + "-" +
                byteArrayToHex(sid, 6, 2) + "-" +
                byteArrayToHex(sid, 8, 2) + "-" +
                byteArrayToHex(sid, 10, 6) + ":" +
                String.format("%d", gno);
    }

    private String byteArrayToHex(byte[] a, int offset, int len) {
        StringBuilder sb = new StringBuilder();
        for (int idx = offset; idx < (offset + len) && idx < a.length; idx++) {
            sb.append(String.format("%02x", a[idx] & 0xff));
        }
        return sb.toString();
    }

    public String getGtid() {
        return gtid;
    }

    public void setGtid(String gtid) {
        this.gtid = gtid;
    }

    public byte getFlags() {
        return flags;
    }

    public void setFlags(byte flags) {
        this.flags = flags;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("GtidEventData");
        sb.append("{flags=").append(flags).append(", gtid='").append(gtid).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
