/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.lancelot.common.net.route;

/**
 * 路由结果集
 *
 * @Author lizhuyang
 */
public class RouteResultset {

    private String statement; // 原始语句
    private int sqlType;
    private RouteResultsetNode[] nodes; // 路由结果节点

    public String getStatement() {
        return statement;
    }

    public int getNodeCount(){
        if(nodes == null){
            return 0;
        }else{
            return nodes.length;
        }
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public int getSqlType() {
        return sqlType;
    }

    public void setSqlType(int sqlType) {
        this.sqlType = sqlType;
    }

    public RouteResultsetNode[] getNodes() {
        return nodes;
    }

    public void setNodes(RouteResultsetNode[] nodes) {
        this.nodes = nodes;
    }


}
