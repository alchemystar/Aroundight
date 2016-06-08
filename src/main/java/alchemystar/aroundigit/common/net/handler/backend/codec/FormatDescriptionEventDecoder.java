/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.net.handler.backend.codec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.aroundigit.common.net.proto.mysql.binlog.FormatDescriptionEventData;

/**
 * FormatDescriptionEventDecoder
 *
 * @Author lizhuyang
 */
public class FormatDescriptionEventDecoder implements EventDataDecoder<FormatDescriptionEventData> {
    private static final Logger logger = LoggerFactory.getLogger(FormatDescriptionEventDecoder.class);

    public FormatDescriptionEventData decode(byte[] data) {
        FormatDescriptionEventData eventData = new FormatDescriptionEventData();
        eventData.read(data);
        logger.debug(eventData.toString());
        return eventData;
    }
}
