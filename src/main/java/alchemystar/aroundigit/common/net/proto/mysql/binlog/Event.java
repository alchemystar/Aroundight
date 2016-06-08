/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.net.proto.mysql.binlog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.aroundigit.common.net.handler.backend.codec.DecoderConfig;
import alchemystar.aroundigit.common.net.handler.backend.codec.EventDataDecoder;
import alchemystar.aroundigit.common.net.proto.mysql.BinaryPacket;
import alchemystar.aroundigit.common.net.proto.mysql.MySQLMessage;

/**
 * Binlog Event
 *
 * @Author lizhuyang
 */
public class Event {

    private static final Logger logger = LoggerFactory.getLogger(Event.class);

    private EventHeader header;
    private EventData data;

    public void read(BinaryPacket bin) {
        MySQLMessage mm = new MySQLMessage(bin.data);
        // skip the marker
        mm.read();
        EventHeader eventHeader = new EventHeader(mm);
        eventHeader.read();
        EventDataDecoder decoder = DecoderConfig.getDecoder(eventHeader.eventType);
        if (decoder != null) {
            data = decoder.decode(mm.readBytes());
        } else {
            logger.warn("don't support this event type,type="+eventHeader.eventType);
        }
    }

    public EventHeader getHeader() {
        return header;
    }

    public void setHeader(EventHeader header) {
        this.header = header;
    }

    public EventData getData() {
        return data;
    }

    public void setData(EventData data) {
        this.data = data;
    }
}
