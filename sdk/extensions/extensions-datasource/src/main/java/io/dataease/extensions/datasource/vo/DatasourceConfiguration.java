package io.dataease.extensions.datasource.vo;

import lombok.Data;

import java.util.List;

@Data
public class DatasourceConfiguration extends Configuration {
    private List<String> illegalParameters;
    private List<String> showTableSqls;


    static public enum DatasourceType {
        folder("folder", "folder", "folder", null, null),
        API("API", "API", "API", "`", "`"),
        Excel("Excel", "Excel", "LOCALFILE", "`", "`"),
        mysql("mysql", "Mysql", "OLTP", "`", "`"),
        impala("impala", "Apache Impala", "OLAP", "`", "`"),
        mariadb("mariadb", "Mariadb", "OLTP", "`", "`"),
        StarRocks("StarRocks", "StarRocks", "OLAP", "`", "`"),
        doris("doris", "Apache Doris", "OLAP", "`", "`"),
        TiDB("TiDB", "TiDB", "OLTP", "`", "`"),
        oracle("oracle", "ORACLE", "OLTP", "\"", "\""),
        pg("pg", "PostgreSQL", "OLTP", "\"", "\""),
        redshift("redshift", "AWS Redshift", "OLTP", "\"", "\""),
        db2("db2", "Db2", "OLTP", "", ""),
        ck("ck", "Clickhouse", "OLAP", "`", "`"),
        h2("h2", "H2", "OLAP", "\"", "\""),
        sqlServer("sqlServer", "Sqlserver", "DL", "[", "]"),
        mongo("mongo", "MongoDB", "DL", "`", "`");

        private String type;
        private String name;

        private String catalog;
        private String prefix;
        private String suffix;

        DatasourceType(String type, String name, String catalog, String prefix, String suffix) {
            this.type = type;
            this.name = name;
            this.catalog = catalog;
            this.prefix = prefix;
            this.suffix = suffix;
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public String getCatalog() {
            return catalog;
        }

        public String getPrefix() {
            return prefix;
        }

        public String getSuffix() {
            return suffix;
        }
    }
}
