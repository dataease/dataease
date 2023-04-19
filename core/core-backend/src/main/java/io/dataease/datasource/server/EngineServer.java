package io.dataease.datasource.server;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.datasource.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.dao.auto.entity.CoreDeEngine;
import io.dataease.datasource.dao.auto.mapper.CoreDeEngineMapper;
import io.dataease.datasource.provider.EngineProvider;
import io.dataease.datasource.provider.ProviderUtil;
import io.dataease.datasource.request.DatasourceRequest;
import io.dataease.datasource.type.Mysql;
import io.dataease.result.ResultMessage;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.JsonUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.dataease.result.ResultCode.DATA_IS_WRONG;

@Service
@Transactional(rollbackFor = Exception.class)
public class EngineServer {
    @Resource
    private Environment env;
    @Resource
    private CoreDeEngineMapper deEngineMapper;


    public CoreDeEngine info() throws Exception {
        List<CoreDeEngine> deEngines = deEngineMapper.selectList(null);
        if (CollectionUtils.isEmpty(deEngines)) {
            throw new Exception("未完整设置数据引擎");
        }
        return deEngines.get(0);
    }

    public CoreDatasource getDeEngine() throws Exception {
        List<CoreDeEngine> deEngines = deEngineMapper.selectList(null);
        if (CollectionUtils.isEmpty(deEngines)) {
            throw new Exception("未完整设置数据引擎");
        }
        CoreDatasource coreDatasource = new CoreDatasource();
        BeanUtils.copyBean(coreDatasource, deEngines.get(0));
        return coreDatasource;
    }
    public ResultMessage validate(CoreDeEngine engine) throws Exception {
        if (StringUtils.isEmpty(engine.getType()) || StringUtils.isEmpty(engine.getConfiguration())) {
            throw new Exception("未完整设置数据引擎");
        }
        try {
            EngineProvider provider = ProviderUtil.getEngineProvider(engine.getType());
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            CoreDatasource datasource = new CoreDatasource();
            BeanUtils.copyBean(datasource, engine);
            datasourceRequest.setDatasource(datasource);
            provider.checkStatus(datasourceRequest);
            return ResultMessage.success(datasource);
        } catch (Exception e) {
            return ResultMessage.failure(DATA_IS_WRONG, "Engine is invalid: " + e.getMessage());
        }
    }

    public ResultMessage save(CoreDeEngine engine) throws Exception {
        //TODO
        if (engine.getId() == null) {
            deEngineMapper.insert(engine);
        } else {
            deEngineMapper.updateById(engine);
        }
        return ResultMessage.success(engine);
    }

    public void initSimpleEngine() {
        QueryWrapper<CoreDeEngine> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("type", engineType.mysql.name());
        List<CoreDeEngine> deEngines = deEngineMapper.selectList(queryWrapper);
        if (!CollectionUtils.isEmpty(deEngines)) {
            return;
        }
        CoreDeEngine engine = new CoreDeEngine();
        engine.setType(engineType.mysql.name());
        Mysql mysqlConfiguration = new Mysql();
        Pattern WITH_SQL_FRAGMENT = Pattern.compile("jdbc:mysql://(.*):(\\d+)/(.*)");
        Matcher matcher = WITH_SQL_FRAGMENT.matcher(env.getProperty("spring.datasource.url"));
        if (!matcher.find()) {
            return;
        }
        mysqlConfiguration.setHost(matcher.group(1));
        mysqlConfiguration.setPort(Integer.valueOf(matcher.group(2)));
        mysqlConfiguration.setDataBase(matcher.group(3).split("\\?")[0]);
        mysqlConfiguration.setExtraParams(matcher.group(3).split("\\?")[1]);
        mysqlConfiguration.setUsername(env.getProperty("spring.datasource.username"));
        mysqlConfiguration.setPassword(env.getProperty("spring.datasource.password"));
        engine.setConfiguration(JsonUtil.toJSONString(mysqlConfiguration).toString());
        deEngineMapper.insert(engine);
    }


    public enum engineType {
        mysql("Mysql");
        private String alias;

        private engineType(String alias) {
            this.alias = alias;
        }

        public String getAlias() {
            return alias;
        }
    }
}
