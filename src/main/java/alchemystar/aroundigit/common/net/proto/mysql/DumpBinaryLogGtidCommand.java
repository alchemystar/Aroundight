/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.net.proto.mysql;

import java.util.Collection;

import alchemystar.aroundigit.common.net.handler.backend.GtidSet;
import alchemystar.aroundigit.common.net.proto.MySQLPacket;
import alchemystar.aroundigit.common.net.proto.util.BufferUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * DumpBinaryLogGtidCommand
 *
 * @Author lizhuyang
 */
public class DumpBinaryLogGtidCommand extends MySQLPacket {

    private long serverId;
    private String binlogFilename;
    private long binlogPosition;
    private GtidSet gtidSet;

    public DumpBinaryLogGtidCommand(long serverId, String binlogFilename, long binlogPosition,
                                    GtidSet gtidSet) {
        this.serverId = serverId;
        this.binlogFilename = binlogFilename;
        this.binlogPosition = binlogPosition;
        this.gtidSet = gtidSet;
    }

    public ByteBuf getByteBuf(ChannelHandlerContext ctx) {
        ByteBuf buffer = ctx.alloc().buffer();
        BufferUtil.writeUB3(buffer, calcPacketSize());
        buffer.writeByte(packetId);

        BufferUtil.writeInteger(buffer, MySQLPacket.COM_BINLOG_DUMP, 1);
        // 00 flag
        buffer.writeByte(0);
        buffer.writeByte(0);
        BufferUtil.writeUB4(buffer, serverId);
        BufferUtil.writeInteger(buffer, binlogFilename.length(), 4);
        buffer.writeBytes(binlogFilename.getBytes());
        BufferUtil.writeLong(buffer, binlogPosition, 8);
        Collection<GtidSet.UUIDSet> uuidSets = gtidSet.getUUIDSets();
        int dataSize = 8; // number of uuidSets
        for (GtidSet.UUIDSet uuidSet : uuidSets) {
            dataSize += 16 /* uuid */ + 8 /* number of intervals */ +
                    uuidSet.getIntervals().size() /* number of intervals */ * 16 /* start-end */;
        }
        BufferUtil.writeInteger(buffer, dataSize, 4);
        BufferUtil.writeLong(buffer, uuidSets.size(), 8);
        for (GtidSet.UUIDSet uuidSet : uuidSets) {
            buffer.writeBytes(hexToByteArray(uuidSet.getUUID().replace("-", "")));
            Collection<GtidSet.Interval> intervals = uuidSet.getIntervals();
            BufferUtil.writeLong(buffer, intervals.size(), 8);
            for (GtidSet.Interval interval : intervals) {
                BufferUtil.writeLong(buffer, interval.getStart(), 8);
                BufferUtil.writeLong(buffer, interval.getEnd() + 1 /* right-open */, 8);
            }
        }

        return buffer;
    }

    @Override
    public int calcPacketSize() {
        return 1 + 2 + 4 + 4 + binlogFilename.length() + 8 + 4 + 8 + getGtidSetLength();
    }

    private int getGtidSetLength() {
        Collection<GtidSet.UUIDSet> uuidSets = gtidSet.getUUIDSets();
        int sum = 0;
        for (GtidSet.UUIDSet uuidSet : uuidSets) {
            sum += hexToByteArray(uuidSet.getUUID().replace("-", "")).length;
            sum += 8;
            Collection<GtidSet.Interval> intervals = uuidSet.getIntervals();
            for (GtidSet.Interval interval : intervals) {
                sum += 16;
            }
        }
        return sum;
    }

    private static byte[] hexToByteArray(String uuid) {
        byte[] b = new byte[uuid.length() / 2];
        for (int i = 0, j = 0; j < uuid.length(); j += 2) {
            b[i++] = (byte) Integer.parseInt(uuid.charAt(j) + "" + uuid.charAt(j + 1), 16);
        }
        return b;
    }

    @Override
    protected String getPacketInfo() {
        return "DumpBinaryLogCommand";
    }

}
