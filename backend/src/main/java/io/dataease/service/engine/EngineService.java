package io.dataease.service.engine;

import com.alibaba.fastjson.JSONObject;
import io.dataease.base.domain.Datasource;
import io.dataease.base.domain.DeEngine;
import io.dataease.base.domain.DeEngineExample;
import io.dataease.base.mapper.DeEngineMapper;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.controller.ResultHolder;
import io.dataease.controller.request.datasource.DatasourceRequest;
import io.dataease.dto.DatasourceDTO;
import io.dataease.provider.ProviderFactory;
import io.dataease.provider.datasource.DatasourceProvider;
import io.dataease.service.datasource.DatasourceService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
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
    static private Datasource ds = new Datasource();


    public Boolean isLocalMode(){
        return env.getProperty("engine_mode", "local").equalsIgnoreCase("local");
    }

    public Boolean isSimpleMode(){
        return env.getProperty("engine_mode", "local").equalsIgnoreCase("simple");
    }

    public Boolean isClusterMode(){
        return env.getProperty("engine_mode", "local").equalsIgnoreCase("cluster");
    }

    public String mode(){
        return env.getProperty("engine_mode", "local");
    }

    public DeEngine info(){
        List<DeEngine> deEngines = deEngineMapper.selectByExampleWithBLOBs(new DeEngineExample());
        if(CollectionUtils.isEmpty(deEngines)){
            return new DeEngine();
        }
        return deEngines.get(0);
    }

    public ResultHolder validate(DatasourceDTO datasource) throws Exception {
        try {
            DatasourceProvider datasourceProvider = ProviderFactory.getProvider(datasource.getType());
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(datasource);
            datasourceProvider.checkStatus(datasourceRequest);
            return ResultHolder.success(datasource);
        }catch (Exception e){
            return ResultHolder.error("Datasource is invalid: " + e.getMessage());
        }
    }

    public ResultHolder save(DeEngine engine) throws Exception {
        if(StringUtils.isEmpty(engine.getId())){
            engine.setId(UUID.randomUUID().toString());
            deEngineMapper.insert(engine);
        }else {
            deEngineMapper.updateByPrimaryKeyWithBLOBs(engine);
        }
        datasource.handleConnectionPool(this.ds, "delete");
        BeanUtils.copyBean(this.ds, engine);
        datasource.handleConnectionPool(this.ds, "add");
        return ResultHolder.success(engine);
    }

    public Datasource getDeEngine() throws Exception{
        if (this.ds != null || StringUtils.isNotEmpty(ds.getType())) {
            return this.ds;
        }
        if(isLocalMode()){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("dataSourceType", "jdbc");
            jsonObject.put("dataBase", env.getProperty("doris.db", "doris"));
            jsonObject.put("username", env.getProperty("doris.user", "root"));
            jsonObject.put("password", env.getProperty("doris.password", "dataease"));
            jsonObject.put("host", env.getProperty("doris.host", "doris"));
            jsonObject.put("port", env.getProperty("doris.port", "9030"));
            jsonObject.put("httpPort", env.getProperty("doris.httpPort", "8030"));

            Datasource datasource = new Datasource();
            datasource.setId("doris");
            datasource.setName("doris");
            datasource.setDesc("doris");
            datasource.setType("engine_doris");
            datasource.setConfiguration(jsonObject.toJSONString());
            this.ds = datasource;
        }
        if(isSimpleMode()){
            List<DeEngine> deEngines = deEngineMapper.selectByExampleWithBLOBs(new DeEngineExample());
            if(CollectionUtils.isEmpty(deEngines)){
                throw new Exception("未设置数据引擎");
            }
            BeanUtils.copyBean(this.ds, deEngines.get(0));
        }

        //TODO cluster mode
        return this.ds;
    }


}
