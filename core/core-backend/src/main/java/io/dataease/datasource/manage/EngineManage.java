package io.dataease.datasource.manage;

import io.dataease.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.dao.auto.entity.CoreDeEngine;
import io.dataease.datasource.dao.auto.repository.CoreDatasourceRepository;
import io.dataease.datasource.dao.auto.repository.CoreDeEngineRepository;
import io.dataease.datasource.type.H2;
import io.dataease.datasource.type.Mysql;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.dto.DatasourceDTO;
import io.dataease.extensions.datasource.dto.DatasourceRequest;
import io.dataease.extensions.datasource.factory.ProviderFactory;
import io.dataease.result.ResultMessage;
import io.dataease.template.dao.auto.entity.DeTemplateVersion;
import io.dataease.template.dao.auto.mapper.DeTemplateVersionRepository;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.IDUtils;
import io.dataease.utils.JsonUtil;
import io.dataease.utils.ModelUtils;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Transactional(rollbackFor = Exception.class)
public class EngineManage {
    @Resource
    private Environment env;
    @Resource
    private CoreDeEngineRepository coreDeEngineRepository;
    @Autowired
    private CoreDatasourceRepository coreDatasourceRepository;

    @Value("${dataease.path.engine:jdbc:h2:/opt/dataease2.0/desktop_data;AUTO_SERVER=TRUE;AUTO_RECONNECT=TRUE;MODE=MySQL;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DATABASE_TO_UPPER=FALSE}")
    private String engineUrl;

    @Resource
    private DeTemplateVersionRepository deTemplateVersionRepository;


    public CoreDeEngine info() throws DEException {
        List<CoreDeEngine> deEngines = coreDeEngineRepository.findAll();
        if (CollectionUtils.isEmpty(deEngines)) {
            DEException.throwException("未完整设置数据引擎");
        }
        return deEngines.get(0);
    }

    public CoreDatasource getDeEngine() {
        List<CoreDeEngine> deEngines = coreDeEngineRepository.findAll();
        if (CollectionUtils.isEmpty(deEngines)) {
            DEException.throwException("未完整设置数据引擎");
        }
        CoreDatasource coreDatasource = new CoreDatasource();
        BeanUtils.copyBean(coreDatasource, deEngines.get(0));
        return coreDatasource;
    }


    public CoreDatasource deEngine() {
        List<CoreDeEngine> deEngines = coreDeEngineRepository.findAll();
        CoreDatasource coreDatasource = new CoreDatasource();
        if (CollectionUtils.isEmpty(deEngines)) {
            return null;
        }
        BeanUtils.copyBean(coreDatasource, deEngines.get(0));
        return coreDatasource;
    }

    public void validate(CoreDeEngine engine) throws Exception {
        if (StringUtils.isEmpty(engine.getType()) || StringUtils.isEmpty(engine.getConfiguration())) {
            throw new Exception("未完整设置数据引擎");
        }
        try {

            DatasourceRequest datasourceRequest = new DatasourceRequest();
            DatasourceDTO datasource = new DatasourceDTO();
            BeanUtils.copyBean(datasource, engine);
            datasourceRequest.setDatasource(datasource);
            ProviderFactory.getProvider(engine.getType()).checkStatus(datasourceRequest);
        } catch (Exception e) {
            DEException.throwException("校验失败：" + e.getMessage());
        }
    }

    public ResultMessage save(CoreDeEngine engine) throws Exception {
        if (engine.getId() == null) {
            engine.setId(IDUtils.snowID());
            coreDeEngineRepository.saveAndFlush(engine);
        } else {
            coreDeEngineRepository.saveAndFlush(engine);
        }
        return ResultMessage.success(engine);
    }

    public void initSimpleEngine() throws Exception {
        initLocalDataSource();
        Specification<CoreDeEngine> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (ModelUtils.isDesktop()) {
                predicates.add(criteriaBuilder.equal(root.get("type"), engineType.h2.name()));
            } else {
                predicates.add(criteriaBuilder.equal(root.get("type"), engineType.mysql.name()));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        List<CoreDeEngine> deEngines = coreDeEngineRepository.findAll(spec);
        if (!CollectionUtils.isEmpty(deEngines)) {
            return;
        }
        CoreDeEngine engine = new CoreDeEngine();
        if (ModelUtils.isDesktop()) {
            engine.setType(engineType.h2.name());
            H2 h2 = new H2();
            h2.setJdbc(engineUrl);
            h2.setDataBase("PUBLIC");
            h2.setUsername(env.getProperty("spring.datasource.username"));
            h2.setPassword(env.getProperty("spring.datasource.password"));
            engine.setConfiguration(JsonUtil.toJSONString(h2).toString());
        } else {
            engine.setType(engineType.mysql.name());
            Mysql mysqlConfiguration = new Mysql();
            Pattern WITH_SQL_FRAGMENT = Pattern.compile("jdbc:mysql://(.*):(\\d+)/(.*)");
            Matcher matcher = WITH_SQL_FRAGMENT.matcher(env.getProperty("spring.datasource.url"));
            if (!matcher.find()) {
                return;
            }
            mysqlConfiguration.setHost(matcher.group(1));
            mysqlConfiguration.setPort(Integer.valueOf(matcher.group(2)));
            String[] databasePrams = matcher.group(3).split("\\?");
            mysqlConfiguration.setDataBase(databasePrams[0]);
            if (databasePrams.length == 2) {
                mysqlConfiguration.setExtraParams(databasePrams[1]);
            }
            mysqlConfiguration.setUsername(env.getProperty("spring.datasource.username"));
            mysqlConfiguration.setPassword(env.getProperty("spring.datasource.password"));
            engine.setConfiguration(JsonUtil.toJSONString(mysqlConfiguration).toString());
        }
        engine.setName("默认引擎");
        engine.setDescription("默认引擎");
        engine.setId(IDUtils.snowID());
        coreDeEngineRepository.saveAndFlush(engine);
    }


    public enum engineType {
        mysql("Mysql"),
        h2("h2");
        private String alias;

        private engineType(String alias) {
            this.alias = alias;
        }

        public String getAlias() {
            return alias;
        }
    }

    public void initLocalDataSource() {
        CoreDatasource coreDatasource = new CoreDatasource();
        coreDatasource.setId(985188400292302848L);
        coreDatasource.setCreateTime(1715053684176L);
        Example<CoreDatasource> example = Example.of(coreDatasource);
        // 版本检查
        Specification<DeTemplateVersion> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("version"), "985188400292302848"));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        // Spring Data JPA 在某些数据库下exists方法会自动生成 fetch first ? rows only，在 Oracle 11g 及以下会报 ORA-00933 错误
        // 使用count方法替代exists方法
        if (!(coreDatasourceRepository.count(example) > 0) && !(deTemplateVersionRepository.count(spec) > 0) && !ModelUtils.isDesktop()) {
            Map<String, String> configuration = parseJdbcUrl();
            if (configuration == null) return;

            CoreDatasource initDatasource = new CoreDatasource();
            initDatasource.setId(985188400292302848L);
            initDatasource.setName("Demo");
            initDatasource.setType(configuration.get("type"));
            initDatasource.setPid(0L);
            initDatasource.setConfiguration((String) JsonUtil.toJSONString(configuration));
            initDatasource.setCreateTime(System.currentTimeMillis());
            initDatasource.setUpdateTime(System.currentTimeMillis());
            initDatasource.setCreateBy("1");
            initDatasource.setUpdateBy(1L);
            initDatasource.setStatus("success");
            initDatasource.setTaskStatus("WaitingForExecution");
            coreDatasourceRepository.deleteById(985188400292302848L);
            coreDatasourceRepository.saveAndFlush(initDatasource);

            DeTemplateVersion version = new DeTemplateVersion();
            version.setVersion("985188400292302848");
            version.setScript("Demo");
            version.setInstalledOn(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
            version.setSuccess(true);
            version.setInstalledRank(IDUtils.snowID());
            deTemplateVersionRepository.saveAndFlush(version);
        }

    }

    /**
     * 定义一个接口，用于解析不同数据库类型的JDBC URL
     * 该接口包含一个parse方法，接受JDBC URL和Spring Environment对象
     * 返回一个Map<String, String>，包含解析后的连接配置
     */
    public interface JdbcUrlParser {
        Map<String, String> parse(String url, Environment env);
    }

    /**
     * MySQL JDBC URL解析器实现
     */
    public static class MysqlJdbcUrlParser implements JdbcUrlParser {
        private static final Pattern PATTERN = Pattern.compile("jdbc:mysql://(.*):(\\d+)/(.*)\\?(.*)");

        @Override
        public Map<String, String> parse(String url, Environment env) {
            Matcher matcher = PATTERN.matcher(url);
            if (!matcher.find()) return null;
            Map<String, String> config = new HashMap<>();
            config.put("host", matcher.group(1));
            config.put("port", matcher.group(2));
            config.put("dataBase", matcher.group(3));
            config.put("type", "mysql");
            config.put("username", env.getProperty("spring.datasource.username"));
            config.put("password", env.getProperty("spring.datasource.password"));
            return config;
        }
    }


    /**
     * Oracle JDBC URL解析器实现
     * 支持两种格式的Oracle JDBC URL：
     * 1. jdbc:oracle:thin:@host:port:serviceName
     * 2. jdbc:oracle:thin:@//host:port/serviceName
     */
    public static class OracleJdbcUrlParser implements JdbcUrlParser {
        private static final Pattern PATTERN1 = Pattern.compile("jdbc:oracle:thin:@(.*):(\\d+)/(.*)\\?(.*)");
        private static final Pattern PATTERN2 = Pattern.compile("jdbc:oracle:thin:@//(.*):(\\d+)/(.*)\\?(.*)");

        @Override
        public Map<String, String> parse(String url, Environment env) {
            Matcher m1 = PATTERN1.matcher(url);
            Matcher m2 = PATTERN2.matcher(url);
            Map<String, String> config = new HashMap<>();
            if (m1.find()) {
                config.put("host", m1.group(1));
                config.put("port", m1.group(2));
                config.put("dataBase", m1.group(3));
            } else if (m2.find()) {
                config.put("host", m2.group(1));
                config.put("port", m2.group(2));
                config.put("dataBase", m2.group(3));
            } else {
                return null;
            }
            config.put("extraParams", "");
            config.put("type", "oracle");
            config.put("username", env.getProperty("spring.datasource.username"));
            config.put("password", env.getProperty("spring.datasource.password"));
            config.put("schema", getCurrentSchema(env.getProperty("spring.datasource.hikari.connection-init-sql")));
            return config;
        }

        /**
         * 提取 Oracle ev 中的spring.datasource.hikari.connection-init-sql
         * 格式为：-Dspring.datasource.hikari.connection-init-sql="ALTER SESSION SET CURRENT_SCHEMA = JIANNENG1"
         * @param arg
         * @return
         */
        public static String getCurrentSchema(String arg) {
            // 匹配等号后最后一个非空白字符串
            Pattern pattern = Pattern.compile("CURRENT_SCHEMA\\s*=\\s*([\\w\\d_]+)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(arg);
            if (matcher.find()) {
                return matcher.group(1).toUpperCase();
            }
            // 如果没有匹配到，返回默认值
            return "USERS";
        }
    }

    /**
     * JDBC URL解析器映射
     */
    private static volatile Map<String, JdbcUrlParser> parserMap;

    /**
     * 按需注册JDBC URL解析器
     *
     * @param env Spring Environment
     * @return 包含不同数据库类型的JDBC URL解析器的Map
     */
    private Map<String, JdbcUrlParser> getParserMap(Environment env) {
        if (parserMap == null) {
            synchronized (EngineManage.class) {
                if (parserMap == null) {
                    parserMap = new HashMap<>();
                    String jdbcUrl = env.getProperty("spring.datasource.url");
                    if (jdbcUrl != null) {
                        if (jdbcUrl.startsWith("jdbc:mysql://")) {
                            parserMap.put("jdbc:mysql://", new MysqlJdbcUrlParser());
                        } else if (jdbcUrl.startsWith("jdbc:oracle:thin:@")) {
                            parserMap.put("jdbc:oracle:thin:@", new OracleJdbcUrlParser());
                        }
                    }
                }
            }
        }
        return parserMap;
    }

    /**
     * 解析JDBC URL
     *
     * @return 解析后的连接配置Map，包含host, port, dataBase, extraParams, type, username, password等信息
     */
    private Map<String, String> parseJdbcUrl() {
        String url = env.getProperty("spring.datasource.url");
        if (StringUtils.isEmpty(url)) {
            return null;
        }
        Map<String, JdbcUrlParser> map = getParserMap(env);
        return map.entrySet().stream()
                .filter(e -> url.startsWith(e.getKey()))
                .map(e -> e.getValue().parse(url, env))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

}
