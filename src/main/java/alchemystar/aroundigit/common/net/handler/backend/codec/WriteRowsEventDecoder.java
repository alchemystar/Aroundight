/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.net.handler.backend.codec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.aroundigit.common.net.proto.mysql.binlog.WriteRowsEventData;

/**
 * WriteRowsEventDecoder
 *
 * @Author lizhuyang
 */
public class WriteRowsEventDecoder implements EventDataDecoder<WriteRowsEventData> {

    private static final Logger logger = LoggerFactory.getLogger(RotateEventDecoder.class);

    public WriteRowsEventData decode(byte[] data) {
        WriteRowsEventData eventData = new WriteRowsEventData();
        eventData.read(data);
        logger.debug(eventData.toString());
        return eventData;
    }
}