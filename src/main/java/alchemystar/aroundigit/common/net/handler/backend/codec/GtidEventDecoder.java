/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.net.handler.backend.codec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.aroundigit.common.net.proto.mysql.binlog.GtidEventData;

/**
 * GtidEventDecoder
 *
 * @Author lizhuyang
 */
public class GtidEventDecoder implements EventDataDecoder<GtidEventData> {

    private static final Logger logger = LoggerFactory.getLogger(GtidEventDecoder.class);

    public GtidEventData decode(byte[] data) {
        GtidEventData eventData = new GtidEventData();
        eventData.read(data);
        logger.debug(eventData.toString());
        return eventData;
    }
}
