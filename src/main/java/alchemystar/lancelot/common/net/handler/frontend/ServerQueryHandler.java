/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.lancelot.common.net.handler.frontend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.lancelot.common.net.proto.util.ErrorCode;
import alchemystar.lancelot.parser.ServerParse;

/**
 * ServerQueryHandler
 *
 * @Author lizhuyang
 */
public class ServerQueryHandler implements FrontendQueryHandler {

    private static final Logger logger = LoggerFactory.getLogger(ServerQueryHandler.class);

    private FrontendConnection source;

    public ServerQueryHandler(FrontendConnection source) {
        this.source = source;
    }

    public void query(String sql) {

        logger.debug("sql = "+sql);

        int rs = ServerParse.parse(sql);
        switch (rs & 0xff) {
            case ServerParse.EXPLAIN:
                // ExplainHandler.handle(sql, c, rs >>> 8);
                break;
            case ServerParse.SET:
                 SetHandler.handle(sql, source, rs >>> 8);
                break;
            case ServerParse.SHOW:
                // todo data source
                ShowHandler.handle(sql, source, rs >>> 8);
                break;
            case ServerParse.SELECT:
                 SelectHandler.handle(sql, source, rs >>> 8);
                break;
            case ServerParse.START:
                StartHandler.handle(sql, source, rs >>> 8);
                break;
            case ServerParse.BEGIN:
                BeginHandler.handle(sql, source );
                break;
            case ServerParse.SAVEPOINT:
                 SavepointHandler.handle(sql, source);
                break;
            case ServerParse.KILL:
                KillHandler.handle(sql, rs >>> 8, source);
                break;
            case ServerParse.KILL_QUERY:
                source.writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Unsupported command");
                break;
            case ServerParse.USE:
                UseHandler.handle(sql, source, rs >>> 8);
                break;
            case ServerParse.COMMIT:
                source.commit();
                break;
            case ServerParse.ROLLBACK:
                source.rollback();
                break;
            default:
                source.execute(sql, rs);
        }
    }
}
