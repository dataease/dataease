package io.dataease.datasource.server;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.ds.DatasourceApi;
import io.dataease.api.ds.vo.DatasourceDTO;
import io.dataease.api.ds.vo.DatasourceConfiguration;
import io.dataease.datasource.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.dao.auto.mapper.CoreDatasourceMapper;
import io.dataease.datasource.provider.ProviderUtil;
import io.dataease.datasource.request.DatasourceRequest;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.CommonBeanFactory;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/datasource")
public class DatasourceServer implements DatasourceApi {
    @Resource
    private CoreDatasourceMapper datasourceMapper;

    @Override
    public List<DatasourceDTO> query(String keyWord) {
        return null;
    }

    @Override
    public DatasourceDTO save(DatasourceDTO dataSourceDTO) throws Exception{
        dataSourceDTO.setConfiguration(new String(Base64.getDecoder().decode(dataSourceDTO.getConfiguration())));
        preCheckDs(dataSourceDTO);
        CoreDatasource coreDatasource = new CoreDatasource();
        dataSourceDTO.setId(UUID.randomUUID().toString());
        BeanUtils.copyBean(coreDatasource, dataSourceDTO);
        coreDatasource.setCreateTime(System.currentTimeMillis());
        checkDatasourceStatus(coreDatasource);
        datasourceMapper.insert(coreDatasource);
        return dataSourceDTO;
    }

    @Override
    public DatasourceDTO update(DatasourceDTO dataSourceDTO) throws Exception{
        if(StringUtils.isEmpty(dataSourceDTO.getId())){
            return save(dataSourceDTO);
        }
        dataSourceDTO.setConfiguration(new String(Base64.getDecoder().decode(dataSourceDTO.getConfiguration())));
        preCheckDs(dataSourceDTO);
        CoreDatasource coreDatasource = new CoreDatasource();
        BeanUtils.copyBean(coreDatasource, dataSourceDTO);
        coreDatasource.setUpdateTime(System.currentTimeMillis());
        checkDatasourceStatus(coreDatasource);
        datasourceMapper.updateById(coreDatasource);
        return dataSourceDTO;
    }

    @Override
    public Collection<DatasourceConfiguration> datasourceTypes(){
        Collection<DatasourceConfiguration> datasourceConfigurations = CommonBeanFactory.getApplicationContext().getBeansOfType(DatasourceConfiguration.class).values();
        return datasourceConfigurations;
    }

    @Override
    public DatasourceDTO validate(DatasourceDTO dataSourceDTO) throws Exception{
        dataSourceDTO.setConfiguration(new String(Base64.getDecoder().decode(dataSourceDTO.getConfiguration())));
        CoreDatasource coreDatasource = new CoreDatasource();
        BeanUtils.copyBean(coreDatasource, dataSourceDTO);
        checkDatasourceStatus(coreDatasource);
        dataSourceDTO.setStatus(coreDatasource.getStatus());
        return dataSourceDTO;
    }

    @Override
    public DatasourceDTO validate(String datasourceId) throws Exception{
        CoreDatasource coreDatasource = datasourceMapper.selectById(datasourceId);
        checkDatasourceStatus(coreDatasource);
        DatasourceDTO datasourceDTO = new DatasourceDTO();
        BeanUtils.copyBean(datasourceDTO, coreDatasource);
        return datasourceDTO;
    }

    private  void preCheckDs(DatasourceDTO datasource) throws Exception {
        if (!datasourceTypes().stream().map(DatasourceConfiguration::getType).collect(Collectors.toList()).contains(datasource.getType())) {
            throw new Exception("Datasource type not supported.");
        }
        //TODO check Configuration
        checkName(datasource.getName(), datasource.getType(), datasource.getId());
    }
    private void checkName(String name, String type, String id) throws Exception{
        QueryWrapper<CoreDatasource> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", name);
        queryWrapper.eq("type", type);
        if (StringUtils.isNotEmpty(id)) {
            queryWrapper.ne("id", id);
        }
        if (CollectionUtils.isEmpty(datasourceMapper.selectList(queryWrapper))) {
            throw new Exception("ds_name_exists");
        }
    }

    public void checkDatasourceStatus(CoreDatasource coreDatasource) {
        try {
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(coreDatasource);
            String status = ProviderUtil.getProvider(coreDatasource.getType()).checkStatus(datasourceRequest);
            coreDatasource.setStatus(status);
        } catch (Exception e) {
            coreDatasource.setStatus("Error");
        }
    }



}
