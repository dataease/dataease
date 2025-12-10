package io.dataease.extensions.sync.provider;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import io.dataease.exception.DEException;
import io.dataease.extensions.sync.model.datasource.ConnectionObj;
import io.dataease.extensions.sync.model.datasource.DatasourceRequest;
import io.dataease.extensions.sync.utils.ProviderUtil;
import io.dataease.extensions.sync.vo.DatasourceConfiguration;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数据源提供者抽象类，定义了与数据源交互的基本操作和SSH会话管理
 *
 * @author jianneng
 */
public abstract class SyncProvider {

    public static Logger logger = LoggerFactory.getLogger(SyncProvider.class);

    @Getter
    private static final Map<Long, Integer> LOCAL_PORTS = new HashMap<>();
    @Getter
    private static final Map<Long, Session> SESSION_MAP = new HashMap<>();

    /**
     * 获取数据源的架构信息
     *
     * @param datasourceRequest 数据源请求
     * @return 架构信息列表
     * @throws DEException 异常
     */
    public abstract List<String> getSchema(DatasourceRequest datasourceRequest) throws DEException;

    /**
     * 获取数据源连接对象
     *
     * @param configuration 数据源配置
     * @return 连接对象
     * @throws DEException 异常
     */
    public abstract ConnectionObj getConnection(DatasourceConfiguration configuration) throws DEException;

    /**
     * 检查数据源状态
     *
     * @param datasourceRequest 数据源请求
     * @return 状态字符串, 成功时返回"Success"字符串
     * @throws DEException 异常
     */
    public abstract String checkStatus(DatasourceRequest datasourceRequest) throws DEException;

    /**
     * 验证数据源配置
     * 在验证过程中，可以尝试连接数据源以确保配置的正确性
     *
     * @param datasourceRequest 数据源请求
     * @return 验证结果字符串, 成功时返回"Success"字符串
     * @throws DEException 异常
     */
    public abstract String validate(DatasourceRequest datasourceRequest) throws DEException;

    /**
     * 检查数据源配置的完整性和有效性
     *
     * @param datasource 数据源请求
     * @throws DEException 异常
     */
    public abstract void checkConfiguration(DatasourceRequest datasource) throws DEException;

    /**
     * 配置额外参数
     * 默认不做任何操作
     * 如: Doris需要配置版本以及be节点等
     *
     * @param datasource 数据源请求
     * @throws DEException 异常
     */
    public void configurationAdditionalParameters(DatasourceRequest datasource) throws DEException {
        // 默认不做任何操作
    }

    /**
     * 执行SQL语句
     *
     * @param configuration 数据源配置
     * @param queryStr      SQL语句
     * @return 执行结果
     */
    public boolean executeSql(DatasourceConfiguration configuration, String queryStr) {
        int queryTimeout = Math.max(configuration.getQueryTimeout(), 30);
        // 对于DDL操作，使用更长的超时时间
        if (isDdlOperation(queryStr)) {
            queryTimeout = Math.max(queryTimeout, 120);
        }
        long startTime = System.currentTimeMillis();
        String operationType = getOperationType(queryStr);
        try (Connection con = getConnection(configuration).getConnection()) {
            con.setNetworkTimeout(Runnable::run, queryTimeout * 1000);
            Statement statement = ProviderUtil.getStatement(con, queryTimeout);
            // 执行SQL
            int result = statement.executeUpdate(queryStr);
            return result >= 0;

        } catch (SQLTimeoutException e) {
            long executeTime = System.currentTimeMillis() - startTime;
            String errorMsg = String.format("%s operation timed out, execution time: %dms, timeout setting: %ds",
                    operationType, executeTime, queryTimeout);
            DEException.throwException(errorMsg);
        } catch (SQLException e) {
            long executeTime = System.currentTimeMillis() - startTime;
            String errorCode = e.getSQLState();
            String errorMsg = String.format("%s operation failed, execution time: %dms, error code: %s, error message: %s",
                    operationType, executeTime, errorCode, e.getMessage());
            DEException.throwException(errorMsg);
        } catch (Exception e) {
            long executeTime = System.currentTimeMillis() - startTime;
            String errorMsg = String.format("%s operation exception, execution time: %dms, exception message: %s",
                    operationType, executeTime, e.getMessage());
            DEException.throwException(errorMsg);
        }
        return false;
    }

    /**
     * 判断是否为DDL操作
     */
    private boolean isDdlOperation(String sql) {
        Pattern ddlPrefix = Pattern.compile("^\\s*(CREATE|ALTER|DROP|TRUNCATE)\\b", Pattern.CASE_INSENSITIVE);
        return sql != null && ddlPrefix.matcher(sql).find();
    }

    /**
     * 获取操作类型
     */
    private String getOperationType(String sql) {
        if (StringUtils.isBlank(sql)) {
            return "UNKNOWN";
        }
        Matcher m = Pattern
                .compile("^\\s*(CREATE|ALTER|DROP|INSERT|UPDATE|DELETE)\\b", Pattern.CASE_INSENSITIVE)
                .matcher(sql);
        return m.find() ? m.group(1).toUpperCase() : "OTHER";
    }

    /**
     * 获取带有查询超时设置的SQL语句对象
     *
     * @param connection   数据库连接对象
     * @param queryTimeout 查询超时时间（秒）
     * @return SQL语句对象
     */
    public Statement getStatement(Connection connection, int queryTimeout) {
        if (connection == null) {
            DEException.throwException("Failed to get connection!");
        }
        Statement stat = null;
        try {
            stat = connection.createStatement();
            stat.setQueryTimeout(queryTimeout);
        } catch (Exception e) {
            logger.error("Failed to get statement!", e);
            DEException.throwException(e.getMessage());
        }
        return stat;
    }

    /**
     * 获取本地可用端口
     *
     * @param datasourceId 数据源ID（可选）
     * @return 可用端口号
     * @throws Exception 异常
     */
    synchronized public Integer getLocalPort(Long datasourceId) throws Exception {
        for (int i = 10000; i < 20000; i++) {
            if (isPortAvailable(i) && !LOCAL_PORTS.containsValue(i)) {
                if (datasourceId == null) {
                    LOCAL_PORTS.put((long) i, i);
                } else {
                    LOCAL_PORTS.put(datasourceId, i);
                }
                return i;
            }
        }
        throw new Exception("localhost无可用端口！");
    }

    /**
     * 检查指定端口是否可用
     *
     * @param port 端口号
     * @return 如果端口可用则返回true，否则返回false
     */
    public boolean isPortAvailable(int port) {
        try {
            Socket socket = new Socket("127.0.0.1", port);
            socket.close();
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    /**
     * 启动SSH会话
     *
     * @param configuration 数据源配置
     * @param connectionObj 连接对象
     * @param datasourceId  数据源ID（可选）
     * @throws Exception 异常
     */
    public void startSshSession(DatasourceConfiguration configuration, ConnectionObj connectionObj, Long datasourceId) throws Exception {
        if (configuration.isUseSSH()) {
            if (datasourceId == null) {
                configuration.setLPort(getLocalPort(null));
                connectionObj.setLPort(configuration.getLPort());
                connectionObj.setConfiguration(configuration);
                Session session = initSession(configuration);
                connectionObj.setSession(session);
            } else {
                Integer lport = SyncProvider.getLOCAL_PORTS().get(datasourceId);
                if (lport != null) {
                    configuration.setLPort(lport);
                    if (SyncProvider.getSESSION_MAP().get(datasourceId) == null || !SyncProvider.getSESSION_MAP().get(datasourceId).isConnected()) {
                        Session session = initSession(configuration);
                        SyncProvider.getSESSION_MAP().put(datasourceId, session);
                    }
                } else {
                    lport = getLocalPort(datasourceId);
                    configuration.setLPort(lport);
                    Session session = initSession(configuration);
                    SyncProvider.getSESSION_MAP().put(datasourceId, session);
                }
            }
        }
    }

    /**
     * 初始化SSH会话
     *
     * @param configuration 数据源配置
     * @return SSH会话对象
     * @throws Exception 异常
     */
    public Session initSession(DatasourceConfiguration configuration) throws Exception {
        JSch jsch = new JSch();
        Session session = jsch.getSession(configuration.getSshUserName(), configuration.getSshHost(), configuration.getSshPort());
        if (!"password".equalsIgnoreCase(configuration.getSshType())) {
            session.setConfig("PreferredAuthentications", "publickey");
            jsch.addIdentity("sshkey", configuration.getSshKey().getBytes(StandardCharsets.UTF_8), null, configuration.getSshKeyPassword() == null ? null : configuration.getSshKeyPassword().getBytes(StandardCharsets.UTF_8));
        }
        if ("password".equalsIgnoreCase(configuration.getSshType())) {
            session.setPassword(configuration.getSshPassword());
        }
        session.setConfig("StrictHostKeyChecking", "no");
        try {
            session.connect(1000 * 5);
        } catch (Exception e) {
            logger.error("Failed to connect to ssh server!", e);
            DEException.throwException("SSH 连接失败：" + e.getMessage());
        }
        session.setPortForwardingL(configuration.getLPort(), configuration.getHost(), configuration.getPort());

        return session;
    }
}
