/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.lancelot.common.net.route;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RouteResultsetNode
 *
 * @Author lizhuyang
 */
public class RouteResultsetNode {

    private static final Logger logger = LoggerFactory.getLogger(RouteResultsetNode.class);

    private  String name; // 数据节点名称,对每一个连接 需唯一
    private  String statement; // 执行的语句
    private  int sqlType;

    public RouteResultsetNode(String name, String statement, int sqlType) {
        this.name = name;
        this.statement = statement;
        this.sqlType = sqlType;
    }

    public String getName() {
        return name;
    }

    public String getStatement() {
        return statement;
    }

    public int getSqlType() {
        return sqlType;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof RouteResultsetNode) {
            RouteResultsetNode rrn = (RouteResultsetNode) obj;
            if (equals(name, rrn.getName())) {
                return true;
            }
        }
        return false;
    }

    private static boolean equals(String str1, String str2) {
        if (str1 == null) {
            return str2 == null;
        }
        return str1.equals(str2);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

}
