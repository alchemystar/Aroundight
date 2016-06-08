/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.lancelot.common.net.handler.frontend;

import alchemystar.lancelot.common.net.proto.util.ErrorCode;

/**
 * BeginHandler
 *
 * @Author lizhuyang
 */
public final class BeginHandler {

    public static void handle(String stmt, FrontendConnection c) {
        c.writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Unsupported statement");
    }

}