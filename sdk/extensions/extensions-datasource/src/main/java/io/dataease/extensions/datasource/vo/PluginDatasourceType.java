package io.dataease.extensions.datasource.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author Junjun
 */
@Data
public class PluginDatasourceType extends Configuration {
    private List<String> illegalParameters;
    private List<String> showTableSqls;


    static public enum DatasourceType {
        hive("hive", "Apache Hive", "DL", "`", "`");

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
