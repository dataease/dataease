package io.dataease.config;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.dialect.DmDialect;

import org.hibernate.mapping.Table;
import org.hibernate.tool.schema.extract.spi.TableInformation;
import org.hibernate.tool.schema.internal.StandardTableExporter;
import org.hibernate.tool.schema.internal.StandardTableMigrator;
import org.hibernate.tool.schema.internal.TableMigrator;
import org.hibernate.tool.schema.spi.Exporter;


public class CustomDMDialect extends DmDialect {

    @Override
    public Exporter<org.hibernate.mapping.Table> getTableExporter() {
        return new StandardTableExporter(this) {
            @Override
            public String[] getSqlCreateStrings(org.hibernate.mapping.Table table, Metadata metadata, SqlStringGenerationContext context) {
                String[] createStrings = super.getSqlCreateStrings(table, metadata, context);
                return processBooleanDefaults(createStrings);
            }

            private String[] processBooleanDefaults(String[] sqlStrings) {
                for (int i = 0; i < sqlStrings.length; i++) {
                    sqlStrings[i] = sqlStrings[i]
                            .replace(" default false", " DEFAULT 0")
                            .replace(" default true", " DEFAULT 1")
                            .replace(" varbinary(16777216)", " BLOB")
                            .replace(" varchar(16777216)", " TEXT");
                }
                return sqlStrings;
            }
        };
    }

    @Override
    public String toBooleanValueString(boolean bool) {
        return bool ? "1" : "0";
    }

    @Override
    public TableMigrator getTableMigrator() {
        TableMigrator delegate = new StandardTableMigrator(this);
        return (Table table, Metadata metadata, TableInformation tableInfo, SqlStringGenerationContext context) -> {
            // 拿到原生生成的完整alter语句数组
            String[] sqls = delegate.getSqlAlterStrings(table, metadata, tableInfo, context);
            for (int i = 0; i < sqls.length; i++) {
                String sql = sqls[i];
                sql = sql.replaceAll(" default false", " default 0");
                sql = sql.replaceAll(" default true", " default 1");
                sqls[i] = sql;
            }
            return sqls;
        };
    }
}
