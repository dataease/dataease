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

        if (!coreDatasourceRepository.exists(example) && !deTemplateVersionRepository.exists(spec) && !ModelUtils.isDesktop()) {
            Pattern WITH_SQL_FRAGMENT = Pattern.compile("jdbc:mysql://(.*):(\\d+)/(.*)\\?(.*)");
            Matcher matcher = WITH_SQL_FRAGMENT.matcher(env.getProperty("spring.datasource.url"));
            if (!matcher.find()) {
                return;
            }
            Map configuration = new HashMap<>();
            configuration.put("dataBase", matcher.group(3));
            configuration.put("username", env.getProperty("spring.datasource.username"));
            configuration.put("password", env.getProperty("spring.datasource.password"));
            configuration.put("host", matcher.group(1));
            configuration.put("port", Integer.valueOf(matcher.group(2)));
            configuration.put("extraParams", "");

            CoreDatasource initDatasource = new CoreDatasource();
            initDatasource.setId(985188400292302848L);
            initDatasource.setName("Demo");
            initDatasource.setType("mysql");
            initDatasource.setPid(0L);
            initDatasource.setConfiguration(JsonUtil.toJSONString(configuration).toString());
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
}
