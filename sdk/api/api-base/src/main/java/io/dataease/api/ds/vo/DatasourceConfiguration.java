package io.dataease.api.ds.vo;

import lombok.Data;

import java.util.List;

@Data
public class DatasourceConfiguration extends Configuration{
    private List<String> illegalParameters;
    private List<String> showTableSqls;


    static public enum DatasourceType {
        API("API", "API", "API"),
        Excel("Excel", "Excel", "LOCALFILE"),
        mysql("mysql", "Mysql", "OLTP"),
        mariadb("mariadb", "Mariadb", "OLTP"),
        StarRocks("StarRocks", "StarRocks", "OLAP"),
        TiDB("TiDB", "TiDB", "OLTP"),
        oracle("oracle", "ORACLE", "OLTP"),
        pg("pg", "PostgreSQL", "OLTP"),
        redshift("redshift", "AWS Redshift", "OLTP"),
        db2("db2", "Db2", "OLTP"),
        ck("ck", "Clickhouse", "OLAP"),
        h2("h2", "H2", "OLAP"),
        sqlServer("sqlServer", "Sqlserver", "DL");

        private String type;
        private String name;

        private String catalog;

        DatasourceType(String type, String name, String catalog) {
            this.type = type;
            this.name = name;
            this.catalog = catalog;
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
    }
}
