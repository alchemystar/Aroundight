/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.net.handler.backend.codec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.aroundigit.common.net.proto.mysql.binlog.UpdateRowsEventData;

/**
 * UpdateRowsEventData
 *
 * @Author lizhuyang
 */
public class UpdateRowsEventDecoder implements EventDataDecoder<UpdateRowsEventData> {

    private static final Logger logger = LoggerFactory.getLogger(RotateEventDecoder.class);

    public UpdateRowsEventData decode(byte[] data) {
        UpdateRowsEventData eventData = new UpdateRowsEventData();
        eventData.read(data);
        logger.debug(eventData.toString());
        return eventData;
    }

}
