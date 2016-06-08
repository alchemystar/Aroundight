/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.lancelot.common.net.handler.frontend;

import alchemystar.lancelot.common.net.proto.util.ErrorCode;
import alchemystar.lancelot.parser.ServerParse;
import alchemystar.lancelot.parser.ServerParseStart;

/**
 * StartHandler
 *
 * @Author lizhuyang
 */
public final class StartHandler {

    public static void handle(String stmt, FrontendConnection c, int offset) {
        switch (ServerParseStart.parse(stmt, offset)) {
            case ServerParseStart.TRANSACTION:
                c.writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Unsupported statement");
                break;
            default:
                // todo data source
                  c.execute(stmt, ServerParse.START);
                break;
        }
    }

}