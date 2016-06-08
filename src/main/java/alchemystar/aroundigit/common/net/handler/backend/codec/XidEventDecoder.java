/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.net.handler.backend.codec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.aroundigit.common.net.proto.mysql.binlog.XidEventData;

/**
 * XidEventDecoder
 *
 * @Author lizhuyang
 */
public class XidEventDecoder implements EventDataDecoder<XidEventData> {

    private static final Logger logger = LoggerFactory.getLogger(XidEventDecoder.class);

    public XidEventData decode(byte[] data) {
        XidEventData eventData = new XidEventData();
        eventData.read(data);
        logger.debug(eventData.toString());
        return eventData;
    }

}
