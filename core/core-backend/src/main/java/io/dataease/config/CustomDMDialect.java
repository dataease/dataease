package io.dataease.config;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.dialect.DmDialect;

import org.hibernate.tool.schema.internal.StandardTableExporter;
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
}
