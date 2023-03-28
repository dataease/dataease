package io.dataease.datasource.server;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.ds.DatasourceApi;
import io.dataease.api.ds.vo.DatasourceDTO;
import io.dataease.api.ds.vo.DatasourceType;
import io.dataease.datasource.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.dao.auto.mapper.CoreDatasourceMapper;
import io.dataease.i18n.Translator;
import io.dataease.utils.BeanUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
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
        datasourceMapper.updateById(coreDatasource);
        return dataSourceDTO;
    }

    @Override
    public List<DatasourceType> datasourceTypes(){
        return new ArrayList<>();
    }

    @Override
    public DatasourceDTO validate(DatasourceDTO dataSourceDTO) throws Exception{
        dataSourceDTO.setConfiguration(new String(Base64.getDecoder().decode(dataSourceDTO.getConfiguration())));
        DatasourceDTO datasourceDTO = new DatasourceDTO();
        return dataSourceDTO;
    }

    @Override
    public DatasourceDTO validate(String datasourceId) throws Exception{
        CoreDatasource coreDatasource = datasourceMapper.selectById(datasourceId);
        DatasourceDTO datasourceDTO = new DatasourceDTO();
        BeanUtils.copyBean(datasourceDTO, coreDatasource);
        return validate(datasourceDTO);
    }

    private  void preCheckDs(DatasourceDTO datasource) throws Exception {
        if (!datasourceTypes().stream().map(DatasourceType::getType).collect(Collectors.toList()).contains(datasource.getType())) {
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



}
