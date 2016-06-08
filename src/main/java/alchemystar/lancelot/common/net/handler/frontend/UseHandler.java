/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.lancelot.common.net.handler.frontend;

import alchemystar.lancelot.common.net.proto.util.ErrorCode;

/**
 * UseHandler
 *
 * @Author lizhuyang
 */
public final class UseHandler {

    public static void handle(String sql, FrontendConnection c, int offset) {
        String schema = sql.substring(offset).trim();
        int length = schema.length();
        if (length > 0) {
            if (schema.charAt(0) == '`' && schema.charAt(length - 1) == '`') {
                schema = schema.substring(1, length - 2);
            }
        }

        // 表示当前连接已经指定了schema
        if (c.getSchema() != null) {
            if (c.getSchema().equals(schema)) {
                c.writeOk();
            } else {
                c.writeErrMessage(ErrorCode.ER_DBACCESS_DENIED_ERROR, "Not allowed to change the database!");
            }
            return;
        }
        c.setSchema(schema);
        c.writeOk();

    }

}