package io.dataease.datasource.manage;

import io.dataease.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.dao.auto.entity.CoreDeEngine;
import io.dataease.datasource.dao.auto.repository.CoreDeEngineRepository;
import io.dataease.exception.DEException;
import io.dataease.utils.BeanUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 只读引擎配置服务：提供数据引擎元数据与配置读取能力。
 * 不依赖任何 *Server / Controller / 上层编排器。
 */
@Component
public class EngineConfigManage {

    @Resource
    private CoreDeEngineRepository coreDeEngineRepository;

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
}
