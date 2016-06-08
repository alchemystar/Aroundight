/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.net.handler.backend.codec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.aroundigit.common.net.proto.mysql.binlog.RotateEventData;

/**
 * RotateEventDecoder
 *
 * @Author lizhuyang
 */
public class RotateEventDecoder implements EventDataDecoder<RotateEventData> {

    private static final Logger logger = LoggerFactory.getLogger(RotateEventDecoder.class);

    public RotateEventData decode(byte[] data) {
        RotateEventData eventData = new RotateEventData();
        eventData.read(data);
        logger.debug(eventData.toString());
        return eventData;
    }
}
