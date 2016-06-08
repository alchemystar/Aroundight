/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.lancelot.route.test;

import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;

import alchemystar.lancelot.route.LancelotStmtParser;

/**
 * LancelotParseTest
 *
 * @Author lizhuyang
 */
public class LancelotParseTest {

    public static void main(String args[]) {
       String sql = "select concat_ws(',',a.F_front_bank_code,count(distinct a.f_order_no)) from t_cardinfo a where "
                + "a.f_sp_no='1' and a.f_code != 0  and a.f_order_no !=0  and not exists (select 1 from t_cardinfo  b"
                + " where b.f_sp_no='2' and b.f_code = 0 and b.f_order_no=a.f_order_no) and id=9 group by a"
                + ".F_front_bank_code;";

    }

}
