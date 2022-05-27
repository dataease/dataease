package io.dataease.service.engine;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.commons.utils.HttpClientConfig;
import io.dataease.commons.utils.HttpClientUtil;
import io.dataease.controller.ResultHolder;
import io.dataease.dto.datasource.DorisConfiguration;
import io.dataease.dto.datasource.MysqlConfiguration;
import io.dataease.listener.util.CacheUtils;
import io.dataease.plugins.common.base.domain.Datasource;
import io.dataease.plugins.common.base.domain.DeEngine;
import io.dataease.plugins.common.base.domain.DeEngineExample;
import io.dataease.plugins.common.base.mapper.DeEngineMapper;
import io.dataease.plugins.common.request.datasource.DatasourceRequest;
import io.dataease.plugins.datasource.provider.Provider;
import io.dataease.provider.ProviderFactory;
import io.dataease.service.datasource.DatasourceService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional(rollbackFor = Exception.class)
public class EngineService {
    @Resource
    private Environment env;
    @Resource
    private DeEngineMapper deEngineMapper;
    @Resource
    private DatasourceService datasource;

    static private List<String> simple_engine = Arrays.asList("engine_mysql");

    static private List<String> cluster_engine = Arrays.asList("engine_doris");

    public Boolean isLocalMode() {
        return env.getProperty("engine_mode", "local").equalsIgnoreCase("local");
    }

    public Boolean isSimpleMode() {
        return env.getProperty("engine_mode", "local").equalsIgnoreCase("simple");
    }

    public Boolean isClusterMode() {
        return env.getProperty("engine_mode", "local").equalsIgnoreCase("cluster");
    }

    public String mode() {
        return env.getProperty("engine_mode", "local");
    }

    public DeEngine info() {
        DeEngineExample deEngineExample = new DeEngineExample();
        if (isClusterMode()) {
            deEngineExample.createCriteria().andTypeEqualTo("engine_doris");
        } else {
            deEngineExample.createCriteria().andTypeEqualTo("engine_mysql");
        }
        List<DeEngine> deEngines = deEngineMapper.selectByExampleWithBLOBs(deEngineExample);
        if (CollectionUtils.isEmpty(deEngines)) {
            return new DeEngine();
        }
        return deEngines.get(0);
    }

    public ResultHolder validate(Datasource datasource) throws Exception {
        if (StringUtils.isEmpty(datasource.getType()) || StringUtils.isEmpty(datasource.getConfiguration())) {
            throw new Exception("未完整设置数据引擎");
        }
        try {
            Provider datasourceProvider = ProviderFactory.getProvider(datasource.getType());
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(datasource);
            datasourceProvider.checkStatus(datasourceRequest);
        } catch (Exception e) {
            return ResultHolder.error("Engine is invalid: " + e.getMessage());
        }

        if (datasource.getType().equalsIgnoreCase("engine_doris")) {
            DorisConfiguration dorisConfiguration = new Gson().fromJson(datasource.getConfiguration(), DorisConfiguration.class);
            HttpClientConfig httpClientConfig = new HttpClientConfig();
            String authValue = "Basic " + Base64.getUrlEncoder().encodeToString((dorisConfiguration.getUsername()
                    + ":" + dorisConfiguration.getPassword()).getBytes());
            httpClientConfig.addHeader("Authorization", authValue);
            String response;
            try {
                response = HttpClientUtil.get("http://" + dorisConfiguration.getHost() + ":" + dorisConfiguration.getHttpPort() + "/api/backends", httpClientConfig);
            } catch (Exception e) {
                return ResultHolder.error("Engine is invalid: " + e.getMessage());
            }

            JsonArray backends = null;
            JsonObject data = JsonParser.parseString(response).getAsJsonObject().getAsJsonObject("data");
            if (data != null) {
                backends = data.getAsJsonArray("backends");
            }
            if (backends == null || backends.size() == 0) {
                return ResultHolder.error("Engine is invalid: no backends found.");
            }

            Integer alives = 0;
            for (int i = 0; i < backends.size(); i++) {
                JsonObject kv = backends.get(i).getAsJsonObject();
                if (kv.get("is_alive").getAsBoolean()) {
                    alives++;
                }
            }

            if (alives < dorisConfiguration.getReplicationNum()) {
                return ResultHolder.error("Engine params is invalid: 副本数量不能大于节点数量.");
            }
        }

        return ResultHolder.success(datasource);
    }

    public ResultHolder save(DeEngine engine) throws Exception {
        checkValid(engine);
        if (StringUtils.isEmpty(engine.getId())) {
            engine.setId(UUID.randomUUID().toString());
            deEngineMapper.insert(engine);
        } else {
            deEngineMapper.updateByPrimaryKeyWithBLOBs(engine);
            datasource.handleConnectionPool(getDeEngine(), "delete");
        }
        setDs(engine);
        datasource.handleConnectionPool(getDeEngine(), "add");
        return ResultHolder.success(engine);
    }

    private void checkValid(DeEngine engine) throws Exception {
        if (isLocalMode()) {
            throw new Exception("Setting engine is not supported.");
        }
        if (isSimpleMode()) {
            if (!simple_engine.contains(engine.getType())) {
                throw new Exception("Engine type not supported.");
            }
        }
        if (isClusterMode()) {
            if (!cluster_engine.contains(engine.getType())) {
                throw new Exception("Engine type not supported.");
            }
        }
    }

    private void setDs(DeEngine engine) {
        CacheUtils.removeAll("ENGINE");
    }

    @Cacheable(value = "ENGINE")
    public Datasource getDeEngine() throws Exception {
        Datasource datasource = new Datasource();

        if (isLocalMode()) {
            Map jsonObjectMap = new HashMap();
            jsonObjectMap.put("dataSourceType", "jdbc");
            jsonObjectMap.put("dataBase", env.getProperty("doris.db", "doris"));
            jsonObjectMap.put("username", env.getProperty("doris.user", "root"));
            jsonObjectMap.put("password", env.getProperty("doris.password", "dataease"));
            jsonObjectMap.put("host", env.getProperty("doris.host", "doris"));
            jsonObjectMap.put("port", env.getProperty("doris.port", "9030"));
            jsonObjectMap.put("httpPort", env.getProperty("doris.httpPort", "8030"));

            DeEngine engine = new DeEngine();
            engine.setId("doris");
            engine.setName("doris");
            engine.setDesc("doris");
            engine.setType("engine_doris");
            engine.setConfiguration(new Gson().toJson(jsonObjectMap));
            BeanUtils.copyBean(datasource, engine);
        }
        if (isClusterMode()) {
            DeEngineExample engineExample = new DeEngineExample();
            engineExample.createCriteria().andTypeEqualTo("engine_doris");
            List<DeEngine> deEngines = deEngineMapper.selectByExampleWithBLOBs(engineExample);
            if (CollectionUtils.isEmpty(deEngines)) {
                throw new Exception("未设置数据引擎");
            }
            BeanUtils.copyBean(datasource, deEngines.get(0));
        }
        if (isSimpleMode()) {
            DeEngineExample engineExample = new DeEngineExample();
            engineExample.createCriteria().andTypeEqualTo("engine_mysql");
            List<DeEngine> deEngines = deEngineMapper.selectByExampleWithBLOBs(engineExample);
            if (CollectionUtils.isEmpty(deEngines)) {
                throw new Exception("未设置数据引擎");
            }
            BeanUtils.copyBean(datasource, deEngines.get(0));
        }
        return datasource;
    }

    public void initSimpleEngine() {
        if (!isSimpleMode()) {
            return;
        }
        DeEngineExample engineExample = new DeEngineExample();
        engineExample.createCriteria().andTypeEqualTo("engine_mysql");
        List<DeEngine> deEngines = deEngineMapper.selectByExampleWithBLOBs(engineExample);
        if (CollectionUtils.isNotEmpty(deEngines)) {
            return;
        }
        DeEngine engine = new DeEngine();
        engine.setId(UUID.randomUUID().toString());
        engine.setType("engine_mysql");
        MysqlConfiguration mysqlConfiguration = new MysqlConfiguration();
        Pattern WITH_SQL_FRAGMENT = Pattern.compile("jdbc:mysql://(.*):(\\d+)/(.*)");
        Matcher matcher = WITH_SQL_FRAGMENT.matcher(env.getProperty("spring.datasource.url"));
        if (!matcher.find()) {
            return;
        }
        ;
        mysqlConfiguration.setHost(matcher.group(1));
        mysqlConfiguration.setPort(Integer.valueOf(matcher.group(2)));
        mysqlConfiguration.setDataBase(matcher.group(3).split("\\?")[0]);
        mysqlConfiguration.setExtraParams(matcher.group(3).split("\\?")[1]);
        mysqlConfiguration.setUsername(env.getProperty("spring.datasource.username"));
        mysqlConfiguration.setPassword(env.getProperty("spring.datasource.password"));
        engine.setConfiguration(new Gson().toJson(mysqlConfiguration));
        deEngineMapper.insert(engine);
    }


}
