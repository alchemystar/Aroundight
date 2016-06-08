/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.net.handler.backend.codec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.aroundigit.common.net.proto.mysql.binlog.DeleteRowsEventData;

/**
 * DeleteRowsEventDecoder
 *
 * @Author lizhuyang
 */
public class DeleteRowsEventDecoder implements EventDataDecoder<DeleteRowsEventData> {

    private static final Logger logger = LoggerFactory.getLogger(DeleteRowsEventDecoder.class);

    public DeleteRowsEventData decode(byte[] data) {
        DeleteRowsEventData eventData = new DeleteRowsEventData();
        eventData.read(data);
        logger.debug(eventData.toString());
        return eventData;
    }

}
