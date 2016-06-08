/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.lancelot.common.net.handler.frontend;

import alchemystar.lancelot.common.net.response.ShowDatabases;
import alchemystar.lancelot.parser.ServerParse;
import alchemystar.lancelot.parser.ServerParseShow;

/**
 * ShowHandler
 * @Author lizhuyang
 */
public final class ShowHandler {
    public static void handle(String stmt, FrontendConnection c, int offset) {
        switch (ServerParseShow.parse(stmt, offset)) {
            case ServerParseShow.DATABASES:
                ShowDatabases.response(c);
                break;
            default:
              // todo datasource
                c.execute(stmt, ServerParse.SHOW);
                break;
        }
    }
}