package io.dataease.engine.sql;

/**
 * @Author Junjun
 */
public class SqlTemplate {
    public static String QUERY_SQL = "querySql(limitFiled, groups, aggregators, filters, orders, table, notUseAs, useAliasForGroup)\n" +
            "::=<<\n" +
            "SELECT\n" +
            "<if(limitFiled)>\n" +
            "   <limitFiled.limitFiled>\n" +
            "<endif>\n" +
            "<if(!groups && !aggregators)>\n" +
            "    *\n" +
            "<endif>\n" +
            "<if(groups && notUseAs)>\n" +
            "    <groups:{group|<if(group)><group.fieldName> <endif>}; separator=\",\\n\">\n" +
            "<endif>\n" +
            "<if(groups && !notUseAs)>\n" +
            "    <groups:{group|<if(group)><group.fieldName> AS <group.fieldAlias><endif>}; separator=\",\\n\">\n" +
            "<endif>\n" +
            "    <if(groups && aggregators)>,<endif>\n" +
            "<if(aggregators)>\n" +
            "    <aggregators:{agg|<if(agg)><agg.fieldName> AS <agg.fieldAlias><endif>}; separator=\",\\n\">\n" +
            "<endif>\n" +
            "FROM\n" +
            "    <table.tableName>   <table.tableAlias>\n" +
            "<if(filters)>\n" +
            "WHERE\n" +
            "    <filters:{filter|<if(filter)><filter><endif>}; separator=\"\\nAND \">\n" +
            "<endif>\n" +
            "<if(groups && !useAliasForGroup)>\n" +
            "GROUP BY\n" +
            "    <groups:{group|<if(group)><group.fieldName><endif>}; separator=\",\\n\">\n" +
            "<endif>\n" +
            "<if(groups && useAliasForGroup)>\n" +
            "GROUP BY\n" +
            "    <groups:{group|<if(group)><group.fieldAlias><endif>}; separator=\",\\n\">\n" +
            "<endif>\n" +
            "<if(orders)>\n" +
            "ORDER BY\n" +
            "    <orders:{order|<if(order)><order.orderAlias> <order.orderDirection><endif>}; separator=\",\\n\">\n" +
            "<endif>\n" +
            ">>";

    public static String PREVIEW_SQL = "previewSql(limitFiled, groups, aggregators, filters, orders, table, isGroup, notUseAs, useAliasForGroup)\n" +
            "::=<<\n" +
            "SELECT\n" +
            "<if(limitFiled)>\n" +
            "   <limitFiled.limitFiled>\n" +
            "<endif>\n" +
            "<if(!groups && !aggregators)>\n" +
            "    *\n" +
            "<endif>\n" +
            "<if(groups && notUseAs)>\n" +
            "    <groups:{group|<if(group)><group.fieldName> <endif>}; separator=\",\\n\">\n" +
            "<endif>\n" +
            "<if(groups && !notUseAs)>\n" +
            "    <groups:{group|<if(group)><group.fieldName> AS <group.fieldAlias><endif>}; separator=\",\\n\">\n" +
            "<endif>\n" +
            "    <if(groups && aggregators)>,<endif>\n" +
            "<if(aggregators)>\n" +
            "    <aggregators:{agg|<if(agg)><agg.fieldName> AS <agg.fieldAlias><endif>}; separator=\",\\n\">\n" +
            "<endif>\n" +
            "FROM\n" +
            "    <table.tableName>   <table.tableAlias>\n" +
            "<if(filters)>\n" +
            "WHERE\n" +
            "    <filters:{filter|<if(filter)><filter><endif>}; separator=\"\\nAND \">\n" +
            "<endif>\n" +
            "<if(isGroup && groups && !useAliasForGroup)>\n" +
            "GROUP BY\n" +
            "    <groups:{group|<if(group)><group.fieldName><endif>}; separator=\",\\n\">\n" +
            "<endif>\n" +
            "<if(isGroup && groups && useAliasForGroup)>\n" +
            "GROUP BY\n" +
            "    <groups:{group|<if(group)><group.fieldAlias><endif>}; separator=\",\\n\">\n" +
            "<endif>\n" +
            "<if(orders)>\n" +
            "ORDER BY\n" +
            "    <orders:{order|<if(order)><order.orderAlias> <order.orderDirection><endif>}; separator=\",\\n\">\n" +
            "<endif>\n" +
            ">>";
}
