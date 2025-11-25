package io.dataease.extensions.sync.utils;

import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.provider.ExtendedJdbcClassLoader;
import io.dataease.extensions.sync.model.datasource.ConnectionObj;
import io.dataease.extensions.sync.vo.DatasourceConfiguration;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 *
 * @author jianneng
 * @date 2025/11/14 10:34
 **/
public class ProviderUtil {

    /**
     * 获取数据库连接
     *
     * @param configuration           连接参数
     * @param extendedJdbcClassLoader 驱动类
     */
    public static ConnectionObj getConnection(DatasourceConfiguration configuration, ExtendedJdbcClassLoader extendedJdbcClassLoader) {
        ConnectionObj connectionObj = new ConnectionObj();
        Properties props = new Properties();
        if (StringUtils.isNotBlank(configuration.getUsername())) {
            props.setProperty("user", configuration.getUsername());
        }
        if (StringUtils.isNotBlank(configuration.getPassword())) {
            props.setProperty("password", configuration.getPassword());
        }
        String driverClassName = configuration.getDriver();
        Connection conn = null;
        try {
            Driver driverClass = (Driver) extendedJdbcClassLoader.loadClass(driverClassName).getDeclaredConstructor().newInstance();
            conn = driverClass.connect(configuration.getJdbc(), props);

        } catch (Exception e) {
            DEException.throwException(e);
        }
        connectionObj.setConnection(conn);
        return connectionObj;
    }

    /**
     * 获取数据库操作对象
     *
     * @param connection   数据库连接
     * @param queryTimeout 超时时间
     */
    public static Statement getStatement(Connection connection, int queryTimeout) throws Exception {
        if (connection == null) {
            throw new Exception("Connection failed");
        }
        Statement stat = connection.createStatement();
        try {
            stat.setQueryTimeout(queryTimeout);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stat;
    }

    /**
     * 获取预编译数据库操作对象
     *
     * @param connection   数据库连接
     * @param querySql     查询语句
     * @param queryTimeout 超时时间
     */
    public static PreparedStatement getPreparedStatement(Connection connection, String querySql, int queryTimeout) throws Exception {
        if (connection == null) {
            throw new Exception("Connection failed");
        }
        // 处理SQL语句，移除末尾分号
        String query = querySql;
        if (query != null && query.trim().endsWith(";")) {
            query = query.trim().substring(0, query.trim().length() - 1);
        }
        PreparedStatement stat = connection.prepareStatement(query);
        try {
            stat.setQueryTimeout(queryTimeout);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stat;
    }

    /**
     * 计算日期偏移
     *
     * @param dateStr 日期字符串
     * @param offset  偏移量
     * @param unit    偏移单位（millisecond, second, minute, hour, day, month, year）
     * @return 偏移后的日期字符串
     */
    public static String calculateDateOffset(String dateStr, long offset, String unit) {
        String sep = dateStr.contains("/") ? "/" : "-";
        String[] patterns = {
                "yyyy%sMM%sdd HH:mm:ss:SSS",
                "yyyy%sMM%sdd HH:mm:ss",
                "yyyy%sMM%sdd HH:mm",
                "yyyy%sMM%sdd",
                "yyyy%sMM",
                "yyyy",
                "yyyy%sMM%sdd HH:mm:ss.SSSSSS",
                "yyyy%sMM%sdd HH:mm:ss.SSS",
                "yyyy%sMM%sdd HH:mm:ss.SS",
                "yyyy%sMM%sdd HH:mm:ss.S"
        };
        int[] precisions = {5, 4, 3, 2, 1, 0, 5, 5, 5, 5};
        LocalDateTime dateTime = null;
        DateTimeFormatter matchedFormatter = null;
        int matchedPrecision = -1;
        for (int i = 0; i < patterns.length; i++) {
            try {
                String pattern = patterns[i].replace("%s", sep);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                if (i <= 2) {
                    dateTime = LocalDateTime.parse(dateStr, formatter);
                } else if (i == 3) {
                    dateTime = LocalDate.parse(dateStr, formatter).atStartOfDay();
                } else if (i == 4) {
                    dateTime = LocalDate.parse(dateStr + sep + "01", DateTimeFormatter.ofPattern("yyyy" + sep + "MM" + sep + "dd")).atStartOfDay();
                } else if (i == 5) {
                    dateTime = LocalDate.parse(dateStr + sep + "01" + sep + "01", DateTimeFormatter.ofPattern("yyyy" + sep + "MM" + sep + "dd")).atStartOfDay();
                } else {
                    dateTime = LocalDateTime.parse(dateStr, formatter);
                }
                matchedFormatter = formatter;
                matchedPrecision = precisions[i];
                break;
            } catch (Exception ignored) {
            }
        }
        if (dateTime == null) throw new IllegalArgumentException("Date format not supported:" + dateStr);
        int unitPrecision = switch (unit.toLowerCase()) {
            case "year" -> 0;
            case "month" -> 1;
            case "day" -> 2;
            case "hour", "minute" -> 3;
            case "second" -> 4;
            case "millisecond" -> 5;
            default -> throw new IllegalArgumentException("Unsupported unit: " + unit);
        };
        if (matchedPrecision < unitPrecision)
            throw new IllegalArgumentException("[" + dateStr + "] does not support the precision of unit [" + unit + "], date format [" + matchedFormatter + "]");
        dateTime = switch (unit.toLowerCase()) {
            case "year" -> dateTime.plusYears(offset);
            case "month" -> dateTime.plusMonths(offset);
            case "day" -> dateTime.plusDays(offset);
            case "hour" -> dateTime.plusHours(offset);
            case "minute" -> dateTime.plusMinutes(offset);
            case "second" -> dateTime.plusSeconds(offset);
            case "millisecond" -> dateTime.plusNanos(offset * 1000000L);
            default -> dateTime;
        };
        return dateTime.format(matchedFormatter);
    }
}
