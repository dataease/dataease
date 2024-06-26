package io.dataease.datasource.server;

import io.dataease.api.ds.EngineApi;
import io.dataease.datasource.dao.auto.entity.CoreDeEngine;
import io.dataease.datasource.dao.auto.mapper.CoreDeEngineMapper;
import io.dataease.datasource.manage.EngineManage;
import io.dataease.datasource.provider.CalciteProvider;
import io.dataease.extensions.datasource.dto.DatasourceDTO;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.IDUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/engine")
@Transactional(rollbackFor = Exception.class)
public class EngineServer implements EngineApi {
    @Resource
    private CoreDeEngineMapper deEngineMapper;
    @Resource
    private EngineManage engineManage;
    @Resource
    private CalciteProvider calciteProvider;

    @Override
    public DatasourceDTO getEngine() {
        DatasourceDTO datasourceDTO = new DatasourceDTO();
        List<CoreDeEngine> deEngines = deEngineMapper.selectList(null);
        if (CollectionUtils.isEmpty(deEngines)) {
            return datasourceDTO;
        }
        return BeanUtils.copyBean(datasourceDTO, deEngines.get(0));
    }

    @Override
    public void save(DatasourceDTO datasourceDTO) {
        if (StringUtils.isNotEmpty(datasourceDTO.getConfiguration())) {
            datasourceDTO.setConfiguration(new String(Base64.getDecoder().decode(datasourceDTO.getConfiguration())));
        }
        CoreDeEngine coreDeEngine = new CoreDeEngine();
        BeanUtils.copyBean(coreDeEngine, datasourceDTO);
        if(coreDeEngine.getId() == null){
            coreDeEngine.setId(IDUtils.snowID());
            datasourceDTO.setId(coreDeEngine.getId());
            deEngineMapper.insert(coreDeEngine);
        }else {
            deEngineMapper.updateById(coreDeEngine);
        }
        calciteProvider.update(datasourceDTO);
    }

    @Override
    public void validate(DatasourceDTO datasourceDTO) throws Exception{
        CoreDeEngine coreDeEngine = new CoreDeEngine();
        BeanUtils.copyBean(coreDeEngine, datasourceDTO);
        coreDeEngine.setConfiguration(new String(Base64.getDecoder().decode(coreDeEngine.getConfiguration())));
        engineManage.validate(coreDeEngine);
    }

    @Override
    public void validateById(Long id) throws Exception {
        engineManage.validate(deEngineMapper.selectById(id));
    }


}
