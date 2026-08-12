package io.dataease.config;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.dialect.OracleDialect;
import org.hibernate.dialect.identity.IdentityColumnSupport;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.mapping.Table;
import org.hibernate.dialect.OracleSqlAstTranslator;
import org.hibernate.query.sqm.FetchClauseType;
import org.hibernate.sql.ast.SqlAstTranslator;
import org.hibernate.sql.ast.SqlAstTranslatorFactory;
import org.hibernate.sql.ast.spi.StandardSqlAstTranslatorFactory;
import org.hibernate.sql.ast.tree.Statement;
import org.hibernate.sql.exec.spi.JdbcOperation;
import org.hibernate.tool.schema.extract.spi.TableInformation;
import org.hibernate.tool.schema.internal.StandardTableExporter;
import org.hibernate.tool.schema.internal.StandardTableMigrator;
import org.hibernate.tool.schema.internal.TableMigrator;
import org.hibernate.tool.schema.spi.Exporter;

/**
 * Oracle 11g 以下 版本的方言实现，主要是分页不支持offset和fetch语法
 *
 * @author jianneng
 * @date 2025/7/4 16:40
 **/
public class DataEaseOracle11gDialect extends OracleDialect {

    @Override
    public Exporter<Table> getTableExporter() {
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

    @Override
    public String toBooleanValueString(boolean bool) {
        return bool ? "1" : "0";
    }

    @Override
    public IdentityColumnSupport getIdentityColumnSupport() {
        return DataEaseOracle12cIdentityColumnSupport.INSTANCE;
    }

    /**
     * Oracle number 不允许带精度
     * 需要精度请在字段上使用 @Column(precision = 10, scale = 2)
     */
    @Override
    public String columnType(int sqlTypeCode) {
        if (sqlTypeCode == -5) {
            return "number";
        }
        return super.columnType(sqlTypeCode);
    }

    @Override
    public boolean supportsFetchClause(FetchClauseType type) {
        return false;
    }

    @Override
    public SqlAstTranslatorFactory getSqlAstTranslatorFactory() {
        return new StandardSqlAstTranslatorFactory() {
            @Override
            public <T extends JdbcOperation> SqlAstTranslator<T> buildTranslator(
                    SessionFactoryImplementor sessionFactory, Statement statement) {
                return new Oracle11gSqlAstTranslator<>(sessionFactory, statement);
            }
        };
    }

    public static class Oracle11gSqlAstTranslator<T extends JdbcOperation>
            extends OracleSqlAstTranslator<T> {

        public Oracle11gSqlAstTranslator(
                SessionFactoryImplementor sessionFactory,
                Statement statement) {
            super(sessionFactory, statement);
        }
    }
}
