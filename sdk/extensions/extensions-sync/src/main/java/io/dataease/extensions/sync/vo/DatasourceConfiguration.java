package io.dataease.extensions.sync.vo;

import io.dataease.extensions.datasource.vo.Configuration;
import io.dataease.extensions.sync.model.datasource.DatasourceRole;
import lombok.Data;

import java.util.List;

@Data
public class DatasourceConfiguration extends Configuration {
    private List<String> illegalParameters;
    private List<String> showTableSqls;

    public enum DatasourceType {
        mysql("mysql", "MySQL", "OLTP", "`", "`", 27, DatasourceRole.SOURCE, "mySQLSourceProvider"),
        es("elasticsearch", "Elasticsearch", "OLAP", "\"", "\"", 14, DatasourceRole.SOURCE, "elasticsearchSourceProvider"),
        doris("doris", "Apache Doris", "OLAP", "`", "`", 26, DatasourceRole.TARGET, "dorisSinkProvider"),
        oracle("oracle", "ORACLE", "OLTP", "\"", "\"", 1, DatasourceRole.SOURCE, "oracleSourceProvider"),
        db2("db2", "DB2", "OLTP", "", "", 12, DatasourceRole.SOURCE, "db2SourceProvider"),
        sqlServer("sqlServer", "SQL Server", "DL", "[", "]", 2, DatasourceRole.SOURCE, "sqlServerSourceProvider");
        private String type;
        private String name;
        private Integer flag;
        private String catalog;
        private String prefix;
        private String suffix;
        // 与 per_sync_datasource.datasource_role、任务 JSON 和插件元数据一致：1 为源端，2 为目标端。
        private Integer datasourceRole;
        private String providerClassName;

        DatasourceType(String type, String name, String catalog, String prefix, String suffix, Integer flag, Integer datasourceRole, String providerClassName) {
            this.type = type;
            this.name = name;
            this.catalog = catalog;
            this.prefix = prefix;
            this.suffix = suffix;
            this.flag = flag;
            this.datasourceRole = datasourceRole;
            this.providerClassName = providerClassName;
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

        public Integer getDatasourceRole() {
            return datasourceRole;
        }

        public String getProviderClassName() {
            return providerClassName;
        }
    }
}
