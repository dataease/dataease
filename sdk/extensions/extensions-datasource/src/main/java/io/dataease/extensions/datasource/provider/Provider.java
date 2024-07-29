package io.dataease.extensions.datasource.provider;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.constant.SqlPlaceholderConstants;
import io.dataease.extensions.datasource.dto.*;
import io.dataease.extensions.datasource.model.SQLMeta;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import lombok.Getter;
import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.dialect.*;
import org.apache.calcite.sql.parser.SqlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * @Author Junjun
 */
public abstract class Provider {

    public static Logger logger = LoggerFactory.getLogger(Provider.class);

    /**
     * 获取schema接口
     *
     * @param datasourceRequest
     * @return
     */
    public abstract List<String> getSchema(DatasourceRequest datasourceRequest);

    /**
     * 获取表接口
     *
     * @param datasourceRequest
     * @return
     */
    public abstract List<DatasetTableDTO> getTables(DatasourceRequest datasourceRequest);

    /**
     * 创建数据库连接
     *
     * @param coreDatasource
     * @return
     * @throws Exception
     */
    public abstract ConnectionObj getConnection(DatasourceDTO coreDatasource) throws Exception;

    /**
     * 检测数据源状态是否有效
     *
     * @param datasourceRequest
     * @return
     * @throws Exception
     */
    public abstract String checkStatus(DatasourceRequest datasourceRequest) throws Exception;

    /**
     * 获取数据
     *
     * @param datasourceRequest
     * @return
     * @throws DEException
     */
    public abstract Map<String, Object> fetchResultField(DatasourceRequest datasourceRequest) throws DEException;

    /**
     * 获取表字段
     *
     * @param datasourceRequest
     * @return
     * @throws DEException
     */
    public abstract List<TableField> fetchTableField(DatasourceRequest datasourceRequest) throws DEException;

    /**
     * 隐藏密码
     *
     * @param datasourceDTO
     */
    public abstract void hidePW(DatasourceDTO datasourceDTO);

    @Getter
    private static final Map<Long, Integer> lPorts = new HashMap<>();
    @Getter
    private static final Map<Long, Session> sessions = new HashMap<>();

    public Statement getStatement(Connection connection, int queryTimeout) {
        if (connection == null) {
            DEException.throwException("Failed to get connection!");
        }
        Statement stat = null;
        try {
            stat = connection.createStatement();
            stat.setQueryTimeout(queryTimeout);
        } catch (Exception e) {
            DEException.throwException(e.getMessage());
        }
        return stat;
    }

    public String rebuildSQL(String sql, SQLMeta sqlMeta, boolean crossDs, Map<Long, DatasourceSchemaDTO> dsMap) {
        logger.info("calcite sql: " + sql);
        if (crossDs) {
            return sql;
        }

        String s = transSqlDialect(sql, dsMap);
        String tableDialect = sqlMeta.getTableDialect();
        s = replaceTablePlaceHolder(s, tableDialect);
        return replaceCalcFieldPlaceHolder(s, sqlMeta);
    }

    public String transSqlDialect(String sql, Map<Long, DatasourceSchemaDTO> dsMap) throws DEException {
        try {
            DatasourceSchemaDTO value = dsMap.entrySet().iterator().next().getValue();

            SqlParser parser = SqlParser.create(sql, SqlParser.Config.DEFAULT.withLex(Lex.JAVA));
            SqlNode sqlNode = parser.parseStmt();
            return sqlNode.toSqlString(getDialect(value)).toString();
        } catch (Exception e) {
            DEException.throwException(e.getMessage());
        }
        return null;
    }

    public String replaceTablePlaceHolder(String s, String placeholder) {
        s = s.replaceAll("\r\n", " ")
                .replaceAll("\n", " ")
                .replaceAll(SqlPlaceholderConstants.TABLE_PLACEHOLDER_REGEX, Matcher.quoteReplacement(placeholder))
                .replaceAll("ASYMMETRIC", "")
                .replaceAll("SYMMETRIC", "");
        return s;
    }

    public String replaceCalcFieldPlaceHolder(String s, SQLMeta sqlMeta) {
        Map<String, String> fieldsDialect = new HashMap<>();
        if (sqlMeta.getXFieldsDialect() != null && !sqlMeta.getXFieldsDialect().isEmpty()) {
            fieldsDialect.putAll(sqlMeta.getXFieldsDialect());
        }
        if (sqlMeta.getYFieldsDialect() != null && !sqlMeta.getYFieldsDialect().isEmpty()) {
            fieldsDialect.putAll(sqlMeta.getYFieldsDialect());
        }
        if (sqlMeta.getCustomWheresDialect() != null && !sqlMeta.getCustomWheresDialect().isEmpty()) {
            fieldsDialect.putAll(sqlMeta.getCustomWheresDialect());
        }
        if (sqlMeta.getExtWheresDialect() != null && !sqlMeta.getExtWheresDialect().isEmpty()) {
            fieldsDialect.putAll(sqlMeta.getExtWheresDialect());
        }
        if (sqlMeta.getWhereTreesDialect() != null && !sqlMeta.getWhereTreesDialect().isEmpty()) {
            fieldsDialect.putAll(sqlMeta.getWhereTreesDialect());
        }

        if (!fieldsDialect.isEmpty()) {
            for (Map.Entry<String, String> ele : fieldsDialect.entrySet()) {
                s = s.replaceAll(SqlPlaceholderConstants.KEYWORD_PREFIX_REGEX + ele.getKey() + SqlPlaceholderConstants.KEYWORD_SUFFIX_REGEX, Matcher.quoteReplacement(ele.getValue()));
            }
        }
        return s;
    }

    public SqlDialect getDialect(DatasourceSchemaDTO coreDatasource) {
        SqlDialect sqlDialect = null;
        DatasourceConfiguration.DatasourceType datasourceType = DatasourceConfiguration.DatasourceType.valueOf(coreDatasource.getType());
        switch (datasourceType) {
            case mysql:
            case mongo:
            case StarRocks:
            case TiDB:
            case mariadb:
                sqlDialect = MysqlSqlDialect.DEFAULT;
                break;
            case doris:
                sqlDialect = DorisSqlDialect.DEFAULT;
                break;
            case impala:
                sqlDialect = ImpalaSqlDialect.DEFAULT;
                break;
            case sqlServer:
                sqlDialect = MssqlSqlDialect.DEFAULT;
                break;
            case oracle:
                sqlDialect = OracleSqlDialect.DEFAULT;
                break;
            case db2:
                sqlDialect = Db2SqlDialect.DEFAULT;
                break;
            case pg:
                sqlDialect = PostgresqlSqlDialect.DEFAULT;
                break;
            case redshift:
                sqlDialect = RedshiftSqlDialect.DEFAULT;
                break;
            case ck:
                sqlDialect = ClickHouseSqlDialect.DEFAULT;
                break;
            case h2:
                sqlDialect = H2SqlDialect.DEFAULT;
                break;
            default:
                sqlDialect = MysqlSqlDialect.DEFAULT;
        }
        return sqlDialect;
    }

    synchronized public Integer getLport(Long datasourceId) throws Exception {
        for (int i = 10000; i < 20000; i++) {
            if (isPortAvailable(i) && !lPorts.values().contains(i)) {
                if (datasourceId == null) {
                    lPorts.put((long) i, i);
                } else {
                    lPorts.put(datasourceId, i);
                }
                return i;
            }
        }
        throw new Exception("localhost无可用端口！");
    }

    public boolean isPortAvailable(int port) {
        try {
            Socket socket = new Socket("127.0.0.1", port);
            socket.close();
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    public void startSshSession(DatasourceConfiguration configuration, ConnectionObj connectionObj, Long datacourseId) throws Exception {
        if (configuration.isUseSSH()) {
            if (datacourseId == null) {
                configuration.setLPort(getLport(null));
                connectionObj.setLPort(configuration.getLPort());
                connectionObj.setConfiguration(configuration);
                Session session = initSession(configuration);
                connectionObj.setSession(session);
            } else {
                Integer lport = Provider.getLPorts().get(datacourseId);
                configuration.setLPort(lport);
                if (lport != null) {
                    if (Provider.getSessions().get(datacourseId) == null || !Provider.getSessions().get(datacourseId).isConnected()) {
                        Session session = initSession(configuration);
                        Provider.getSessions().put(datacourseId, session);
                    }
                } else {
                    configuration.setLPort(getLport(datacourseId));
                    Session session = initSession(configuration);
                    Provider.getSessions().put(datacourseId, session);
                }
                configuration.setLPort(lport);
            }
        }
    }

    public Session initSession(DatasourceConfiguration configuration) throws Exception {
        JSch jsch = new JSch();
        Session session = jsch.getSession(configuration.getSshUserName(), configuration.getSshHost(), configuration.getSshPort());
        if (!configuration.getSshType().equalsIgnoreCase("password")) {
            session.setConfig("PreferredAuthentications", "publickey");
            jsch.addIdentity("sshkey", configuration.getSshKey().getBytes(StandardCharsets.UTF_8), null, configuration.getSshKeyPassword() == null ? null : configuration.getSshKeyPassword().getBytes(StandardCharsets.UTF_8));
        }
        if (configuration.getSshType().equalsIgnoreCase("password")) {
            session.setPassword(configuration.getSshPassword());
        }
        session.setConfig("StrictHostKeyChecking", "no");
        try {
            session.connect(1000 * 5);
        } catch (Exception e) {
            DEException.throwException("SSH 连接失败：" + e.getMessage());
        }
        session.setPortForwardingL(configuration.getLPort(), configuration.getHost(), configuration.getPort());

        return session;
    }
}
