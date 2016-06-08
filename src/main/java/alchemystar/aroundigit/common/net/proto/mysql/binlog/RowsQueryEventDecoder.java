/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.net.proto.mysql.binlog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.aroundigit.common.net.handler.backend.codec.EventDataDecoder;

/**
 * RowsQueryEventDecoder
 *
 * @Author lizhuyang
 */
public class RowsQueryEventDecoder implements EventDataDecoder<RowsQueryEventData> {

    private static final Logger logger = LoggerFactory.getLogger(RowsQueryEventDecoder.class);

    public RowsQueryEventData decode(byte[] data) {
        RowsQueryEventData eventData = new RowsQueryEventData();
        eventData.read(data);
        logger.debug(eventData.toString());
        return eventData;
    }
}