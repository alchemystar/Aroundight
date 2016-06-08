/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.lancelot.route;

import java.util.Map;

import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;

import alchemystar.lancelot.common.net.route.RouteResultset;
import alchemystar.lancelot.common.net.route.RouteResultsetNode;
import alchemystar.lancelot.loader.XmlLoader;
import alchemystar.lancelot.parser.ServerParse;
import alchemystar.lancelot.route.visitor.LancelotOutputVisitor;
import alchemystar.lancelot.route.visitor.LancelotTbSuffixVisitor;

/**
 * LancelotStmtParser
 *
 * @Author lizhuyang
 */
public class LancelotStmtParser {

    private static final Integer dbCount = 0;
    private static final Integer tbCount = 2;

    public static RouteResultset parser(String sql, int sqlType) {
        switch (sqlType) {
            case ServerParse.SELECT:
                return multiParse(sql);
        }
        return null;
    }

    // 当前固定multiSet
    public static RouteResultset multiParse(String sql) {
        RouteResultset routeResultset = new RouteResultset();
        RouteResultsetNode[] nodes = new RouteResultsetNode[tbCount];
        for (int i = 1; i <= tbCount; i++) {
            MySqlStatementParser parser = new MySqlStatementParser(sql);
            SQLSelectStatement selectStmt = parser.parseSelect();
            StringBuilder builder = new StringBuilder();
            LancelotTbSuffixVisitor lanceVisitor = new LancelotTbSuffixVisitor(builder, "_" + i);
            selectStmt.accept(lanceVisitor);
            RouteResultsetNode node = new RouteResultsetNode(String.valueOf(i), builder.toString(), ServerParse.SELECT);
            nodes[i - 1] = node;
        }
        routeResultset.setNodes(nodes);
        return routeResultset;
    }

    public static RouteResultset parseSelect(String sql) {
        RouteResultset routeResultset = new RouteResultset();

        MySqlStatementParser parser = new MySqlStatementParser(sql);
        SQLSelectStatement selectStmt = parser.parseSelect();
        SchemaStatVisitor visitor = new SchemaStatVisitor();
        selectStmt.accept(visitor);
        Map<TableStat.Name, TableStat> tableStatMap = visitor.getTables();
        boolean hit = false;
        for (TableStat.Name name : tableStatMap.keySet()) {
            for (String table : XmlLoader.getTableRules().keySet()) {
                if (name.getName().equals(table)) {
                    hit = true;
                }
            }
        }
        if (hit) {
            RouteResultsetNode node = new RouteResultsetNode("1", sql, ServerParse.SELECT);
            RouteResultsetNode[] nodes = new RouteResultsetNode[1];
            nodes[0] = node;
            routeResultset.setNodes(nodes);
            return routeResultset;

        }
        //命中的情况
        RouteResultsetNode routeResultsetNode = null;
        for (TableStat.Condition condition : visitor.getConditions()) {
            if (XmlLoader.getHitColumns().contains(condition.getColumn().getName())) {
                // column hit
                StringBuilder buider = new StringBuilder();
                LancelotOutputVisitor lancelotOutputVisitor =
                        new LancelotOutputVisitor(buider, XmlLoader.getTableRules(), visitor);
                routeResultsetNode = new RouteResultsetNode("1", lancelotOutputVisitor.toString
                        (), ServerParse.SELECT);
                RouteResultsetNode[] nodes = new RouteResultsetNode[1];
                nodes[0] = routeResultsetNode;
                routeResultset.setNodes(nodes);
                return routeResultset;
            }
        }
        // 有table hit,但无column hit

        return null;
    }

}
