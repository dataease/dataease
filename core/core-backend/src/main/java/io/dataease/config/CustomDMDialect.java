package io.dataease.config;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.dialect.DmDialect;
import org.hibernate.query.sqm.CastType;

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
    public int getMaxVarcharLength() {
        // 达梦 VARCHAR 最大长度限制，避免 Hibernate 生成 varchar(2147483647) 导致 Precision is out of range
        return 8188;
    }

    @Override
    public String castPattern(CastType from, CastType to) {
        // 达梦的 DmDialect 将 boolean 转数字生成为 decode(?1,false,0,true,1,null)，
        // 达梦数据库中 false/true 与数值列比较会报 Data type mismatch，改为 0/1。
        if (from == CastType.BOOLEAN && to.isNumeric()) {
            return "decode(?1,0,0,1,1,null)";
        }
        return super.castPattern(from, to);
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
