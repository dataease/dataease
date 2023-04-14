package io.dataease.datasource.server;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.dataset.dto.DatasetTableDTO;
import io.dataease.api.ds.DatasourceApi;
import io.dataease.api.ds.vo.DatasourceConfiguration;
import io.dataease.api.ds.vo.DatasourceDTO;
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
    public DatasourceDTO save(DatasourceDTO dataSourceDTO) throws Exception {
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
    public DatasourceDTO update(DatasourceDTO dataSourceDTO) throws Exception {
        if (StringUtils.isEmpty(dataSourceDTO.getId())) {
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
    public Collection<DatasourceConfiguration> datasourceTypes() {
        Collection<DatasourceConfiguration> datasourceConfigurations = CommonBeanFactory.getApplicationContext().getBeansOfType(DatasourceConfiguration.class).values();
        return datasourceConfigurations;
    }

    @Override
    public DatasourceDTO validate(DatasourceDTO dataSourceDTO) throws Exception {
        dataSourceDTO.setConfiguration(new String(Base64.getDecoder().decode(dataSourceDTO.getConfiguration())));
        CoreDatasource coreDatasource = new CoreDatasource();
        BeanUtils.copyBean(coreDatasource, dataSourceDTO);
        checkDatasourceStatus(coreDatasource);
        dataSourceDTO.setStatus(coreDatasource.getStatus());
        return dataSourceDTO;
    }

    @Override
    public DatasourceDTO validate(String datasourceId) throws Exception {
        CoreDatasource coreDatasource = datasourceMapper.selectById(datasourceId);
        checkDatasourceStatus(coreDatasource);
        DatasourceDTO datasourceDTO = new DatasourceDTO();
        BeanUtils.copyBean(datasourceDTO, coreDatasource);
        return datasourceDTO;
    }

    @Override
    public List<DatasourceDTO> list() {
        // todo mock,获取数据源列表
        DatasourceDTO dto = new DatasourceDTO();
        dto.setId("1");
        dto.setName("de_demo");
        return Collections.singletonList(dto);
    }

    @Override
    public List<DatasetTableDTO> getTables(String datasourceId) {
        List<DatasetTableDTO> list = new ArrayList<>();
        // todo mock,获取数据库表格
        DatasetTableDTO t1 = new DatasetTableDTO();
        t1.setDatasourceId(1L);
        t1.setName("ds_group测试");
        t1.setTableName("dataset_group");
        t1.setType("db");

        DatasetTableDTO t2 = new DatasetTableDTO();
        t2.setDatasourceId(1L);
        t2.setName("ds_table测试");
        t2.setTableName("dataset_table");
        t2.setType("db");

        DatasetTableDTO t3 = new DatasetTableDTO();
        t3.setDatasourceId(1L);
        t3.setName("ds_field测试");
        t3.setTableName("dataset_table_field");
        t3.setType("db");

        DatasetTableDTO t4 = new DatasetTableDTO();
        t4.setDatasourceId(1L);
        t4.setName("ds_function测试");
        t4.setTableName("dataset_table_function");
        t4.setType("db");

        list.add(t1);
        list.add(t2);
        list.add(t3);
        list.add(t4);
        return list;
    }

    private void preCheckDs(DatasourceDTO datasource) throws Exception {
        if (!datasourceTypes().stream().map(DatasourceConfiguration::getType).collect(Collectors.toList()).contains(datasource.getType())) {
            throw new Exception("Datasource type not supported.");
        }
        //TODO check Configuration
        checkName(datasource.getName(), datasource.getType(), datasource.getId());
    }

    private void checkName(String name, String type, String id) throws Exception {
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
