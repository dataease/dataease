package io.dataease.service.engine;

import com.alibaba.fastjson.JSONObject;
import io.dataease.base.domain.Datasource;
import io.dataease.base.domain.DeEngine;
import io.dataease.base.domain.DeEngineExample;
import io.dataease.base.mapper.DeEngineMapper;
import io.dataease.commons.utils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class EngineService {
    @Resource
    private Environment env;
    @Resource
    private DeEngineMapper deEngineMapper;
    static private Datasource ds = null;


    public Boolean isLocalMode(){
        return env.getProperty("engine_mode", "local").equalsIgnoreCase("local");
    }

    public Boolean isSimpleMode(){
        return env.getProperty("engine_mode", "local").equalsIgnoreCase("simple");
    }
     public Datasource getDeEngine() throws Exception{
        if (this.ds != null) {
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
            List<DeEngine> deEngines = deEngineMapper.selectByExample(new DeEngineExample());
            if(CollectionUtils.isEmpty(deEngines)){
                throw new Exception("未设置数据引擎");
            }
            BeanUtils.copyBean(this.ds, deEngines.get(0));
        }

        //TODO cluster mode
        return this.ds;
    }


}
