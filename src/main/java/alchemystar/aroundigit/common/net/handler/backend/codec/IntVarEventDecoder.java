/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.net.handler.backend.codec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.aroundigit.common.net.proto.mysql.binlog.IntVarEventData;

/**
 * IntVarEventDecoder
 *
 * @Author lizhuyang
 */
public class IntVarEventDecoder implements EventDataDecoder<IntVarEventData> {

    private static final Logger logger = LoggerFactory.getLogger(IntVarEventDecoder.class);

    public IntVarEventData decode(byte[] data) {
        IntVarEventData eventData = new IntVarEventData();
        eventData.read(data);
        logger.debug(eventData.toString());
        return eventData;
    }

}