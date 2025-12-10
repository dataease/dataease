package io.dataease.extensions.sync.datatype;

import lombok.Getter;

import java.util.Arrays;

/**
 * 数据类型标准库枚举
 *
 * @author jianneng
 * @date 2025/11/18 19:09
 **/
public enum StandardDataType {
    // 数值类型
    TINYINT("TINYINT", "8位整数", "8位有符号整数，范围-128~127", Category.NUMBER),
    SMALLINT("SMALLINT", "16位整数", "16位有符号整数", Category.NUMBER),
    INTEGER("INTEGER", "32位整数", "32位有符号整数", Category.NUMBER),
    BIGINT("BIGINT", "64位整数", "64位有符号整数", Category.NUMBER),
    MEDIUMINT("MEDIUMINT", "24位整数", "24位有符号整数，MySQL特有", Category.NUMBER),
    INT("INT", "32位整数", "32位有符号整数", Category.NUMBER),
    TINYINT_UNSIGNED("TINYINT_UNSIGNED", "无符号8位整数", "无符号8位整数", Category.NUMBER),
    SMALLINT_UNSIGNED("SMALLINT_UNSIGNED", "无符号16位整数", "无符号16位整数", Category.NUMBER),
    INTEGER_UNSIGNED("INTEGER_UNSIGNED", "无符号32位整数", "无符号32位整数", Category.NUMBER),
    BIGINT_UNSIGNED("BIGINT_UNSIGNED", "无符号64位整数", "无符号64位整数", Category.NUMBER),
    MEDIUMINT_UNSIGNED("MEDIUMINT_UNSIGNED", "无符号24位整数", "无符号24位整数", Category.NUMBER),
    INT_UNSIGNED("INT_UNSIGNED", "无符号32位整数", "无符号32位整数", Category.NUMBER),
    FLOAT("FLOAT", "单精度浮点数", "单精度浮点数", Category.NUMBER),
    DOUBLE("DOUBLE", "双精度浮点数", "双精度浮点数", Category.NUMBER),
    DECIMAL("DECIMAL", "高精度定点数", "高精度定点数，常用于货币", Category.NUMBER),
    NUMERIC("NUMERIC", "高精度定点数", "高精度定点数", Category.NUMBER),
    REAL("REAL", "近似浮点数", "近似浮点数", Category.NUMBER),
    DOUBLE_PRECISION("DOUBLE_PRECISION", "双精度浮点数", "双精度浮点数", Category.NUMBER),
    DECIMAL_UNSIGNED("DECIMAL_UNSIGNED", "无符号高精度定点数", "无符号高精度定点数", Category.NUMBER),
    NUMERIC_UNSIGNED("NUMERIC_UNSIGNED", "无符号高精度定点数", "无符号高精度定点数", Category.NUMBER),
    DECFLOAT("DECFLOAT", "十进制浮点数", "十进制浮点数", Category.NUMBER),
    NUMBER("NUMBER", "通用数字类型", "通用数字类型（如Oracle）", Category.NUMBER),
    BINARY_FLOAT("BINARY_FLOAT", "二进制浮点数", "二进制浮点数（Oracle）", Category.NUMBER),
    BINARY_DOUBLE("BINARY_DOUBLE", "二进制双精度浮点数", "二进制双精度浮点数（Oracle）", Category.NUMBER),
    BYTE("BYTE", "字节", "单字节整数", Category.NUMBER),
    SHORT("SHORT", "短整型", "短整型", Category.NUMBER),
    LONG_INT("LONG_INT", "长整型", "长整型", Category.NUMBER),
    UNSIGNED_LONG("UNSIGNED_LONG", "无符号长整型", "无符号长整型", Category.NUMBER),
    HALF_FLOAT("HALF_FLOAT", "半精度浮点数", "半精度浮点数", Category.NUMBER),
    SCALED_FLOAT("SCALED_FLOAT", "可缩放浮点数", "可缩放浮点数（如Elasticsearch）", Category.NUMBER),
    INT_IDENTITY("INT_IDENTITY", "自增32位整数", "自增主键类型", Category.NUMBER),
    BIGINT_IDENTITY("BIGINT_IDENTITY", "自增64位整数", "自增主键类型", Category.NUMBER),
    SMALLINT_IDENTITY("SMALLINT_IDENTITY", "自增16位整数", "自增主键类型", Category.NUMBER),
    TINYINT_IDENTITY("TINYINT_IDENTITY", "自增8位整数", "自增主键类型", Category.NUMBER),
    DECIMAL_IDENTITY("DECIMAL_IDENTITY", "自增高精度定点数", "自增主键类型", Category.NUMBER),
    NUMERIC_IDENTITY("NUMERIC_IDENTITY", "自增高精度定点数", "自增主键类型", Category.NUMBER),
    INTEGER_IDENTITY("INTEGER_IDENTITY", "自增32位整数", "自增主键类型", Category.NUMBER),

    // 字符类型
    CHAR("CHAR", "定长字符串", "定长字符串", Category.STRING),
    VARCHAR("VARCHAR", "变长字符串", "变长字符串", Category.STRING),
    STRING("STRING", "字符串", "通用字符串类型", Category.STRING),
    TEXT("TEXT", "大文本", "大容量文本数据", Category.STRING),
    CLOB("CLOB", "字符大对象", "字符大对象", Category.STRING),
    TINYTEXT("TINYTEXT", "小文本", "小容量文本数据", Category.STRING),
    MEDIUMTEXT("MEDIUMTEXT", "中等文本", "中等容量文本数据", Category.STRING),
    LONGTEXT("LONGTEXT", "超大文本", "超大容量文本数据", Category.STRING),
    TINYSTRING("TINYSTRING", "小字符串", "小容量字符串", Category.STRING),
    MEDIUMSTRING("MEDIUMSTRING", "中等字符串", "中等容量字符串", Category.STRING),
    LONGSTRING("LONGSTRING", "超大字符串", "超大容量字符串", Category.STRING),
    CHARACTER("CHARACTER", "字符", "单字符或定长字符串", Category.STRING),
    VARCHAR2("VARCHAR2", "变长字符串", "Oracle变长字符串", Category.STRING),
    NCHAR("NCHAR", "定长Unicode字符串", "定长Unicode字符串", Category.STRING),
    LONG("LONG", "长字符串", "Oracle长字符串", Category.STRING),
    NCLOB("NCLOB", "Unicode字符大对象", "Unicode字符大对象", Category.STRING),
    NVARCHAR2("NVARCHAR2", "变长Unicode字符串", "Oracle变长Unicode字符串", Category.STRING),
    NVARCHAR("NVARCHAR", "变长Unicode字符串", "变长Unicode字符串", Category.STRING),
    NTEXT("NTEXT", "Unicode大文本", "Unicode大文本", Category.STRING),

    // 二进制类型
    BINARY("BINARY", "定长二进制", "定长二进制数据", Category.BINARY),
    VARBINARY("VARBINARY", "变长二进制", "变长二进制数据", Category.BINARY),
    BLOB("BLOB", "二进制大对象", "二进制大对象", Category.BINARY),
    BYTEA("BYTEA", "二进制数组", "PostgreSQL二进制数组", Category.BINARY),
    BIT("BIT", "位类型", "位类型", Category.BINARY),
    TINYBINARY("TINYBINARY", "小二进制", "小容量二进制", Category.BINARY),
    LONGBINARY("LONGBINARY", "超大二进制", "超大容量二进制", Category.BINARY),
    TINYBLOB("TINYBLOB", "小二进制大对象", "小容量二进制大对象", Category.BINARY),
    MEDIUMBLOB("MEDIUMBLOB", "中等二进制大对象", "中等容量二进制大对象", Category.BINARY),
    LONGBLOB("LONGBLOB", "超大二进制大对象", "超大容量二进制大对象", Category.BINARY),
    LONGVARCHAR("LONGVARCHAR", "长变长字符串", "长变长字符串", Category.BINARY),
    RAW("RAW", "原始二进制", "Oracle原始二进制", Category.BINARY),
    LONG_RAW("LONG_RAW", "长原始二进制", "Oracle长原始二进制", Category.BINARY),

    // 日期时间类型
    DATE("DATE", "日期", "仅包含年月日的日期类型", Category.DATE),
    TIME("TIME", "时间", "仅包含时分秒的时间类型", Category.DATE),
    DATETIME("DATETIME", "日期时间", "包含日期和时间", Category.DATE),
    TIMESTAMP("TIMESTAMP", "时间戳", "时间戳，通常精确到秒或毫秒", Category.DATE),
    TIMESTAMPTZ("TIMESTAMPTZ", "带时区时间戳", "带时区的时间戳", Category.DATE),
    YEAR("YEAR", "年份", "年份类型", Category.DATE),
    DATETIME2("DATETIME2", "高精度日期时间", "高精度日期时间（SQL Server）", Category.DATE),
    SMALLDATETIME("SMALLDATETIME", "低精度日期时间", "低精度日期时间（SQL Server）", Category.DATE),
    DATETIMEOFFSET("DATETIMEOFFSET", "带时区偏移的日期时间", "带时区偏移的日期时间（SQL Server）", Category.DATE),

    // 布尔类型
    BOOLEAN("BOOLEAN", "布尔", "布尔类型，真或假", Category.BOOLEAN),

    // 枚举类型
    ENUM("ENUM", "枚举", "枚举类型，有限集合", Category.ENUM),

    // JSON/XML
    JSON("JSON", "JSON对象", "JSON格式数据", Category.OBJECT),
    JSONB("JSONB", "二进制JSON", "二进制JSON格式数据（PostgreSQL）", Category.OBJECT),
    XML("XML", "XML对象", "XML格式数据", Category.OBJECT),

    // 地理空间类型
    GEOMETRY("GEOMETRY", "地理空间", "通用地理空间类型", Category.GEO),
    POINT("POINT", "点", "地理空间点", Category.GEO),
    LINESTRING("LINESTRING", "线", "地理空间线", Category.GEO),
    POLYGON("POLYGON", "多边形", "地理空间多边形", Category.GEO),
    GEO_POINT("GEO_POINT", "地理点", "地理点（如Elasticsearch）", Category.GEO),
    GEO_SHAPE("GEO_SHAPE", "地理形状", "地理形状（如Elasticsearch）", Category.GEO),

    // 网络地址类型
    INET("INET", "IP地址", "IPv4或IPv6地址", Category.NETWORK),
    CIDR("CIDR", "网络地址块", "网络地址块", Category.NETWORK),
    MACADDR("MACADDR", "MAC地址", "物理网卡地址", Category.NETWORK),
    IP("IP", "IP地址", "IP地址", Category.NETWORK),

    // UUID
    UUID("UUID", "唯一标识符", "通用唯一标识符", Category.UUID),

    // 金额
    MONEY("MONEY", "货币金额", "货币金额类型", Category.MONEY),
    SMALLMONEY("SMALLMONEY", "小额货币", "小额货币类型", Category.MONEY),

    // 其他
    INTERVAL("INTERVAL", "时间间隔", "时间间隔类型", Category.OTHER),
    SET("SET", "集合", "集合类型", Category.OTHER),
    ARRAY("ARRAY", "数组", "数组类型", Category.OTHER),
    OBJECT("OBJECT", "对象", "对象类型", Category.OTHER),
    ROWID("ROWID", "行唯一标识", "行唯一标识（Oracle）", Category.OTHER),
    UROWID("UROWID", "通用行唯一标识", "通用行唯一标识（Oracle）", Category.OTHER),
    KEYWORD("KEYWORD", "关键字", "关键字（如Elasticsearch）", Category.OTHER),
    NESTED("NESTED", "嵌套对象", "嵌套对象（如Elasticsearch）", Category.OTHER),

    // 未知类型
    UNKNOWN("UNKNOWN", "未知", "未知类型", Category.UNKNOWN);

    @Getter
    private final String baseName;
    @Getter
    private final String displayName;
    @Getter
    private final String description;
    @Getter
    private final Category category;

    StandardDataType(String baseName, String displayName, String description, Category category) {
        this.baseName = baseName;
        this.displayName = displayName;
        this.description = description;
        this.category = category;
    }

    public static StandardDataType customValueOf(String type) {
        return Arrays.stream(StandardDataType.values())
                .filter(item -> org.apache.commons.lang3.StringUtils.equalsIgnoreCase(item.name(), type.replace(" ", "_")))
                .findFirst()
                .orElse(StandardDataType.UNKNOWN);
    }

    public enum Category {
        NUMBER("数值型"), STRING("字符串型"), BINARY("二进制型"), DATE("日期时间型"),
        BOOLEAN("布尔型"), ENUM("枚举型"), OBJECT("对象/结构体"), GEO("地理空间型"),
        NETWORK("网络地址型"), UUID("唯一标识符"), MONEY("金额型"),
        OTHER("其他"), UNKNOWN("未知");
        @Getter
        private final String description;

        Category(String description) {
            this.description = description;
        }
    }
}

