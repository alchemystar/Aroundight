/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.lancelot.route.visitor;

import com.alibaba.druid.sql.ast.SQLCommentHint;
import com.alibaba.druid.sql.ast.SQLSetQuantifier;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlOutputVisitor;

/**
 * @Author lizhuyang
 */
public class LancelotTbSuffixVisitor extends MySqlOutputVisitor {

    private String tableSuffix = null;

    public LancelotTbSuffixVisitor(Appendable appender, String tableSuffix) {
        super(appender);
        this.tableSuffix = tableSuffix;
    }

    @Override
    public boolean visit(MySqlSelectQueryBlock x) {
        if (x.getOrderBy() != null) {
            x.getOrderBy().setParent(x);
        }

        print0(ucase ? "SELECT " : "select ");

        for (int i = 0, size = x.getHintsSize(); i < size; ++i) {
            SQLCommentHint hint = x.getHints().get(i);
            hint.accept(this);
            print(' ');
        }

        if (SQLSetQuantifier.ALL == x.getDistionOption()) {
            print0(ucase ? "ALL " : "all ");
        } else if (SQLSetQuantifier.DISTINCT == x.getDistionOption()) {
            print0(ucase ? "DISTINCT " : "distinct ");
        } else if (SQLSetQuantifier.DISTINCTROW == x.getDistionOption()) {
            print0(ucase ? "DISTINCTROW " : "distinctrow ");
        }

        if (x.isHignPriority()) {
            print0(ucase ? "HIGH_PRIORITY " : "high_priority ");
        }

        if (x.isStraightJoin()) {
            print0(ucase ? "STRAIGHT_JOIN " : "straight_join ");
        }

        if (x.isSmallResult()) {
            print0(ucase ? "SQL_SMALL_RESULT " : "sql_small_result ");
        }

        if (x.isBigResult()) {
            print0(ucase ? "SQL_BIG_RESULT " : "sql_big_result ");
        }

        if (x.isBufferResult()) {
            print0(ucase ? "SQL_BUFFER_RESULT " : "sql_buffer_result ");
        }

        if (x.getCache() != null) {
            if (x.getCache().booleanValue()) {
                print0(ucase ? "SQL_CACHE " : "sql_cache ");
            } else {
                print0(ucase ? "SQL_NO_CACHE " : "sql_no_cache ");
            }
        }

        if (x.isCalcFoundRows()) {
            print0(ucase ? "SQL_CALC_FOUND_ROWS " : "sql_calc_found_rows ");
        }

        printSelectList(x.getSelectList());

        if (x.getInto() != null) {
            println();
            print0(ucase ? "INTO " : "into ");
            x.getInto().accept(this);
        }

        if (x.getFrom() != null) {
            println();
            print0(ucase ? "FROM " : "from ");
            // modify lizhuyang
            renderTB(x);
            x.getFrom().accept(this);
        }

        if (x.getWhere() != null) {
            println();
            print0(ucase ? "WHERE " : "where ");
            x.getWhere().setParent(x);
            x.getWhere().accept(this);
        }

        if (x.getGroupBy() != null) {
            println();
            x.getGroupBy().accept(this);
        }

        if (x.getOrderBy() != null) {
            println();
            x.getOrderBy().accept(this);
        }

        if (x.getLimit() != null) {
            println();
            x.getLimit().accept(this);
        }

        if (x.getProcedureName() != null) {
            print0(ucase ? " PROCEDURE " : " procedure ");
            x.getProcedureName().accept(this);
            if (!x.getProcedureArgumentList().isEmpty()) {
                print('(');
                printAndAccept(x.getProcedureArgumentList(), ", ");
                print(')');
            }
        }

        if (x.isForUpdate()) {
            println();
            print0(ucase ? "FOR UPDATE" : "for update");
        }

        if (x.isLockInShareMode()) {
            println();
            print0(ucase ? "LOCK IN SHARE MODE" : "lock in share mode");
        }

        return false;
    }

    private void renderTB(MySqlSelectQueryBlock x) {
        if (x.getFrom() instanceof SQLExprTableSource) {
            SQLExprTableSource from = (SQLExprTableSource) x.getFrom();
            if (from.getExpr() instanceof SQLIdentifierExpr) {
                SQLIdentifierExpr expr = (SQLIdentifierExpr) from.getExpr();
                expr.setName(expr.getName() + tableSuffix);
            }
        }
    }
}
