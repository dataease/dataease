package io.dataease.config;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.tool.schema.internal.StandardTableExporter;
import org.hibernate.tool.schema.spi.Exporter;

public class CustomSQLServerDialect extends SQLServerDialect {

    @Override
    public Exporter<org.hibernate.mapping.Table> getTableExporter() {
        return new StandardTableExporter(this) {
            @Override
            public String[] getSqlCreateStrings(org.hibernate.mapping.Table table, Metadata metadata, SqlStringGenerationContext context) {
                String[] createStrings = super.getSqlCreateStrings(table, metadata, context);
                createStrings = processVarcharToNvarchar(createStrings);
                return processBooleanDefaults(createStrings);
            }

            private String[] processVarcharToNvarchar(String[] sqlStrings) {
                for (int i = 0; i < sqlStrings.length; i++) {
                    sqlStrings[i] = sqlStrings[i].replaceAll("(?i)(?<!n)varchar", "nvarchar");
                }
                return sqlStrings;
            }

            private String[] processBooleanDefaults(String[] sqlStrings) {
                for (int i = 0; i < sqlStrings.length; i++) {
                    sqlStrings[i] = sqlStrings[i]
                            .replace(" default false", " DEFAULT 0")
                            .replace(" default true", " DEFAULT 1");
                }
                return sqlStrings;
            }
        };
    }

    @Override
    public String toBooleanValueString(boolean bool) {
        return bool ? "1" : "0";
    }
}
