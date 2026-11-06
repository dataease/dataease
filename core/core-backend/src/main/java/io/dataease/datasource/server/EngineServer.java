package io.dataease.datasource.server;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.utils.*;

import io.dataease.api.ds.EngineApi;
import io.dataease.exception.DEException;
import io.dataease.result.ResultCode;
import io.dataease.datasource.dao.auto.entity.CoreDeEngine;
import io.dataease.datasource.dao.auto.repository.CoreDeEngineRepository;
import io.dataease.datasource.manage.EngineManage;
import io.dataease.datasource.provider.CalciteProvider;
import io.dataease.datasource.type.*;
import io.dataease.extensions.datasource.dto.DatasourceDTO;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/engine")
@Transactional(rollbackFor = Exception.class)
public class EngineServer implements EngineApi {
    @Resource
    private CoreDeEngineRepository coreDeEngineRepository;
    @Resource
    private EngineManage engineManage;
    @Resource
    private CalciteProvider calciteProvider;
    @Resource
    private CommonThreadPool commonThreadPool;

    @Override
    public DatasourceDTO getEngine() {
        DatasourceDTO datasourceDTO = new DatasourceDTO();
        List<CoreDeEngine> deEngines = coreDeEngineRepository.findAll();
        if (CollectionUtils.isEmpty(deEngines)) {
            return datasourceDTO;
        }
        BeanUtils.copyBean(datasourceDTO, deEngines.get(0));
        switch (datasourceDTO.getType()) {
            case "mysql":
                datasourceDTO.setConfiguration(JsonUtil.toJSONString(JsonUtil.parseObject(datasourceDTO.getConfiguration(), Mysql.class)).toString());
                break;
            case "StarRocks":
                datasourceDTO.setConfiguration(JsonUtil.toJSONString(JsonUtil.parseObject(datasourceDTO.getConfiguration(), StarRocks.class)).toString());
                break;
            case "h2":
                datasourceDTO.setConfiguration(JsonUtil.toJSONString(JsonUtil.parseObject(datasourceDTO.getConfiguration(), H2.class)).toString());
                break;
            case "oracle":
                datasourceDTO.setConfiguration(JsonUtil.toJSONString(JsonUtil.parseObject(datasourceDTO.getConfiguration(), Oracle.class)).toString());
                break;
            case "pg":
            case "kingbase":
            case "gaussdb":
                datasourceDTO.setConfiguration(JsonUtil.toJSONString(JsonUtil.parseObject(datasourceDTO.getConfiguration(), Pg.class)).toString());
                break;
            case "sqlServer":
                datasourceDTO.setConfiguration(JsonUtil.toJSONString(JsonUtil.parseObject(datasourceDTO.getConfiguration(), Sqlserver.class)).toString());
                break;
        }
        return datasourceDTO;
    }

    @Override
    public void save(DatasourceDTO datasourceDTO) {
        if (StringUtils.isNotEmpty(datasourceDTO.getConfiguration())) {
            datasourceDTO.setConfiguration(restoreConfiguration(datasourceDTO));
        }
        CoreDeEngine coreDeEngine = new CoreDeEngine();
        BeanUtils.copyBean(coreDeEngine, datasourceDTO);
        engineManage.validateH2JdbcConfig(coreDeEngine);
        if (coreDeEngine.getId() == null) {
            coreDeEngine.setId(IDUtils.snowID());
            datasourceDTO.setId(coreDeEngine.getId());
            coreDeEngine.setCreateTime(System.currentTimeMillis());
            coreDeEngine.setUpdateTime(System.currentTimeMillis());
            coreDeEngineRepository.saveAndFlush(coreDeEngine);
        } else {
            coreDeEngine.setUpdateTime(System.currentTimeMillis());
            coreDeEngineRepository.saveAndFlush(coreDeEngine);
        }
        Long revision = coreDeEngine.getUpdateTime();
        commonThreadPool.addTask(() -> {
            CoreDeEngine ds = coreDeEngineRepository.findById(coreDeEngine.getId()).orElse(null);
            if (ds == null) {
                LogUtil.error("Failed to update engine status: engine not found, id=" + coreDeEngine.getId());
                return;
            }
            try {
                calciteProvider.update(datasourceDTO);
                CoreDeEngine current = coreDeEngineRepository.findById(coreDeEngine.getId()).orElse(null);
                if (current != null && java.util.Objects.equals(current.getUpdateTime(), revision)) {
                    current.setStatus("success");
                    coreDeEngineRepository.saveAndFlush(current);
                } else {
                    LogUtil.info("Skip engine status update, a newer configuration revision exists for id=" + coreDeEngine.getId());
                }
            } catch (Exception e) {
                LogUtil.error("Failed to init engine: " + e.getMessage());
                CoreDeEngine current = coreDeEngineRepository.findById(coreDeEngine.getId()).orElse(null);
                if (current != null && java.util.Objects.equals(current.getUpdateTime(), revision)) {
                    current.setStatus("Error");
                    coreDeEngineRepository.saveAndFlush(current);
                } else {
                    LogUtil.info("Skip engine error status update, a newer configuration revision exists for id=" + coreDeEngine.getId());
                }
            }
        });
    }

    @Override
    public void validate(DatasourceDTO datasourceDTO) throws Exception {
        if (datasourceDTO == null || StringUtils.isBlank(datasourceDTO.getConfiguration())) {
            DEException.throwException(ResultCode.PARAM_IS_BLANK.code(), ResultCode.PARAM_IS_BLANK.message());
        }
        CoreDeEngine coreDeEngine = new CoreDeEngine();
        BeanUtils.copyBean(coreDeEngine, datasourceDTO);
        coreDeEngine.setConfiguration(restoreConfiguration(datasourceDTO));
        engineManage.validate(coreDeEngine, false);
    }

    private String restoreConfiguration(DatasourceDTO request) {
        String plain = new String(Base64.getDecoder().decode(request.getConfiguration()), java.nio.charset.StandardCharsets.UTF_8);
        CoreDeEngine stored = request.getId() == null ? null : coreDeEngineRepository.findById(request.getId()).orElse(null);
        if (stored != null && !stored.getType().equals(request.getType())) {
            throw new DEException(ResultCode.PARAM_IS_INVALID.code(), io.dataease.i18n.Translator.get("i18n_configuration_type_mismatch"));
        }
        return ConfigurationCredentials.restore(plain, stored == null ? null : stored.getConfiguration());
    }

    @Override
    public void validateById(Long id) throws Exception {
        engineManage.validate(coreDeEngineRepository.findById(id).orElse(null), true);
    }

    @Override
    public boolean supportSetKey() throws Exception {
        List<CoreDeEngine> deEngines = coreDeEngineRepository.findAll();
        if (CollectionUtils.isEmpty(deEngines)) {
            return false;
        } else {
            return !deEngines.getFirst().getType().equalsIgnoreCase("h2");
        }

    }
}
