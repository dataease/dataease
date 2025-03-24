package io.dataease.extensions.datasource.vo;

import lombok.Data;

import java.util.List;

@Data
public class DatasourceConfiguration extends Configuration {
    private List<String> illegalParameters;
    private List<String> showTableSqls;


    static public enum DatasourceType {
        folder("folder", "folder", "folder", null, null, 25),
        API("API", "API", "API", "`", "`", 15),
        Excel("Excel", "Excel", "LOCALFILE", "`", "`", 16),
        ExcelRemote("ExcelRemote", "ExcelRemote", "LOCALFILE", "`", "`", 29),
        mysql("mysql", "Mysql", "OLTP", "`", "`", 27),
        impala("impala", "Apache Impala", "OLAP", "`", "`", 5),
        mariadb("mariadb", "Mariadb", "OLTP", "`", "`", 6),
        StarRocks("StarRocks", "StarRocks", "OLAP", "`", "`", 7),
        es("es", "Elasticsearch", "OLAP", "\"", "\"", 14),
        doris("doris", "Apache Doris", "OLAP", "`", "`", 26),
        TiDB("TiDB", "TiDB", "OLTP", "`", "`", 3),
        oracle("oracle", "ORACLE", "OLTP", "\"", "\"", 1),
        pg("pg", "PostgreSQL", "OLTP", "\"", "\"", 9),
        redshift("redshift", "AWS Redshift", "OLTP", "\"", "\"", 13),
        db2("db2", "Db2", "OLTP", "", "", 12),
        ck("ck", "Clickhouse", "OLAP", "`", "`", 11),
        h2("h2", "H2", "OLAP", "\"", "\"", 30),
        sqlServer("sqlServer", "Sqlserver", "DL", "[", "]", 2),
        mongo("mongo", "MongoDB", "DL", "`", "`", 10);

        private String type;
        private String name;
        private Integer flag;
        private String catalog;
        private String prefix;
        private String suffix;

        DatasourceType(String type, String name, String catalog, String prefix, String suffix, Integer flag) {
            this.type = type;
            this.name = name;
            this.catalog = catalog;
            this.prefix = prefix;
            this.suffix = suffix;
            this.flag = flag;
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

        public Integer getFlag() {
            return flag;
        }
    }
}
