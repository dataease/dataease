package io.dataease.config;

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.zip.CRC32;


public class DynamicCaseNamingStrategy extends CamelCaseToUnderscoresNamingStrategy {
    private final ThreadLocal<String> currentTableName = new ThreadLocal<>();
    /**
     * Oracle数据库的最大标识符长度
     */
    private static final int ORACLE_MAX_IDENTIFIER_LENGTH = 30;
    /**
     * Oracle数据库的关键字列表
     * 参考:
     * <a href="https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/Oracle-SQL-Reserved-Words.html">...</a>
     */
    private static final Set<String> ORACLE_KEYWORDS = Set.of(
            "ACCESS", "ADD", "ALL", "ALTER", "AND", "ANY", "AS", "ASC", "AUDIT",
            "BETWEEN", "BY", "CHAR", "CHECK", "CLUSTER", "COLUMN", "COMMENT",
            "COMPRESS", "CONNECT", "CREATE", "CURRENT", "DATE", "DECIMAL", "DEFAULT",
            "DELETE", "DESC", "DISTINCT", "DROP", "ELSE", "EXCLUSIVE", "EXISTS",
            "FILE", "FLOAT", "FOR", "FROM", "GRANT", "GROUP", "HAVING", "IDENTIFIED",
            "IMMEDIATE", "IN", "INCREMENT", "INDEX", "INITIAL", "INSERT", "INTEGER",
            "INTERSECT", "INTO", "IS", "LEVEL", "LIKE", "LOCK", "LONG", "MAXEXTENTS",
            "MINUS", "MLSLABEL", "MODE", "MODIFY", "NOAUDIT", "NOCOMPRESS", "NOT",
            "NOWAIT", "NULL", "NUMBER", "OF", "OFFLINE", "ON", "ONLINE", "OPTION",
            "OR", "ORDER", "PCTFREE", "PRIOR", "PRIVILEGES", "PUBLIC", "RAW",
            "RENAME", "RESOURCE", "REVOKE", "ROW", "ROWID", "ROWNUM", "ROWS",
            "SELECT", "SESSION", "SET", "SHARE", "SIZE", "SMALLINT", "START",
            "SUCCESSFUL", "SYNONYM", "SYSDATE", "TABLE", "THEN", "TO", "TRIGGER",
            "UID", "UNION", "UNIQUE", "UPDATE", "USER", "VALIDATE", "VALUES",
            "VARCHAR", "VARCHAR2", "VIEW", "WHENEVER", "WHERE", "WITH"
    );

    private static boolean isOracleKeyword(String word) {
        if (word == null) return false;
        return ORACLE_KEYWORDS.contains(word.toUpperCase());
    }

    /**
     * Oracle数据库标识符长度超过30位时，截断前面部分并添加哈希值
     */
    private Identifier truncateWithHashFromStart(Identifier name) {
        if (name == null) return null;
        String text = name.getText();
        if (text.length() > ORACLE_MAX_IDENTIFIER_LENGTH) {
            CRC32 crc = new CRC32();
            crc.update(text.getBytes(StandardCharsets.UTF_8));
            String hash = Long.toHexString(crc.getValue());
            // hash 取前5位，剩下25位保留结尾部分
            String suffix = text.substring(text.length() - (ORACLE_MAX_IDENTIFIER_LENGTH - 6));
            text = hash.substring(0, 5) + "_" + suffix;
        }
        return Identifier.toIdentifier(text, name.isQuoted());
    }

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        currentTableName.set(name.getText());
        Identifier identifier = super.toPhysicalTableName(name, context);
        if (name.getText().startsWith("QRTZ_")) {
            identifier = Identifier.toIdentifier(name.getText().toUpperCase());
        }
        // 通过context获取数据库类型,判断是否是 Oracle
        String dialectClassName = context.getDialect().getClass().getName();
        if (dialectClassName.contains("Oracle")) {
            identifier = truncateWithHashFromStart(identifier);
            identifier =  addQuotesIfOracleKeyword(identifier);
        }
        return identifier;

    }

    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
        Identifier identifier = super.toPhysicalColumnName(name, context);
        if (currentTableName.get().startsWith("QRTZ_")) {
            identifier = Identifier.toIdentifier(name.getText().toUpperCase());
        }
        // 通过context获取数据库类型,判断是否是 Oracle
        String dialectClassName = context.getDialect().getClass().getName();
        if (dialectClassName.contains("Oracle")) {
            identifier = addQuotesIfOracleKeyword(identifier);
        }
        return identifier;

    }

    /**
     * 如果是Oracle关键字，添加双引号
     */
    private Identifier addQuotesIfOracleKeyword(Identifier name) {
        if (isOracleKeyword(name.getText())) {
            return Identifier.toIdentifier(String.format("\"%s\"", name.getText()));
        }
        return name;
    }
}
