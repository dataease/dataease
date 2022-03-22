package io.dataease.service.engine;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import io.dataease.base.domain.Datasource;
import io.dataease.base.domain.DeEngine;
import io.dataease.base.domain.DeEngineExample;
import io.dataease.base.mapper.DeEngineMapper;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.commons.utils.HttpClientConfig;
import io.dataease.commons.utils.HttpClientUtil;
import io.dataease.controller.ResultHolder;
import io.dataease.controller.request.datasource.DatasourceRequest;
import io.dataease.dto.DatasourceDTO;
import io.dataease.dto.datasource.DorisConfiguration;
import io.dataease.listener.util.CacheUtils;
import io.dataease.provider.ProviderFactory;
import io.dataease.provider.datasource.DatasourceProvider;
import io.dataease.service.datasource.DatasourceService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class EngineService {
    @Resource
    private Environment env;
    @Resource
    private DeEngineMapper deEngineMapper;
    @Resource
    private DatasourceService datasource;

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

    public ResultHolder validate(DatasourceDTO datasource) throws Exception {
        if (StringUtils.isEmpty(datasource.getType()) || StringUtils.isEmpty(datasource.getConfiguration())) {
            throw new Exception("未完整设置数据引擎");
        }
        try {
            DatasourceProvider datasourceProvider = ProviderFactory.getProvider(datasource.getType());
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
            }catch (Exception e){
                return ResultHolder.error("Engine is invalid: " + e.getMessage());
            }

            JSONArray backends =  Optional.ofNullable(JSONObject.parseObject(response).getJSONObject("data")).orElse(new JSONObject()).getJSONArray("backends");
            if(CollectionUtils.isEmpty(backends)){
                return ResultHolder.error("Engine is invalid: no backends found.");
            }

            Integer alives = 0;
            for (int i = 0; i < backends.size(); i++) {
                JSONObject kv = backends.getJSONObject(i);
                if (kv.getBoolean("is_alive")) {
                    alives ++;
                }
            }

            if(alives  < dorisConfiguration.getReplicationNum()){
                return ResultHolder.error("Engine params is invalid: 副本数量不能大于节点数量.");
            }
        }

        return ResultHolder.success(datasource);
    }

    public ResultHolder save(DeEngine engine) throws Exception {
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

    private void setDs(DeEngine engine) {
        Datasource datasource = new Datasource();
        BeanUtils.copyBean(datasource, engine);
        CacheUtils.put("ENGINE", "engine", datasource, null, null);
    }

    public Datasource getDeEngine() throws Exception {
        Object catcheEngine = CacheUtils.get("ENGINE", "engine");
        if (catcheEngine != null) {
            return (Datasource) catcheEngine;
        }

        if (isLocalMode()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("dataSourceType", "jdbc");
            jsonObject.put("dataBase", env.getProperty("doris.db", "doris"));
            jsonObject.put("username", env.getProperty("doris.user", "root"));
            jsonObject.put("password", env.getProperty("doris.password", "dataease"));
            jsonObject.put("host", env.getProperty("doris.host", "doris"));
            jsonObject.put("port", env.getProperty("doris.port", "9030"));
            jsonObject.put("httpPort", env.getProperty("doris.httpPort", "8030"));

            DeEngine engine = new DeEngine();
            engine.setId("doris");
            engine.setName("doris");
            engine.setDesc("doris");
            engine.setType("engine_doris");
            engine.setConfiguration(jsonObject.toJSONString());
            setDs(engine);
        } else {
            List<DeEngine> deEngines = deEngineMapper.selectByExampleWithBLOBs(new DeEngineExample());
            if (CollectionUtils.isEmpty(deEngines)) {
                throw new Exception("未设置数据引擎");
            }
            setDs(deEngines.get(0));
        }
        return getDeEngine();
    }


}
