/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.net.proto.mysql.binlog;

import alchemystar.aroundigit.common.net.proto.mysql.MySQLMessage;

/**
 * EventHeader
 *
 * @Author lizhuyang
 */
public class EventHeader {

    private static final EventType[] EVENT_TYPES = EventType.values();
    // for convenience the next event body
    private MySQLMessage mm = null;
    // v1 (MySQL 3.23)
    public long timestamp;
    public EventType eventType;
    public long serverId;
    public long eventLength;
    // v3 (MySQL 4.0.2-4.1)
    public long nextPosition;
    public int flags;

    public EventHeader(MySQLMessage mm) {
        this.mm = mm;
    }

    public void read() {
        timestamp = mm.readUB4() * 1000;
        eventType = getEventType(mm.read());
        serverId = mm.readUB4();
        eventLength = mm.readUB4();
        nextPosition = mm.readUB4();
        flags = mm.readUB2();
    }

    private static EventType getEventType(int ordinal) {
        if (ordinal >= EVENT_TYPES.length) {
            throw new RuntimeException("Unknown event type " + ordinal);
        }
        return EVENT_TYPES[ordinal];
    }

    public long getHeaderLength() {
        return 19;
    }

    public long getDataLength() {
        return getEventLength() - getHeaderLength();
    }

    public long getEventLength() {
        return eventLength;
    }

    public MySQLMessage getMm() {
        return mm;
    }

    public void setMm(MySQLMessage mm) {
        this.mm = mm;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("EventHeaderV4");
        sb.append("{timestamp=").append(timestamp);
        sb.append(", eventType=").append(eventType);
        sb.append(", serverId=").append(serverId);
        sb.append(", headerLength=").append(getHeaderLength());
        sb.append(", dataLength=").append(getDataLength());
        sb.append(", nextPosition=").append(nextPosition);
        sb.append(", flags=").append(flags);
        sb.append('}');
        return sb.toString();
    }
}
